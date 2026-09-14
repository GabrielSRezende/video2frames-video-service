package br.com.video2frames.video2frames_video_service.infrastructure.messaging;

import br.com.video2frames.video2frames_video_service.application.dto.VideoProcessedEvent;
import br.com.video2frames.video2frames_video_service.application.usecase.HandleVideoProcessedUseCase;
import br.com.video2frames.video2frames_video_service.infrastructure.messaging.dto.VideoProcessedMessage;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.UUID;

@Component
public class VideoProcessedQueuePoller {

    private static final Logger log = LoggerFactory.getLogger(VideoProcessedQueuePoller.class);

    private final SqsClient sqsClient;
    private final SqsQueueUrls queueUrls;
    private final HandleVideoProcessedUseCase handleVideoProcessedUseCase;
    private final String queueName;
    private final int waitTimeSeconds;
    private final Gson gson = new Gson();

    public VideoProcessedQueuePoller(
            SqsClient sqsClient,
            SqsQueueUrls queueUrls,
            HandleVideoProcessedUseCase handleVideoProcessedUseCase,
            @Value("${aws.sqs.video-processed-queue}") String queueName,
            @Value("${aws.sqs.poll-wait-time-seconds}") int waitTimeSeconds) {
        this.sqsClient = sqsClient;
        this.queueUrls = queueUrls;
        this.handleVideoProcessedUseCase = handleVideoProcessedUseCase;
        this.queueName = queueName;
        this.waitTimeSeconds = waitTimeSeconds;
    }

    @Scheduled(fixedDelay = 1000)
    public void poll() {
        String queueUrl = queueUrls.resolve(queueName);

        var response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(waitTimeSeconds)
                .build());

        for (Message message : response.messages()) {
            try {
                var payload = gson.fromJson(message.body(), VideoProcessedMessage.class);
                handleVideoProcessedUseCase.execute(new VideoProcessedEvent(
                        UUID.fromString(payload.videoId()), payload.zipKey(), payload.frameCount()));

                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
            } catch (Exception e) {
                log.error("Falha ao processar mensagem de video-processed, deixando para nova tentativa", e);
            }
        }
    }
}
