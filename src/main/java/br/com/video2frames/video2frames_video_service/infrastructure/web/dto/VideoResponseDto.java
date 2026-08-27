package br.com.video2frames.video2frames_video_service.infrastructure.web.dto;

import br.com.video2frames.video2frames_video_service.application.dto.VideoResult;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VideoResponseDto(
        UUID id,
        String fileName,
        VideoStatus status,
        Integer frameCount,
        String errorReason,
        OffsetDateTime createdAt) {

    public static VideoResponseDto from(VideoResult result) {
        return new VideoResponseDto(
                result.id(), result.fileName(), result.status(),
                result.frameCount(), result.errorReason(), result.createdAt());
    }
}
