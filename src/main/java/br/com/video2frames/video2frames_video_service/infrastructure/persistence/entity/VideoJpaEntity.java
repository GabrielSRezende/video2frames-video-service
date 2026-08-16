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
@Table(name = "videos")
public class VideoJpaEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VideoStatus status;

    @Column(name = "video_key", nullable = false)
    private String videoKey;

    @Column(name = "zip_key")
    private String zipKey;

    @Column(name = "frame_count")
    private Integer frameCount;

    @Column(name = "error_reason", length = 1000)
    private String errorReason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected VideoJpaEntity() {
        // exigido pelo JPA
    }

    public VideoJpaEntity(
            UUID id, String ownerEmail, String fileName,
            VideoStatus status,
            String videoKey, String zipKey, Integer frameCount, String errorReason,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.ownerEmail = ownerEmail;
        this.fileName = fileName;
        this.status = status;
        this.videoKey = videoKey;
        this.zipKey = zipKey;
        this.frameCount = frameCount;
        this.errorReason = errorReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public String getFileName() {
        return fileName;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public String getVideoKey() {
        return videoKey;
    }

    public String getZipKey() {
        return zipKey;
    }

    public Integer getFrameCount() {
        return frameCount;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
