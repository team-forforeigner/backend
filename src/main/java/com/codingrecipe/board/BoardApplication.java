package com.codingrecipe.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
// 통합 위한 import
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@ComponentScan(basePackages = {"com.condingrecipe.board" , "com.survival"})
@EntityScan(basePackages = {"com.condingrecipe.board.Entity" , "com.survival.Entity"})
@EnableJpaRepositories(basePackages = {"com.codingrecipe.board.repository" , "com.survival.repository"})
public class BoardApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoardApplication.class, args);
	}

}