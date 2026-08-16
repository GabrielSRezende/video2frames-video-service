package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.VideoDownload;
import br.com.video2frames.video2frames_video_service.application.port.VideoStoragePort;
import br.com.video2frames.video2frames_video_service.domain.exception.InvalidVideoStateTransitionException;
import br.com.video2frames.video2frames_video_service.domain.exception.VideoAccessDeniedException;
import br.com.video2frames.video2frames_video_service.domain.exception.VideoNotFoundException;
import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DownloadVideoZipUseCase {

    private final VideoRepository videoRepository;
    private final VideoStoragePort videoStoragePort;

    public DownloadVideoZipUseCase(VideoRepository videoRepository, VideoStoragePort videoStoragePort) {
        this.videoRepository = videoRepository;
        this.videoStoragePort = videoStoragePort;
    }

    public VideoDownload execute(String requesterEmail, UUID videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new VideoNotFoundException(videoId));

        if (!video.belongsTo(requesterEmail)) {
            throw new VideoAccessDeniedException();
        }

        if (!video.isDownloadable()) {
            throw new InvalidVideoStateTransitionException(
                    "Vídeo ainda não está pronto para download (status atual: " + video.getStatus() + ")");
        }

        String zipFileName = video.getFileName().replaceAll("\\.[^.]+$", "") + "-frames.zip";
        return new VideoDownload(videoStoragePort.fetchZip(video.getZipKey()), zipFileName);
    }
}
