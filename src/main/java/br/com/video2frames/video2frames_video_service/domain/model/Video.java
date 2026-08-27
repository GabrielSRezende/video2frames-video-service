package br.com.video2frames.video2frames_video_service.domain.model;

import br.com.video2frames.video2frames_video_service.domain.exception.InvalidVideoStateTransitionException;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class Video {

    private final UUID id;
    private final String ownerEmail;
    private final String fileName;
    private final VideoStatus status;
    private final String videoKey;
    private final String zipKey;
    private final Integer frameCount;
    private final String errorReason;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    private Video(
            UUID id, String ownerEmail, String fileName, VideoStatus status, String videoKey,
            String zipKey, Integer frameCount, String errorReason,
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

    public static Video upload(String ownerEmail, String fileName, String videoKey) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Video(null, ownerEmail, fileName, VideoStatus.UPLOADED, videoKey, null, null, null, now, now);
    }

    public Video withVideoKey(String videoKey) {
        return new Video(id, ownerEmail, fileName, status, videoKey, zipKey, frameCount, errorReason, createdAt, OffsetDateTime.now());
    }

    public static Video reconstruct(
            UUID id, String ownerEmail, String fileName, VideoStatus status, String videoKey,
            String zipKey, Integer frameCount, String errorReason,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return new Video(id, ownerEmail, fileName, status, videoKey, zipKey, frameCount, errorReason, createdAt, updatedAt);
    }

    public Video withId(UUID id) {
        return new Video(id, ownerEmail, fileName, status, videoKey, zipKey, frameCount, errorReason, createdAt, updatedAt);
    }

    public Video markProcessing() {
        requireStatus(VideoStatus.UPLOADED, "iniciar processamento");
        return new Video(id, ownerEmail, fileName, VideoStatus.PROCESSING, videoKey, zipKey, frameCount, errorReason, createdAt, OffsetDateTime.now());
    }

    public Video markCompleted(String zipKey, int frameCount) {
        requireStatus(VideoStatus.PROCESSING, "concluir processamento");
        return new Video(id, ownerEmail, fileName, VideoStatus.COMPLETED, videoKey, zipKey, frameCount, null, createdAt, OffsetDateTime.now());
    }

    public Video markFailed(String reason) {
        if (status == VideoStatus.COMPLETED || status == VideoStatus.FAILED) {
            throw new InvalidVideoStateTransitionException(
                    "Não é possível marcar como falho um vídeo em estado terminal: " + status);
        }
        return new Video(id, ownerEmail, fileName, VideoStatus.FAILED, videoKey, zipKey, frameCount, reason, createdAt, OffsetDateTime.now());
    }

    public boolean isDownloadable() {
        return status == VideoStatus.COMPLETED && zipKey != null;
    }

    public boolean belongsTo(String email) {
        return ownerEmail.equalsIgnoreCase(email);
    }

    private void requireStatus(VideoStatus expected, String action) {
        if (status != expected) {
            throw new InvalidVideoStateTransitionException(
                    "Não é possível " + action + ": status atual é " + status + ", esperado " + expected);
        }
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
