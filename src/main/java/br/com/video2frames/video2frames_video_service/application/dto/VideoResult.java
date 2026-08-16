package br.com.video2frames.video2frames_video_service.application.dto;

import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VideoResult(
        UUID id,
        String fileName,
        VideoStatus status,
        Integer frameCount,
        String errorReason,
        OffsetDateTime createdAt) {

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
