package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.VideoFailedEvent;
import br.com.video2frames.video2frames_video_service.application.port.VideoStoragePort;
import br.com.video2frames.video2frames_video_service.domain.exception.VideoNotFoundException;
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
public class HandleVideoFailedUseCase {

    private final VideoRepository videoRepository;
    private final VideoStatusHistoryRepository historyRepository;
    private final VideoStoragePort videoStoragePort;
    private final CacheManager cacheManager;

    public HandleVideoFailedUseCase(
            VideoRepository videoRepository,
            VideoStatusHistoryRepository historyRepository,
            VideoStoragePort videoStoragePort,
            CacheManager cacheManager) {
        this.videoRepository = videoRepository;
        this.historyRepository = historyRepository;
        this.videoStoragePort = videoStoragePort;
        this.cacheManager = cacheManager;
    }

    public void execute(VideoFailedEvent event) {
        Video video = videoRepository.findById(event.videoId())
                .orElseThrow(() -> {
                    log.warn("Evento video-failed recebido para vídeo inexistente {}", event.videoId());
                    return new VideoNotFoundException(event.videoId());
                });

        Video failed = videoRepository.save(video.markFailed(event.reason()));
        historyRepository.save(VideoStatusHistoryEntry.record(failed.getId(), failed.getStatus(), event.reason()));

        log.warn("Status do vídeo {} atualizado para {} (motivo: {})",
                failed.getId(), failed.getStatus(), event.reason());

        compensateUploadStep(failed);
        evictUserVideosCache(failed.getOwnerEmail());
    }

    private void evictUserVideosCache(String ownerEmail) {
        Cache cache = cacheManager.getCache("userVideos");
        if (cache != null) {
            cache.evict(ownerEmail);
        }
    }

    // Best-effort: não deve falhar o evento se a limpeza do S3 der erro.
    private void compensateUploadStep(Video failed) {
        try {
            videoStoragePort.deleteOriginalVideo(failed.getVideoKey());
            log.info("Saga: compensação aplicada — vídeo original {} removido do S3 após falha definitiva",
                    failed.getVideoKey());
        } catch (RuntimeException e) {
            log.warn("Saga: falha ao aplicar compensação (remoção de {} no S3); arquivo ficará órfão até limpeza manual",
                    failed.getVideoKey(), e);
        }
    }
}
