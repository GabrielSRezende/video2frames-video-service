package br.com.video2frames.video2frames_video_service.infrastructure.persistence.repository;

import br.com.video2frames.video2frames_video_service.infrastructure.persistence.entity.VideoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VideoJpaRepository extends JpaRepository<VideoJpaEntity, UUID> {

    List<VideoJpaEntity> findAllByOwnerEmail(String ownerEmail);
}
