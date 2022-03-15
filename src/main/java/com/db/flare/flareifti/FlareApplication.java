package com.db.flare.flareifti;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableBatchProcessing
public class FlareApplication {

	public static void main(String[] args) {
		SpringApplication application = (new SpringApplicationBuilder(new Class[0])).sources(new Class[]{FlareApplication.class}).web(WebApplicationType.NONE).build();
		ConfigurableApplicationContext applicationContext = application.run(args);
		int exitCode = SpringApplication.exit(applicationContext, new ExitCodeGenerator[0]);
		System.exit(exitCode);
	}

}
