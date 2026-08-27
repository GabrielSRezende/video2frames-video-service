package br.com.video2frames.video2frames_video_service.infrastructure.web.controller;

import br.com.video2frames.video2frames_video_service.application.dto.VideoDownload;
import br.com.video2frames.video2frames_video_service.application.dto.VideoResult;
import br.com.video2frames.video2frames_video_service.application.usecase.DownloadVideoZipUseCase;
import br.com.video2frames.video2frames_video_service.application.usecase.ListUserVideosUseCase;
import br.com.video2frames.video2frames_video_service.application.usecase.UploadVideoUseCase;
import br.com.video2frames.video2frames_video_service.domain.exception.InvalidVideoStateTransitionException;
import br.com.video2frames.video2frames_video_service.domain.exception.UnsupportedVideoFormatException;
import br.com.video2frames.video2frames_video_service.domain.exception.VideoAccessDeniedException;
import br.com.video2frames.video2frames_video_service.domain.exception.VideoNotFoundException;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;
import br.com.video2frames.video2frames_video_service.infrastructure.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    @Mock
    private UploadVideoUseCase uploadVideoUseCase;

    @Mock
    private ListUserVideosUseCase listUserVideosUseCase;

    @Mock
    private DownloadVideoZipUseCase downloadVideoZipUseCase;

    private VideoController controller;

    private MockMvc mockMvc;

    private final Authentication authentication =
            new UsernamePasswordAuthenticationToken("gabriel@video2frames.com", null);

    @BeforeEach
    void setUp() {
        controller = new VideoController(uploadVideoUseCase, listUserVideosUseCase, downloadVideoZipUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void upload_comArquivoValido_retorna200ComOVideoResponseDto() throws Exception {
        UUID videoId = UUID.randomUUID();
        VideoResult result = new VideoResult(
                videoId, "meu-video.mp4", VideoStatus.PROCESSING, null, null, OffsetDateTime.now());
        when(uploadVideoUseCase.execute(any())).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile("file", "meu-video.mp4", "video/mp4", "conteudo-fake".getBytes());

        mockMvc.perform(multipart("/api/videos").file(file).principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(videoId.toString()))
                .andExpect(jsonPath("$.fileName").value("meu-video.mp4"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void upload_quandoFormatoNaoEhSuportado_retorna400() throws Exception {
        when(uploadVideoUseCase.execute(any())).thenThrow(new UnsupportedVideoFormatException("application/pdf"));

        MockMultipartFile file = new MockMultipartFile("file", "documento.pdf", "application/pdf", "conteudo".getBytes());

        mockMvc.perform(multipart("/api/videos").file(file).principal(authentication))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void upload_usaOEmailDaAutenticacaoComoDonoDoVideo() throws Exception {
        VideoResult result = new VideoResult(
                UUID.randomUUID(), "meu-video.mp4", VideoStatus.PROCESSING, null, null, OffsetDateTime.now());
        when(uploadVideoUseCase.execute(any())).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile("file", "meu-video.mp4", "video/mp4", "conteudo-fake".getBytes());

        mockMvc.perform(multipart("/api/videos").file(file).principal(authentication))
                .andExpect(status().isOk());

        var captor = org.mockito.ArgumentCaptor.forClass(
                br.com.video2frames.video2frames_video_service.application.dto.UploadVideoCommand.class);
        org.mockito.Mockito.verify(uploadVideoUseCase).execute(captor.capture());
        assertThat(captor.getValue().ownerEmail()).isEqualTo("gabriel@video2frames.com");
    }

    @Test
    void upload_quandoLeituraDoArquivoFalha_lancaUncheckedIOException() throws Exception {
        MultipartFile brokenFile = org.mockito.Mockito.mock(MultipartFile.class);
        when(brokenFile.getInputStream()).thenThrow(new IOException("falha ao ler o stream"));

        assertThatThrownBy(() -> controller.upload(authentication, brokenFile))
                .isInstanceOf(UncheckedIOException.class);
    }

    @Test
    void list_retorna200ComAListaDeVideosDoUsuario() throws Exception {
        VideoResult result = new VideoResult(
                UUID.randomUUID(), "meu-video.mp4", VideoStatus.COMPLETED, 30, null, OffsetDateTime.now());
        when(listUserVideosUseCase.execute("gabriel@video2frames.com")).thenReturn(List.of(result));

        mockMvc.perform(get("/api/videos").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("meu-video.mp4"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].frameCount").value(30));
    }

    @Test
    void list_quandoUsuarioNaoTemVideos_retorna200ComListaVazia() throws Exception {
        when(listUserVideosUseCase.execute("gabriel@video2frames.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/videos").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void download_comVideoPronto_retorna200ComOConteudoDoZipEHeaderDeAnexo() throws Exception {
        UUID videoId = UUID.randomUUID();
        VideoDownload download = new VideoDownload(
                new ByteArrayInputStream("zip-bytes".getBytes()), "meu-video-frames.zip");
        when(downloadVideoZipUseCase.execute("gabriel@video2frames.com", videoId)).thenReturn(download);

        mockMvc.perform(get("/api/videos/{id}/download", videoId).principal(authentication))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"meu-video-frames.zip\""));
    }

    @Test
    void download_quandoVideoNaoEncontrado_retorna404() throws Exception {
        UUID videoId = UUID.randomUUID();
        when(downloadVideoZipUseCase.execute(eq("gabriel@video2frames.com"), eq(videoId)))
                .thenThrow(new VideoNotFoundException(videoId));

        mockMvc.perform(get("/api/videos/{id}/download", videoId).principal(authentication))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void download_quandoVideoPertenceAOutroUsuario_retorna403() throws Exception {
        UUID videoId = UUID.randomUUID();
        when(downloadVideoZipUseCase.execute(eq("gabriel@video2frames.com"), eq(videoId)))
                .thenThrow(new VideoAccessDeniedException());

        mockMvc.perform(get("/api/videos/{id}/download", videoId).principal(authentication))
                .andExpect(status().isForbidden());
    }

    @Test
    void download_quandoVideoAindaNaoEstaPronto_retorna409() throws Exception {
        UUID videoId = UUID.randomUUID();
        when(downloadVideoZipUseCase.execute(eq("gabriel@video2frames.com"), eq(videoId)))
                .thenThrow(new InvalidVideoStateTransitionException("Vídeo ainda não está pronto para download"));

        mockMvc.perform(get("/api/videos/{id}/download", videoId).principal(authentication))
                .andExpect(status().isConflict());
    }
}
