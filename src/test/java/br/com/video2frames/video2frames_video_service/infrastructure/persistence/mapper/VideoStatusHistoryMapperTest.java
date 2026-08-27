package br.com.video2frames.video2frames_video_service.infrastructure.persistence.mapper;

import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatusHistoryEntry;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.entity.VideoStatusHistoryJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoStatusHistoryMapperTest {

    private final VideoStatusHistoryMapper mapper = new VideoStatusHistoryMapper();

    @Test
    void toEntity_convertEntradaDeDominioParaEntidadeJpa() {
        UUID id = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        VideoStatusHistoryEntry entry = VideoStatusHistoryEntry.reconstruct(id, videoId, VideoStatus.FAILED, "motivo", createdAt);

        VideoStatusHistoryJpaEntity entity = mapper.toEntity(entry);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getVideoId()).isEqualTo(videoId);
        assertThat(entity.getStatus()).isEqualTo(VideoStatus.FAILED);
        assertThat(entity.getReason()).isEqualTo("motivo");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void toDomain_convertEntidadeJpaParaEntradaDeDominio() {
        UUID id = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        VideoStatusHistoryJpaEntity entity = new VideoStatusHistoryJpaEntity(id, videoId, VideoStatus.COMPLETED, null, createdAt);

        VideoStatusHistoryEntry entry = mapper.toDomain(entity);

        assertThat(entry.getId()).isEqualTo(id);
        assertThat(entry.getVideoId()).isEqualTo(videoId);
        assertThat(entry.getStatus()).isEqualTo(VideoStatus.COMPLETED);
        assertThat(entry.getReason()).isNull();
        assertThat(entry.getCreatedAt()).isEqualTo(createdAt);
    }
}
