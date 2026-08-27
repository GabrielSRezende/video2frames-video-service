package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.VideoResult;
import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUserVideosUseCaseTest {

    @Mock
    private VideoRepository videoRepository;

    private ListUserVideosUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListUserVideosUseCase(videoRepository);
    }

    @Test
    void execute_quandoUsuarioNaoTemVideos_retornaListaVazia() {
        when(videoRepository.findAllByOwnerEmail("gabriel@video2frames.com")).thenReturn(List.of());

        List<VideoResult> result = useCase.execute("gabriel@video2frames.com");

        assertThat(result).isEmpty();
    }

    @Test
    void execute_quandoUsuarioTemVideos_retornaOrdenadosDoMaisRecenteParaOMaisAntigo() {
        Video antigo = Video.reconstruct(
                UUID.randomUUID(), "gabriel@video2frames.com", "video-antigo.mp4", VideoStatus.COMPLETED,
                "key1", "zip1", 5, null,
                OffsetDateTime.now().minusDays(2), OffsetDateTime.now().minusDays(2));
        Video recente = Video.reconstruct(
                UUID.randomUUID(), "gabriel@video2frames.com", "video-recente.mp4", VideoStatus.PROCESSING,
                "key2", null, null, null,
                OffsetDateTime.now(), OffsetDateTime.now());

        when(videoRepository.findAllByOwnerEmail("gabriel@video2frames.com")).thenReturn(List.of(antigo, recente));

        List<VideoResult> result = useCase.execute("gabriel@video2frames.com");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).fileName()).isEqualTo("video-recente.mp4");
        assertThat(result.get(1).fileName()).isEqualTo("video-antigo.mp4");
    }
}
