package br.com.video2frames.video2frames_video_service.domain.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoStatusHistoryEntryTest {

    @Test
    void record_criaEntradaSemIdComACarimboDeTempoAtual() {
        UUID videoId = UUID.randomUUID();

        VideoStatusHistoryEntry entry = VideoStatusHistoryEntry.record(videoId, VideoStatus.UPLOADED, null);

        assertThat(entry.getId()).isNull();
        assertThat(entry.getVideoId()).isEqualTo(videoId);
        assertThat(entry.getStatus()).isEqualTo(VideoStatus.UPLOADED);
        assertThat(entry.getReason()).isNull();
        assertThat(entry.getCreatedAt()).isNotNull();
    }

    @Test
    void record_quandoStatusFailed_incluiOMotivo() {
        UUID videoId = UUID.randomUUID();

        VideoStatusHistoryEntry entry = VideoStatusHistoryEntry.record(videoId, VideoStatus.FAILED, "erro no worker");

        assertThat(entry.getStatus()).isEqualTo(VideoStatus.FAILED);
        assertThat(entry.getReason()).isEqualTo("erro no worker");
    }

    @Test
    void reconstruct_restauraEntradaExistenteComTodosOsCampos() {
        UUID id = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);

        VideoStatusHistoryEntry entry = VideoStatusHistoryEntry.reconstruct(
                id, videoId, VideoStatus.COMPLETED, null, createdAt);

        assertThat(entry.getId()).isEqualTo(id);
        assertThat(entry.getVideoId()).isEqualTo(videoId);
        assertThat(entry.getStatus()).isEqualTo(VideoStatus.COMPLETED);
        assertThat(entry.getCreatedAt()).isEqualTo(createdAt);
    }
}
