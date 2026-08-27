package br.com.video2frames.video2frames_video_service.infrastructure.messaging;

import br.com.video2frames.video2frames_video_service.application.dto.VideoUploadedEvent;
import br.com.video2frames.video2frames_video_service.infrastructure.messaging.dto.VideoUploadedMessage;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqsVideoProcessingQueuePublisherTest {

    @Mock
    private SqsClient sqsClient;

    private SqsQueueUrls queueUrls;

    private SqsVideoProcessingQueuePublisher publisher;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        queueUrls = new SqsQueueUrls(sqsClient);
        publisher = new SqsVideoProcessingQueuePublisher(sqsClient, queueUrls, "video-uploaded-queue");
    }

    @Test
    void publishVideoUploaded_enviaMensagemJsonParaAFilaResolvida() {
        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("http://localstack/video-uploaded-queue").build());

        UUID videoId = UUID.randomUUID();
        VideoUploadedEvent event = new VideoUploadedEvent(videoId, "gabriel@video2frames.com", "videos/gabriel/id.mp4");

        publisher.publishVideoUploaded(event);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest sent = captor.getValue();
        assertThat(sent.queueUrl()).isEqualTo("http://localstack/video-uploaded-queue");

        VideoUploadedMessage message = gson.fromJson(sent.messageBody(), VideoUploadedMessage.class);
        assertThat(message.videoId()).isEqualTo(videoId.toString());
        assertThat(message.ownerEmail()).isEqualTo("gabriel@video2frames.com");
        assertThat(message.videoKey()).isEqualTo("videos/gabriel/id.mp4");
    }
}
