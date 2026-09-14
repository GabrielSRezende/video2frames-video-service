package br.com.video2frames.video2frames_video_service.infrastructure.messaging;

import br.com.video2frames.video2frames_video_service.application.dto.VideoUploadedEvent;
import br.com.video2frames.video2frames_video_service.application.port.VideoProcessingQueuePort;
import br.com.video2frames.video2frames_video_service.infrastructure.messaging.dto.VideoUploadedMessage;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class SqsVideoProcessingQueuePublisher implements VideoProcessingQueuePort {

    private final SqsClient sqsClient;
    private final SqsQueueUrls queueUrls;
    private final String queueName;
    private final Gson gson = new Gson();

    public SqsVideoProcessingQueuePublisher(
            SqsClient sqsClient,
            SqsQueueUrls queueUrls,
            @Value("${aws.sqs.video-uploaded-queue}") String queueName) {
        this.sqsClient = sqsClient;
        this.queueUrls = queueUrls;
        this.queueName = queueName;
    }

    @Override
    public void publishVideoUploaded(VideoUploadedEvent event) {
        var message = new VideoUploadedMessage(
                event.videoId().toString(), event.ownerEmail(), event.videoKey(), event.fileName());

        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrls.resolve(queueName))
                .messageBody(gson.toJson(message))
                .build());
    }
}
