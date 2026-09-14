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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UploadVideoUseCase {

    private static final String PENDING_KEY_PLACEHOLDER = "pending";

    private final VideoRepository videoRepository;
    private final VideoStatusHistoryRepository historyRepository;
    private final VideoStoragePort videoStoragePort;
    private final VideoProcessingQueuePort videoProcessingQueuePort;
    private final CacheManager cacheManager;

    public UploadVideoUseCase(
            VideoRepository videoRepository,
            VideoStatusHistoryRepository historyRepository,
            VideoStoragePort videoStoragePort,
            VideoProcessingQueuePort videoProcessingQueuePort,
            CacheManager cacheManager) {
        this.videoRepository = videoRepository;
        this.historyRepository = historyRepository;
        this.videoStoragePort = videoStoragePort;
        this.videoProcessingQueuePort = videoProcessingQueuePort;
        this.cacheManager = cacheManager;
    }

    public VideoResult execute(UploadVideoCommand command) {
        if (command.contentType() == null || !command.contentType().startsWith("video/")) {
            log.warn("Upload rejeitado para {}: content-type não suportado ({})",
                    command.ownerEmail(), command.contentType());
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
                new VideoUploadedEvent(withKey.getId(), withKey.getOwnerEmail(), videoKey, withKey.getFileName()));

        Video processing = videoRepository.save(withKey.markProcessing());
        historyRepository.save(VideoStatusHistoryEntry.record(processing.getId(), processing.getStatus(), null));

        log.info("Vídeo {} enviado por {} e publicado para processamento", processing.getId(), processing.getOwnerEmail());

        evictUserVideosCache(processing.getOwnerEmail());

        return VideoResult.from(processing);
    }

    private void evictUserVideosCache(String ownerEmail) {
        Cache cache = cacheManager.getCache("userVideos");
        if (cache != null) {
            cache.evict(ownerEmail);
        }
    }
}
