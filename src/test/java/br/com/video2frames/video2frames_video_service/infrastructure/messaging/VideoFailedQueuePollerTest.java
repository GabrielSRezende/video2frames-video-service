package br.com.video2frames.video2frames_video_service.infrastructure.messaging;

import br.com.video2frames.video2frames_video_service.application.dto.VideoFailedEvent;
import br.com.video2frames.video2frames_video_service.application.usecase.HandleVideoFailedUseCase;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoFailedQueuePollerTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private HandleVideoFailedUseCase handleVideoFailedUseCase;

    private SqsQueueUrls queueUrls;

    private VideoFailedQueuePoller poller;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        queueUrls = new SqsQueueUrls(sqsClient);
        poller = new VideoFailedQueuePoller(sqsClient, queueUrls, handleVideoFailedUseCase, "video-failed-queue", 5);

        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("http://localstack/video-failed-queue").build());
    }

    @Test
    void poll_semMensagensNaFila_naoProcessaNadaENaoDeleta() {
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(java.util.List.of()).build());

        poller.poll();

        verify(handleVideoFailedUseCase, never()).execute(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void poll_comMensagemValida_processaAEventoEDeletaAMensagem() {
        UUID videoId = UUID.randomUUID();
        String body = gson.toJson(new br.com.video2frames.video2frames_video_service.infrastructure.messaging.dto.VideoFailedMessage(
                videoId.toString(), "worker travou"));
        Message message = Message.builder().body(body).receiptHandle("receipt-1").build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());

        poller.poll();

        verify(handleVideoFailedUseCase).execute(new VideoFailedEvent(videoId, "worker travou"));

        var captor = org.mockito.ArgumentCaptor.forClass(DeleteMessageRequest.class);
        verify(sqsClient).deleteMessage(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().receiptHandle()).isEqualTo("receipt-1");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().queueUrl()).isEqualTo("http://localstack/video-failed-queue");
    }

    @Test
    void poll_quandoUseCaseLancaExcecao_naoDeletaAMensagemParaPermitirNovaTentativa() {
        UUID videoId = UUID.randomUUID();
        String body = gson.toJson(new br.com.video2frames.video2frames_video_service.infrastructure.messaging.dto.VideoFailedMessage(
                videoId.toString(), "worker travou"));
        Message message = Message.builder().body(body).receiptHandle("receipt-1").build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());
        doThrow(new RuntimeException("falha ao processar")).when(handleVideoFailedUseCase).execute(any());

        poller.poll();

        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void poll_comMensagemComJsonInvalido_naoDeletaAMensagemENaoChamaOUseCase() {
        Message message = Message.builder().body("{ json-invalido").receiptHandle("receipt-2").build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());

        poller.poll();

        verify(handleVideoFailedUseCase, never()).execute(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }
}
