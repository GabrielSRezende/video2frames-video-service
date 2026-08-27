package br.com.video2frames.video2frames_video_service.infrastructure.persistence.mapper;

import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.entity.VideoJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class VideoMapper {

    public VideoJpaEntity toEntity(Video video) {
        return new VideoJpaEntity(
                video.getId(), video.getOwnerEmail(), video.getFileName(), video.getStatus(),
                video.getVideoKey(), video.getZipKey(), video.getFrameCount(), video.getErrorReason(),
                video.getCreatedAt(), video.getUpdatedAt());
    }

    public Video toDomain(VideoJpaEntity entity) {
        return Video.reconstruct(
                entity.getId(), entity.getOwnerEmail(), entity.getFileName(), entity.getStatus(),
                entity.getVideoKey(), entity.getZipKey(), entity.getFrameCount(), entity.getErrorReason(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
