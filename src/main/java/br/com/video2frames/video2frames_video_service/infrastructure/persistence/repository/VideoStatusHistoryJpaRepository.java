package br.com.video2frames.video2frames_video_service.infrastructure.persistence.repository;

import br.com.video2frames.video2frames_video_service.infrastructure.persistence.entity.VideoStatusHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoStatusHistoryJpaRepository extends JpaRepository<VideoStatusHistoryJpaEntity, UUID> {
}
