package br.com.video2frames.video2frames_video_service.application.dto;

import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Implementa Serializable porque este é o tipo cacheado no Redis
 * ("userVideos" — ver CacheConfig): a lista de vídeos por usuário precisa
 * sobreviver ao round-trip de serialização JDK usado pelo RedisCacheManager.
 */
public record VideoResult(
        UUID id,
        String fileName,
        VideoStatus status,
        Integer frameCount,
        String errorReason,
        OffsetDateTime createdAt) implements Serializable {

    public static VideoResult from(Video video) {
        return new VideoResult(
                video.getId(),
                video.getFileName(),
                video.getStatus(),
                video.getFrameCount(),
                video.getErrorReason(),
                video.getCreatedAt());
    }
}
