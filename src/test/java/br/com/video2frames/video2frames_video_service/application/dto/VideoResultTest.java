package br.com.video2frames.video2frames_video_service.application.dto;

import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoResultTest {

    @Test
    void from_convertVideoParaVideoResultComOsCamposCorrespondentes() {
        UUID id = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now().minusMinutes(5);
        Video video = Video.reconstruct(
                id, "gabriel@video2frames.com", "meu-video.mp4", VideoStatus.COMPLETED,
                "videos/gabriel/id.mp4", "videos/gabriel/id-frames.zip", 42, null,
                createdAt, OffsetDateTime.now());

        VideoResult result = VideoResult.from(video);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.fileName()).isEqualTo("meu-video.mp4");
        assertThat(result.status()).isEqualTo(VideoStatus.COMPLETED);
        assertThat(result.frameCount()).isEqualTo(42);
        assertThat(result.errorReason()).isNull();
        assertThat(result.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void from_quandoVideoFalhou_incluiOMotivoDoErro() {
        Video video = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .markFailed("Formato de codec não suportado");

        VideoResult result = VideoResult.from(video);

        assertThat(result.status()).isEqualTo(VideoStatus.FAILED);
        assertThat(result.errorReason()).isEqualTo("Formato de codec não suportado");
        assertThat(result.frameCount()).isNull();
    }
}
