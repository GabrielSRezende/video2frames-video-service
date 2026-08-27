package br.com.video2frames.video2frames_video_service.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class VideoStatusHistoryEntry {

    private final UUID id;
    private final UUID videoId;
    private final VideoStatus status;
    private final String reason;
    private final OffsetDateTime createdAt;

    private VideoStatusHistoryEntry(UUID id, UUID videoId, VideoStatus status, String reason, OffsetDateTime createdAt) {
        this.id = id;
        this.videoId = videoId;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static VideoStatusHistoryEntry record(UUID videoId, VideoStatus status, String reason) {
        return new VideoStatusHistoryEntry(null, videoId, status, reason, OffsetDateTime.now());
    }

    public static VideoStatusHistoryEntry reconstruct(
            UUID id, UUID videoId, VideoStatus status, String reason, OffsetDateTime createdAt) {
        return new VideoStatusHistoryEntry(id, videoId, status, reason, createdAt);
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
