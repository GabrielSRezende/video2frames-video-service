package br.com.video2frames.video2frames_video_service.infrastructure.web.controller;

import br.com.video2frames.video2frames_video_service.application.dto.UploadVideoCommand;
import br.com.video2frames.video2frames_video_service.application.dto.VideoDownload;
import br.com.video2frames.video2frames_video_service.application.usecase.DownloadVideoZipUseCase;
import br.com.video2frames.video2frames_video_service.application.usecase.ListUserVideosUseCase;
import br.com.video2frames.video2frames_video_service.application.usecase.UploadVideoUseCase;
import br.com.video2frames.video2frames_video_service.infrastructure.web.dto.VideoResponseDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;

/**
 * A camada web só traduz HTTP <-> comandos de aplicação; o e-mail do
 * usuário vem do Authentication populado pelo JwtAuthenticationFilter, não
 * de um parâmetro que o cliente poderia forjar.
 */
@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final UploadVideoUseCase uploadVideoUseCase;
    private final ListUserVideosUseCase listUserVideosUseCase;
    private final DownloadVideoZipUseCase downloadVideoZipUseCase;

    public VideoController(
            UploadVideoUseCase uploadVideoUseCase,
            ListUserVideosUseCase listUserVideosUseCase,
            DownloadVideoZipUseCase downloadVideoZipUseCase) {
        this.uploadVideoUseCase = uploadVideoUseCase;
        this.listUserVideosUseCase = listUserVideosUseCase;
        this.downloadVideoZipUseCase = downloadVideoZipUseCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoResponseDto> upload(
            Authentication authentication, @RequestParam("file") MultipartFile file) {

        try {
            var command = new UploadVideoCommand(
                    authentication.getName(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getInputStream(),
                    file.getSize());

            var result = uploadVideoUseCase.execute(command);
            return ResponseEntity.ok(VideoResponseDto.from(result));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado", e);
        }
    }

    @GetMapping
    public ResponseEntity<List<VideoResponseDto>> list(Authentication authentication) {
        var videos = listUserVideosUseCase.execute(authentication.getName()).stream()
                .map(VideoResponseDto::from)
                .toList();
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(Authentication authentication, @PathVariable UUID id) {
        VideoDownload download = downloadVideoZipUseCase.execute(authentication.getName(), id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.suggestedFileName() + "\"")
                .body(new InputStreamResource(download.content()));
    }
}
