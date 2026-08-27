package br.com.video2frames.video2frames_video_service.infrastructure.messaging;

import br.com.video2frames.video2frames_video_service.application.dto.VideoProcessedEvent;
import br.com.video2frames.video2frames_video_service.application.usecase.HandleVideoProcessedUseCase;
import br.com.video2frames.video2frames_video_service.infrastructure.messaging.dto.VideoProcessedMessage;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoProcessedQueuePollerTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private HandleVideoProcessedUseCase handleVideoProcessedUseCase;

    private SqsQueueUrls queueUrls;

    private VideoProcessedQueuePoller poller;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        queueUrls = new SqsQueueUrls(sqsClient);
        poller = new VideoProcessedQueuePoller(sqsClient, queueUrls, handleVideoProcessedUseCase, "video-processed-queue", 5);

        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("http://localstack/video-processed-queue").build());
    }

    @Test
    void poll_semMensagensNaFila_naoProcessaNadaENaoDeleta() {
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(List.of()).build());

        poller.poll();

        verify(handleVideoProcessedUseCase, never()).execute(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void poll_comMensagemValida_processaAEventoEDeletaAMensagem() {
        UUID videoId = UUID.randomUUID();
        String body = gson.toJson(new VideoProcessedMessage(videoId.toString(), "videos/gabriel/id-frames.zip", 42));
        Message message = Message.builder().body(body).receiptHandle("receipt-1").build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());

        poller.poll();

        verify(handleVideoProcessedUseCase)
                .execute(new VideoProcessedEvent(videoId, "videos/gabriel/id-frames.zip", 42));

        ArgumentCaptor<DeleteMessageRequest> captor = ArgumentCaptor.forClass(DeleteMessageRequest.class);
        verify(sqsClient).deleteMessage(captor.capture());
        assertThat(captor.getValue().receiptHandle()).isEqualTo("receipt-1");
        assertThat(captor.getValue().queueUrl()).isEqualTo("http://localstack/video-processed-queue");
    }

    @Test
    void poll_quandoUseCaseLancaExcecao_naoDeletaAMensagemParaPermitirNovaTentativa() {
        UUID videoId = UUID.randomUUID();
        String body = gson.toJson(new VideoProcessedMessage(videoId.toString(), "zip-key", 10));
        Message message = Message.builder().body(body).receiptHandle("receipt-1").build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());
        doThrow(new RuntimeException("falha ao processar")).when(handleVideoProcessedUseCase).execute(any());

        poller.poll();

        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void poll_comVariasMensagens_processaEDeletaCadaUmaDelas() {
        UUID videoId1 = UUID.randomUUID();
        UUID videoId2 = UUID.randomUUID();
        Message message1 = Message.builder()
                .body(gson.toJson(new VideoProcessedMessage(videoId1.toString(), "zip1", 1)))
                .receiptHandle("r1").build();
        Message message2 = Message.builder()
                .body(gson.toJson(new VideoProcessedMessage(videoId2.toString(), "zip2", 2)))
                .receiptHandle("r2").build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message1, message2).build());

        poller.poll();

        verify(handleVideoProcessedUseCase).execute(new VideoProcessedEvent(videoId1, "zip1", 1));
        verify(handleVideoProcessedUseCase).execute(new VideoProcessedEvent(videoId2, "zip2", 2));
        verify(sqsClient, org.mockito.Mockito.times(2)).deleteMessage(any(DeleteMessageRequest.class));
    }
}
