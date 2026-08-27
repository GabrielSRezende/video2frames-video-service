package br.com.video2frames.video2frames_video_service.infrastructure.web.dto;

import br.com.video2frames.video2frames_video_service.application.dto.VideoResult;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoResponseDtoTest {

    @Test
    void from_convertVideoResultParaVideoResponseDtoComOsMesmosCampos() {
        UUID id = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        VideoResult result = new VideoResult(id, "meu-video.mp4", VideoStatus.COMPLETED, 20, null, createdAt);

        VideoResponseDto dto = VideoResponseDto.from(result);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.fileName()).isEqualTo("meu-video.mp4");
        assertThat(dto.status()).isEqualTo(VideoStatus.COMPLETED);
        assertThat(dto.frameCount()).isEqualTo(20);
        assertThat(dto.errorReason()).isNull();
        assertThat(dto.createdAt()).isEqualTo(createdAt);
    }
}
