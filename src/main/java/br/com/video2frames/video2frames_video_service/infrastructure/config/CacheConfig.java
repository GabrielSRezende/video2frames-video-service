package br.com.video2frames.video2frames_video_service.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Cache Redis para a listagem de vídeos por usuário ("userVideos"), a única
 * leitura repetida do fluxo (o usuário consulta status com frequência
 * enquanto o processamento roda em background). O TTL de 5 minutos
 * (application.yml) já limita o pior caso de dado desatualizado, mas cada
 * use case que muda o estado de um vídeo (upload, sucesso, falha) também
 * invalida a entrada do usuário correspondente explicitamente — ver
 * UploadVideoUseCase, HandleVideoProcessedUseCase e HandleVideoFailedUseCase.
 *
 * Valores serializados via JDK padrão (VideoResult implementa Serializable)
 * em vez de JSON: a serialização polimórfica de coleções genéricas
 * (List<VideoResult>) via Jackson 3 (tools.jackson) na versão atual do
 * spring-data-redis se mostrou instável (tipo perdido/incompatível entre
 * escrita e leitura). JDK serialization não depende de metadados de tipo
 * reconstruídos — mais simples e confiável para este caso.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration(
            @Value("${spring.cache.redis.time-to-live:5m}") Duration timeToLive) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(timeToLive)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
    }
}
