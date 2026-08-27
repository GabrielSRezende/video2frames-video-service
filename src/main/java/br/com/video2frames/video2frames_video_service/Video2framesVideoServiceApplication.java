package br.com.video2frames.video2frames_video_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class Video2framesVideoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(Video2framesVideoServiceApplication.class, args);
	}

}
