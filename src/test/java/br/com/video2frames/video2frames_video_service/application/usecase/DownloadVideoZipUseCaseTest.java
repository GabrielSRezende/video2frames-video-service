package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.VideoDownload;
import br.com.video2frames.video2frames_video_service.application.port.VideoStoragePort;
import br.com.video2frames.video2frames_video_service.domain.exception.InvalidVideoStateTransitionException;
import br.com.video2frames.video2frames_video_service.domain.exception.VideoAccessDeniedException;
import br.com.video2frames.video2frames_video_service.domain.exception.VideoNotFoundException;
import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadVideoZipUseCaseTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private VideoStoragePort videoStoragePort;

    private DownloadVideoZipUseCase useCase;

    private final UUID videoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new DownloadVideoZipUseCase(videoRepository, videoStoragePort);
    }

    @Test
    void execute_quandoVideoNaoEncontrado_lancaVideoNotFoundException() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("gabriel@video2frames.com", videoId))
                .isInstanceOf(VideoNotFoundException.class);

        verify(videoStoragePort, never()).fetchZip(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void execute_quandoVideoNaoPertenceAoSolicitante_lancaVideoAccessDeniedException() {
        Video video = Video.upload("outro@video2frames.com", "meu-video.mp4", "videos/outro/id.mp4").withId(videoId);
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));

        assertThatThrownBy(() -> useCase.execute("gabriel@video2frames.com", videoId))
                .isInstanceOf(VideoAccessDeniedException.class);

        verify(videoStoragePort, never()).fetchZip(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void execute_quandoVideoAindaNaoEstaCompleto_lancaInvalidVideoStateTransitionException() {
        Video video = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .withId(videoId)
                .markProcessing();
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));

        assertThatThrownBy(() -> useCase.execute("gabriel@video2frames.com", videoId))
                .isInstanceOf(InvalidVideoStateTransitionException.class);

        verify(videoStoragePort, never()).fetchZip(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void execute_quandoVideoCompleto_retornaDownloadComZipDoStorageENomeSugerido() {
        Video video = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .withId(videoId)
                .markProcessing()
                .markCompleted("videos/gabriel/id-frames.zip", 30);
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));

        InputStream zipStream = new ByteArrayInputStream("zip-bytes".getBytes());
        when(videoStoragePort.fetchZip("videos/gabriel/id-frames.zip")).thenReturn(zipStream);

        VideoDownload download = useCase.execute("gabriel@video2frames.com", videoId);

        assertThat(download.content()).isSameAs(zipStream);
        assertThat(download.suggestedFileName()).isEqualTo("meu-video-frames.zip");
    }

    @Test
    void execute_quandoNomeDoArquivoNaoTemExtensao_geraNomeSugeridoAdicionandoSufixo() {
        Video video = Video.upload("gabriel@video2frames.com", "meuvideo", "videos/gabriel/id")
                .withId(videoId)
                .markProcessing()
                .markCompleted("videos/gabriel/id-frames.zip", 5);
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(videoStoragePort.fetchZip("videos/gabriel/id-frames.zip"))
                .thenReturn(new ByteArrayInputStream(new byte[0]));

        VideoDownload download = useCase.execute("gabriel@video2frames.com", videoId);

        assertThat(download.suggestedFileName()).isEqualTo("meuvideo-frames.zip");
    }

    @Test
    void execute_quandoEmailDoSolicitanteDifereApenasNoCase_permiteODownload() {
        Video video = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .withId(videoId)
                .markProcessing()
                .markCompleted("videos/gabriel/id-frames.zip", 8);
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(videoStoragePort.fetchZip("videos/gabriel/id-frames.zip"))
                .thenReturn(new ByteArrayInputStream(new byte[0]));

        VideoDownload download = useCase.execute("GABRIEL@VIDEO2FRAMES.COM", videoId);

        assertThat(download.suggestedFileName()).isEqualTo("meu-video-frames.zip");
    }
}
