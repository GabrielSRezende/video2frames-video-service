package br.com.video2frames.video2frames_video_service.infrastructure.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqsQueueUrlsTest {

    @Mock
    private SqsClient sqsClient;

    private SqsQueueUrls queueUrls;

    @BeforeEach
    void setUp() {
        queueUrls = new SqsQueueUrls(sqsClient);
    }

    @Test
    void resolve_consultaOSqsClientEDevolveAUrlDaFila() {
        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("http://localstack/video-uploaded-queue").build());

        String url = queueUrls.resolve("video-uploaded-queue");

        assertThat(url).isEqualTo("http://localstack/video-uploaded-queue");

        ArgumentCaptor<GetQueueUrlRequest> captor = ArgumentCaptor.forClass(GetQueueUrlRequest.class);
        verify(sqsClient).getQueueUrl(captor.capture());
        assertThat(captor.getValue().queueName()).isEqualTo("video-uploaded-queue");
    }

    @Test
    void resolve_quandoChamadoVariasVezesParaAMesmaFila_consultaOSqsClientApenasUmaVez() {
        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("http://localstack/video-uploaded-queue").build());

        queueUrls.resolve("video-uploaded-queue");
        queueUrls.resolve("video-uploaded-queue");
        String url = queueUrls.resolve("video-uploaded-queue");

        assertThat(url).isEqualTo("http://localstack/video-uploaded-queue");
        verify(sqsClient, times(1)).getQueueUrl(any(GetQueueUrlRequest.class));
    }

    @Test
    void resolve_paraFilasDiferentes_consultaOSqsClientSeparadamente() {
        when(sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("fila-a").build()))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("http://localstack/fila-a").build());
        when(sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("fila-b").build()))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("http://localstack/fila-b").build());

        assertThat(queueUrls.resolve("fila-a")).isEqualTo("http://localstack/fila-a");
        assertThat(queueUrls.resolve("fila-b")).isEqualTo("http://localstack/fila-b");

        verify(sqsClient, times(2)).getQueueUrl(any(GetQueueUrlRequest.class));
    }
}
