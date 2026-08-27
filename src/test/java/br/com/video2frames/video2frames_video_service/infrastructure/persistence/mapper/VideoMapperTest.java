package br.com.video2frames.video2frames_video_service.infrastructure.persistence.mapper;

import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.entity.VideoJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoMapperTest {

    private final VideoMapper mapper = new VideoMapper();

    @Test
    void toEntity_convertVideoDeDominioParaEntidadeJpa() {
        UUID id = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();
        Video video = Video.reconstruct(
                id, "gabriel@video2frames.com", "meu-video.mp4", VideoStatus.COMPLETED,
                "videos/gabriel/id.mp4", "videos/gabriel/id-frames.zip", 30, null, createdAt, updatedAt);

        VideoJpaEntity entity = mapper.toEntity(video);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getOwnerEmail()).isEqualTo("gabriel@video2frames.com");
        assertThat(entity.getFileName()).isEqualTo("meu-video.mp4");
        assertThat(entity.getStatus()).isEqualTo(VideoStatus.COMPLETED);
        assertThat(entity.getVideoKey()).isEqualTo("videos/gabriel/id.mp4");
        assertThat(entity.getZipKey()).isEqualTo("videos/gabriel/id-frames.zip");
        assertThat(entity.getFrameCount()).isEqualTo(30);
        assertThat(entity.getErrorReason()).isNull();
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toDomain_convertEntidadeJpaParaVideoDeDominio() {
        UUID id = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();
        VideoJpaEntity entity = new VideoJpaEntity(
                id, "gabriel@video2frames.com", "meu-video.mp4", VideoStatus.FAILED,
                "videos/gabriel/id.mp4", null, null, "erro no worker", createdAt, updatedAt);

        Video video = mapper.toDomain(entity);

        assertThat(video.getId()).isEqualTo(id);
        assertThat(video.getOwnerEmail()).isEqualTo("gabriel@video2frames.com");
        assertThat(video.getFileName()).isEqualTo("meu-video.mp4");
        assertThat(video.getStatus()).isEqualTo(VideoStatus.FAILED);
        assertThat(video.getVideoKey()).isEqualTo("videos/gabriel/id.mp4");
        assertThat(video.getErrorReason()).isEqualTo("erro no worker");
        assertThat(video.getCreatedAt()).isEqualTo(createdAt);
        assertThat(video.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
