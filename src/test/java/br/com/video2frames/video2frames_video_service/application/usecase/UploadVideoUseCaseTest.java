package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.UploadVideoCommand;
import br.com.video2frames.video2frames_video_service.application.dto.VideoResult;
import br.com.video2frames.video2frames_video_service.application.dto.VideoUploadedEvent;
import br.com.video2frames.video2frames_video_service.application.port.VideoProcessingQueuePort;
import br.com.video2frames.video2frames_video_service.application.port.VideoStoragePort;
import br.com.video2frames.video2frames_video_service.domain.exception.UnsupportedVideoFormatException;
import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatusHistoryEntry;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoRepository;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadVideoUseCaseTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private VideoStatusHistoryRepository historyRepository;

    @Mock
    private VideoStoragePort videoStoragePort;

    @Mock
    private VideoProcessingQueuePort videoProcessingQueuePort;

    @Mock
    private CacheManager cacheManager;

    private UploadVideoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UploadVideoUseCase(
                videoRepository, historyRepository, videoStoragePort, videoProcessingQueuePort, cacheManager);
    }

    @Test
    void execute_quandoContentTypeNaoEhVideo_lancaUnsupportedVideoFormatException() {
        UploadVideoCommand command = new UploadVideoCommand(
                "gabriel@video2frames.com", "documento.pdf", "application/pdf",
                new ByteArrayInputStream(new byte[0]), 0L);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnsupportedVideoFormatException.class);

        verifyNoInteractions(videoRepository, historyRepository, videoStoragePort, videoProcessingQueuePort);
    }

    @Test
    void execute_quandoContentTypeNulo_lancaUnsupportedVideoFormatException() {
        UploadVideoCommand command = new UploadVideoCommand(
                "gabriel@video2frames.com", "meu-video.mp4", null,
                new ByteArrayInputStream(new byte[0]), 0L);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnsupportedVideoFormatException.class);

        verifyNoInteractions(videoRepository, historyRepository, videoStoragePort, videoProcessingQueuePort);
    }

    @Test
    void execute_quandoComandoValido_persisteEnviaParaStorageEPublicaEventoRetornandoResultadoProcessing() {
        UUID generatedId = UUID.randomUUID();
        InputStream content = new ByteArrayInputStream("conteudo-fake".getBytes());
        UploadVideoCommand command = new UploadVideoCommand(
                "gabriel@video2frames.com", "meu-video.mp4", "video/mp4", content, 100L);

        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video video = invocation.getArgument(0);
            return video.getId() == null ? video.withId(generatedId) : video;
        });
        when(videoStoragePort.storeOriginalVideo(
                eq(generatedId), eq("gabriel@video2frames.com"), eq("meu-video.mp4"), eq(content), eq(100L), eq("video/mp4")))
                .thenReturn("videos/gabriel/" + generatedId + ".mp4");

        VideoResult result = useCase.execute(command);

        assertThat(result.id()).isEqualTo(generatedId);
        assertThat(result.fileName()).isEqualTo("meu-video.mp4");
        assertThat(result.status()).isEqualTo(VideoStatus.PROCESSING);

        verify(videoRepository, times(3)).save(any(Video.class));

        ArgumentCaptor<VideoUploadedEvent> eventCaptor = ArgumentCaptor.forClass(VideoUploadedEvent.class);
        verify(videoProcessingQueuePort).publishVideoUploaded(eventCaptor.capture());
        assertThat(eventCaptor.getValue().videoId()).isEqualTo(generatedId);
        assertThat(eventCaptor.getValue().ownerEmail()).isEqualTo("gabriel@video2frames.com");
        assertThat(eventCaptor.getValue().videoKey()).isEqualTo("videos/gabriel/" + generatedId + ".mp4");

        ArgumentCaptor<VideoStatusHistoryEntry> historyCaptor = ArgumentCaptor.forClass(VideoStatusHistoryEntry.class);
        verify(historyRepository, times(2)).save(historyCaptor.capture());
        List<VideoStatusHistoryEntry> historyEntries = historyCaptor.getAllValues();
        assertThat(historyEntries.get(0).getStatus()).isEqualTo(VideoStatus.UPLOADED);
        assertThat(historyEntries.get(1).getStatus()).isEqualTo(VideoStatus.PROCESSING);
    }

    @Test
    void execute_quandoComandoValido_armazenaNoStorageAntesDePublicarNaFilaEDeMarcarProcessing() {
        UUID generatedId = UUID.randomUUID();
        UploadVideoCommand command = new UploadVideoCommand(
                "gabriel@video2frames.com", "meu-video.mov", "video/quicktime",
                new ByteArrayInputStream(new byte[0]), 0L);

        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video video = invocation.getArgument(0);
            return video.getId() == null ? video.withId(generatedId) : video;
        });
        when(videoStoragePort.storeOriginalVideo(any(), any(), any(), any(), anyLong(), any()))
                .thenReturn("videos/gabriel/key.mov");

        useCase.execute(command);

        InOrder order = inOrder(videoStoragePort, videoProcessingQueuePort, videoRepository);
        order.verify(videoStoragePort).storeOriginalVideo(any(), any(), any(), any(), anyLong(), any());
        order.verify(videoProcessingQueuePort).publishVideoUploaded(any());
        order.verify(videoRepository).save(argThat(video -> video.getStatus() == VideoStatus.PROCESSING));
    }
}
