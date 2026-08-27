package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.VideoProcessedEvent;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandleVideoProcessedUseCaseTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private VideoStatusHistoryRepository historyRepository;

    private HandleVideoProcessedUseCase useCase;

    private final UUID videoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new HandleVideoProcessedUseCase(videoRepository, historyRepository);
    }

    @Test
    void execute_quandoVideoNaoEncontrado_lancaVideoNotFoundException() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        VideoProcessedEvent event = new VideoProcessedEvent(videoId, "videos/gabriel/id-frames.zip", 42);

        assertThatThrownBy(() -> useCase.execute(event))
                .isInstanceOf(VideoNotFoundException.class);

        verify(videoRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void execute_quandoVideoEmProcessamento_marcaComoCompletoESalvaHistorico() {
        Video processing = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
                .withId(videoId)
                .markProcessing();
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(processing));
        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VideoProcessedEvent event = new VideoProcessedEvent(videoId, "videos/gabriel/id-frames.zip", 42);

        useCase.execute(event);

        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(videoCaptor.capture());
        assertThat(videoCaptor.getValue().getStatus()).isEqualTo(VideoStatus.COMPLETED);
        assertThat(videoCaptor.getValue().getZipKey()).isEqualTo("videos/gabriel/id-frames.zip");
        assertThat(videoCaptor.getValue().getFrameCount()).isEqualTo(42);

        ArgumentCaptor<VideoStatusHistoryEntry> historyCaptor = ArgumentCaptor.forClass(VideoStatusHistoryEntry.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getVideoId()).isEqualTo(videoId);
        assertThat(historyCaptor.getValue().getStatus()).isEqualTo(VideoStatus.COMPLETED);
        assertThat(historyCaptor.getValue().getReason()).isNull();
    }

    @Test
    void execute_quandoVideoNaoEstaEmProcessamento_lancaInvalidVideoStateTransitionException() {
        Video uploaded = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4").withId(videoId);
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(uploaded));

        VideoProcessedEvent event = new VideoProcessedEvent(videoId, "videos/gabriel/id-frames.zip", 42);

        assertThatThrownBy(() -> useCase.execute(event))
                .isInstanceOf(InvalidVideoStateTransitionException.class);

        verify(videoRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }
}
