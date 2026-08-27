package br.com.video2frames.video2frames_video_service.infrastructure.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3VideoStorageTest {

    @Mock
    private S3Client s3Client;

    private S3VideoStorage storage;

    @BeforeEach
    void setUp() {
        storage = new S3VideoStorage(s3Client, "video2frames-bucket");
    }

    @Test
    void storeOriginalVideo_enviaOArquivoParaOS3EDevolveAKeyGerada() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        UUID videoId = UUID.randomUUID();
        InputStream content = new ByteArrayInputStream("conteudo-fake".getBytes());

        String key = storage.storeOriginalVideo(
                videoId, "gabriel@video2frames.com", "meu-video.mp4", content, 100L, "video/mp4");

        assertThat(key).isEqualTo("videos/gabriel@video2frames.com/" + videoId + ".mp4");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertThat(captor.getValue().bucket()).isEqualTo("video2frames-bucket");
        assertThat(captor.getValue().key()).isEqualTo(key);
        assertThat(captor.getValue().contentType()).isEqualTo("video/mp4");
    }

    @Test
    void storeOriginalVideo_quandoNomeDoArquivoNaoTemExtensao_geraKeySemExtensao() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        UUID videoId = UUID.randomUUID();

        String key = storage.storeOriginalVideo(
                videoId, "gabriel@video2frames.com", "semextensao", new ByteArrayInputStream(new byte[0]), 0L, "video/mp4");

        assertThat(key).isEqualTo("videos/gabriel@video2frames.com/" + videoId);
    }

    @Test
    void fetchZip_buscaOObjetoNoBucketConfiguradoComAKeyInformada() throws IOException {
        ResponseInputStream<GetObjectResponse> response = new ResponseInputStream<>(
                GetObjectResponse.builder().build(), new ByteArrayInputStream("zip-bytes".getBytes()));
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(response);

        InputStream result = storage.fetchZip("videos/gabriel/id-frames.zip");

        assertThat(result.readAllBytes()).isEqualTo("zip-bytes".getBytes());

        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("video2frames-bucket");
        assertThat(captor.getValue().key()).isEqualTo("videos/gabriel/id-frames.zip");
    }
}
