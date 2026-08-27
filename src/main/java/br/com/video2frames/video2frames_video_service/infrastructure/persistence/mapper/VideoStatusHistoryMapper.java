package br.com.video2frames.video2frames_video_service.infrastructure.persistence.mapper;

import br.com.video2frames.video2frames_video_service.domain.model.VideoStatusHistoryEntry;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.entity.VideoStatusHistoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class VideoStatusHistoryMapper {

    public VideoStatusHistoryJpaEntity toEntity(VideoStatusHistoryEntry entry) {
        return new VideoStatusHistoryJpaEntity(
                entry.getId(), entry.getVideoId(), entry.getStatus(), entry.getReason(), entry.getCreatedAt());
    }

    public VideoStatusHistoryEntry toDomain(VideoStatusHistoryJpaEntity entity) {
        return VideoStatusHistoryEntry.reconstruct(
                entity.getId(), entity.getVideoId(), entity.getStatus(), entity.getReason(), entity.getCreatedAt());
    }
}
