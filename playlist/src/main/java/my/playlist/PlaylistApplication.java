package my.playlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlaylistApplication {

	public static void main(String[] args) {
		System.setProperty("spring.config.location", "file:/C:/Users/Gnanesh/Myplaylist/Myplaylist/playlist/src/main/java/my/playlist/application.yaml");
		SpringApplication.run(PlaylistApplication.class, args);
	}

}
