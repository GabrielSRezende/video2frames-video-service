package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.UploadVideoCommand;
import br.com.video2frames.video2frames_video_service.application.dto.VideoResult;
import br.com.video2frames.video2frames_video_service.application.dto.VideoUploadedEvent;
import br.com.video2frames.video2frames_video_service.application.port.VideoProcessingQueuePort;
import br.com.video2frames.video2frames_video_service.application.port.VideoStoragePort;
import br.com.video2frames.video2frames_video_service.domain.exception.UnsupportedVideoFormatException;
import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatusHistoryEntry;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoRepository;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoStatusHistoryRepository;
import org.springframework.stereotype.Component;

/**
 * Fluxo: persiste o vídeo (pra ter um id definitivo), envia o arquivo pro
 * storage, registra a key definitiva, publica o evento na fila de
 * processamento e só então marca como PROCESSING. Cada etapa é persistida
 * separadamente para que, se algo falhar no meio do caminho, o estado no
 * banco reflita exatamente até onde chegamos.
 */
@Component
public class UploadVideoUseCase {

    private static final String PENDING_KEY_PLACEHOLDER = "pending";

    private final VideoRepository videoRepository;
    private final VideoStatusHistoryRepository historyRepository;
    private final VideoStoragePort videoStoragePort;
    private final VideoProcessingQueuePort videoProcessingQueuePort;

    public UploadVideoUseCase(
            VideoRepository videoRepository,
            VideoStatusHistoryRepository historyRepository,
            VideoStoragePort videoStoragePort,
            VideoProcessingQueuePort videoProcessingQueuePort) {
        this.videoRepository = videoRepository;
        this.historyRepository = historyRepository;
        this.videoStoragePort = videoStoragePort;
        this.videoProcessingQueuePort = videoProcessingQueuePort;
    }

    public VideoResult execute(UploadVideoCommand command) {
        if (command.contentType() == null || !command.contentType().startsWith("video/")) {
            throw new UnsupportedVideoFormatException(command.contentType());
        }

        Video created = videoRepository.save(
                Video.upload(command.ownerEmail(), command.fileName(), PENDING_KEY_PLACEHOLDER));
        historyRepository.save(VideoStatusHistoryEntry.record(created.getId(), created.getStatus(), null));

        String videoKey = videoStoragePort.storeOriginalVideo(
                created.getId(), command.ownerEmail(), command.fileName(),
                command.content(), command.contentLength(), command.contentType());

        Video withKey = videoRepository.save(created.withVideoKey(videoKey));

        videoProcessingQueuePort.publishVideoUploaded(
                new VideoUploadedEvent(withKey.getId(), withKey.getOwnerEmail(), videoKey));

        Video processing = videoRepository.save(withKey.markProcessing());
        historyRepository.save(VideoStatusHistoryEntry.record(processing.getId(), processing.getStatus(), null));

        return VideoResult.from(processing);
    }
}
