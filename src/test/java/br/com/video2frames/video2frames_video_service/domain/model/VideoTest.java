package br.com.video2frames.video2frames_video_service.domain.model;

import br.com.video2frames.video2frames_video_service.domain.exception.InvalidVideoStateTransitionException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VideoTest {

    @Test
    void upload_criaVideoSemIdComStatusUploaded() {
        Video video = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4");

        assertThat(video.getId()).isNull();
        assertThat(video.getOwnerEmail()).isEqualTo("gabriel@video2frames.com");
        assertThat(video.getFileName()).isEqualTo("meu-video.mp4");
        assertThat(video.getStatus()).isEqualTo(VideoStatus.UPLOADED);
        assertThat(video.getVideoKey()).isEqualTo("videos/gabriel/id.mp4");
        assertThat(video.getZipKey()).isNull();
        assertThat(video.getFrameCount()).isNull();
        assertThat(video.getErrorReason()).isNull();
        assertThat(video.getCreatedAt()).isNotNull();
        assertThat(video.getUpdatedAt()).isNotNull();
    }

    @Test
    void withId_retornaCopiaComIdMantendoOsDemaisCampos() {
        Video video = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "pending");
        UUID id = UUID.randomUUID();

        Video withId = video.withId(id);

        assertThat(withId.getId()).isEqualTo(id);
        assertThat(withId.getOwnerEmail()).isEqualTo(video.getOwnerEmail());
        assertThat(withId.getFileName()).isEqualTo(video.getFileName());
        assertThat(video.getId()).isNull(); // imutabilidade
    }

    @Test
    void withVideoKey_retornaCopiaComNovaKeyMantendoOsDemaisCampos() {
        Video video = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "pending").withId(UUID.randomUUID());

        Video withKey = video.withVideoKey("videos/gabriel/real-key.mp4");

        assertThat(withKey.getVideoKey()).isEqualTo("videos/gabriel/real-key.mp4");
        assertThat(withKey.getId()).isEqualTo(video.getId());
        assertThat(withKey.getStatus()).isEqualTo(video.getStatus());
        assertThat(video.getVideoKey()).isEqualTo("pending"); // imutabilidade
    }

    @Test
    void reconstruct_restauraVideoExistenteComTodosOsCampos() {
        UUID id = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(2);
        OffsetDateTime updatedAt = OffsetDateTime.now().minusHours(1);

        Video video = Video.reconstruct(
                id, "gabriel@video2frames.com", "meu-video.mp4", VideoStatus.COMPLETED,
                "videos/gabriel/id.mp4", "videos/gabriel/id-frames.zip", 10, null, createdAt, updatedAt);

        assertThat(video.getId()).isEqualTo(id);
        assertThat(video.getStatus()).isEqualTo(VideoStatus.COMPLETED);
        assertThat(video.getZipKey()).isEqualTo("videos/gabriel/id-frames.zip");
        assertThat(video.getFrameCount()).isEqualTo(10);
        assertThat(video.getCreatedAt()).isEqualTo(createdAt);
        assertThat(video.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void markProcessing_quandoStatusUploaded_transicionaParaProcessing() {
        Video video = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4");

        Video processing = video.markProcessing();

        assertThat(processing.getStatus()).isEqualTo(VideoStatus.PROCESSING);
        assertThat(video.getStatus()).isEqualTo(VideoStatus.UPLOADED); // imutabilidade
    }

    @Test
    void markProcessing_quandoStatusNaoEhUploaded_lancaInvalidVideoStateTransitionException() {
        Video processing = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .markProcessing();

        assertThatThrownBy(processing::markProcessing)
                .isInstanceOf(InvalidVideoStateTransitionException.class);
    }

    @Test
    void markCompleted_quandoStatusProcessing_transicionaParaCompletedComZipKeyEFrameCount() {
        Video processing = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .markProcessing();

        Video completed = processing.markCompleted("videos/gabriel/id-frames.zip", 120);

        assertThat(completed.getStatus()).isEqualTo(VideoStatus.COMPLETED);
        assertThat(completed.getZipKey()).isEqualTo("videos/gabriel/id-frames.zip");
        assertThat(completed.getFrameCount()).isEqualTo(120);
        assertThat(completed.getErrorReason()).isNull();
    }

    @Test
    void markCompleted_quandoStatusNaoEhProcessing_lancaInvalidVideoStateTransitionException() {
        Video uploaded = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4");

        assertThatThrownBy(() -> uploaded.markCompleted("zip-key", 10))
                .isInstanceOf(InvalidVideoStateTransitionException.class);
    }

    @Test
    void markFailed_quandoStatusUploaded_transicionaParaFailedComMotivo() {
        Video uploaded = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4");

        Video failed = uploaded.markFailed("Falha ao decodificar o vídeo");

        assertThat(failed.getStatus()).isEqualTo(VideoStatus.FAILED);
        assertThat(failed.getErrorReason()).isEqualTo("Falha ao decodificar o vídeo");
    }

    @Test
    void markFailed_quandoStatusProcessing_transicionaParaFailedComMotivo() {
        Video processing = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .markProcessing();

        Video failed = processing.markFailed("Erro no worker de processamento");

        assertThat(failed.getStatus()).isEqualTo(VideoStatus.FAILED);
        assertThat(failed.getErrorReason()).isEqualTo("Erro no worker de processamento");
    }

    @Test
    void markFailed_quandoStatusCompleted_lancaInvalidVideoStateTransitionException() {
        Video completed = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .markProcessing()
                .markCompleted("zip-key", 10);

        assertThatThrownBy(() -> completed.markFailed("motivo qualquer"))
                .isInstanceOf(InvalidVideoStateTransitionException.class);
    }

    @Test
    void markFailed_quandoStatusJaEhFailed_lancaInvalidVideoStateTransitionException() {
        Video failed = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .markFailed("primeiro motivo");

        assertThatThrownBy(() -> failed.markFailed("segundo motivo"))
                .isInstanceOf(InvalidVideoStateTransitionException.class);
    }

    @Test
    void isDownloadable_quandoCompletedComZipKey_retornaTrue() {
        Video completed = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .markProcessing()
                .markCompleted("videos/gabriel/id-frames.zip", 10);

        assertThat(completed.isDownloadable()).isTrue();
    }

    @Test
    void isDownloadable_quandoStatusNaoEhCompleted_retornaFalse() {
        Video processing = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .markProcessing();

        assertThat(processing.isDownloadable()).isFalse();
    }

    @Test
    void isDownloadable_quandoCompletedMasSemZipKey_retornaFalse() {
        Video completedSemZip = Video.reconstruct(
                UUID.randomUUID(), "gabriel@video2frames.com", "meu-video.mp4", VideoStatus.COMPLETED,
                "videos/gabriel/id.mp4", null, null, null, OffsetDateTime.now(), OffsetDateTime.now());

        assertThat(completedSemZip.isDownloadable()).isFalse();
    }

    @Test
    void belongsTo_quandoEmailIgualIgnorandoCase_retornaTrue() {
        Video video = Video.upload("Gabriel@Video2Frames.com", "meu-video.mp4", "pending");

        assertThat(video.belongsTo("gabriel@video2frames.com")).isTrue();
    }

    @Test
    void belongsTo_quandoEmailDiferente_retornaFalse() {
        Video video = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "pending");

        assertThat(video.belongsTo("outro@video2frames.com")).isFalse();
    }
}
