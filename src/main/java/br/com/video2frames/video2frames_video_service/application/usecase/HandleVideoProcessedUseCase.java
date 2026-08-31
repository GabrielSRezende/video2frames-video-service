package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.VideoProcessedEvent;
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
public class HandleVideoProcessedUseCase {

    private final VideoRepository videoRepository;
    private final VideoStatusHistoryRepository historyRepository;
    private final CacheManager cacheManager;

    public HandleVideoProcessedUseCase(
            VideoRepository videoRepository, VideoStatusHistoryRepository historyRepository, CacheManager cacheManager) {
        this.videoRepository = videoRepository;
        this.historyRepository = historyRepository;
        this.cacheManager = cacheManager;
    }

    public void execute(VideoProcessedEvent event) {
        Video video = videoRepository.findById(event.videoId())
                .orElseThrow(() -> {
                    log.warn("Evento video-processed recebido para vídeo inexistente {}", event.videoId());
                    return new VideoNotFoundException(event.videoId());
                });

        Video completed = videoRepository.save(video.markCompleted(event.zipKey(), event.frameCount()));
        historyRepository.save(VideoStatusHistoryEntry.record(completed.getId(), completed.getStatus(), null));

        log.info("Status do vídeo {} atualizado para {} ({} frames extraídos)",
                completed.getId(), completed.getStatus(), event.frameCount());

        evictUserVideosCache(completed.getOwnerEmail());
    }

    private void evictUserVideosCache(String ownerEmail) {
        Cache cache = cacheManager.getCache("userVideos");
        if (cache != null) {
            cache.evict(ownerEmail);
        }
    }
}
