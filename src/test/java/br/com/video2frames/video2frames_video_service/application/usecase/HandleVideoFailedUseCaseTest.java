package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.VideoFailedEvent;
import br.com.video2frames.video2frames_video_service.application.port.VideoStoragePort;
import br.com.video2frames.video2frames_video_service.domain.exception.InvalidVideoStateTransitionException;
import br.com.video2frames.video2frames_video_service.domain.exception.VideoNotFoundException;
import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatusHistoryEntry;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoRepository;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandleVideoFailedUseCaseTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private VideoStatusHistoryRepository historyRepository;

    @Mock
    private VideoStoragePort videoStoragePort;

    @Mock
    private CacheManager cacheManager;

    private HandleVideoFailedUseCase useCase;

    private final UUID videoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new HandleVideoFailedUseCase(videoRepository, historyRepository, videoStoragePort, cacheManager);
    }

    @Test
    void execute_quandoVideoNaoEncontrado_lancaVideoNotFoundException() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        VideoFailedEvent event = new VideoFailedEvent(videoId, "worker travou");

        assertThatThrownBy(() -> useCase.execute(event))
                .isInstanceOf(VideoNotFoundException.class);

        verify(videoRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void execute_quandoVideoEncontrado_marcaComoFalhoESalvaHistoricoComOMotivo() {
        Video video = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4").withId(videoId);
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VideoFailedEvent event = new VideoFailedEvent(videoId, "codec não suportado pelo worker");

        useCase.execute(event);

        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(videoCaptor.capture());
        assertThat(videoCaptor.getValue().getStatus()).isEqualTo(VideoStatus.FAILED);
        assertThat(videoCaptor.getValue().getErrorReason()).isEqualTo("codec não suportado pelo worker");

        ArgumentCaptor<VideoStatusHistoryEntry> historyCaptor = ArgumentCaptor.forClass(VideoStatusHistoryEntry.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getVideoId()).isEqualTo(videoId);
        assertThat(historyCaptor.getValue().getStatus()).isEqualTo(VideoStatus.FAILED);
        assertThat(historyCaptor.getValue().getReason()).isEqualTo("codec não suportado pelo worker");

        verify(videoStoragePort).deleteOriginalVideo("videos/gabriel/id.mp4");
    }

    @Test
    void execute_quandoCompensacaoNoS3Falha_naoImpedeAAtualizacaoDeStatus() {
        Video video = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4").withId(videoId);
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito.doThrow(new RuntimeException("S3 indisponível"))
                .when(videoStoragePort).deleteOriginalVideo(any());

        VideoFailedEvent event = new VideoFailedEvent(videoId, "codec não suportado pelo worker");

        useCase.execute(event);

        verify(videoRepository).save(any(Video.class));
        verify(historyRepository).save(any(VideoStatusHistoryEntry.class));
    }

    @Test
    void execute_quandoVideoJaEstaEmEstadoTerminal_lancaInvalidVideoStateTransitionException() {
        Video completed = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .withId(videoId)
                .markProcessing()
                .markCompleted("zip-key", 10);
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(completed));

        VideoFailedEvent event = new VideoFailedEvent(videoId, "motivo tardio");

        assertThatThrownBy(() -> useCase.execute(event))
                .isInstanceOf(InvalidVideoStateTransitionException.class);

        verify(videoRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }
}
