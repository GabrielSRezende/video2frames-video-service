package br.com.video2frames.video2frames_video_service.infrastructure.persistence.entity;

import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "video_status_history")
public class VideoStatusHistoryJpaEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "video_id", nullable = false)
    private UUID videoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VideoStatus status;

    @Column(length = 1000)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected VideoStatusHistoryJpaEntity() {
        // exigido pelo JPA
    }

    public VideoStatusHistoryJpaEntity(UUID id, UUID videoId, VideoStatus status, String reason, OffsetDateTime createdAt) {
        this.id = id;
        this.videoId = videoId;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVideoId() {
        return videoId;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
