package com.codingrecipe.board.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

/**
 * 설명 : Redis 설정 클래스입니다.
 */

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.cluster.nodes}")
    private List<String> clusterNodes;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.timeout:5000}")
    private long timeoutMillis;

    @Value("${spring.data.redis.ssl:false}")
    private boolean useSsl;

    @PostConstruct
    public void logRedisProperties() {
        log.info("Redis Cluster Nodes: {}", clusterNodes);
        log.info("Redis Password set: {}", (password.isEmpty() ? "No" : "Yes"));
        log.info("Redis Timeout (ms): {}", timeoutMillis);
        log.info("Redis SSL Enabled: {}", useSsl);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        // 클러스터 노드 설정
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(clusterNodes);
        if (!password.isEmpty()) {
            clusterConfig.setPassword(password);
        }
        clusterConfig.setMaxRedirects(3);

        // Lettuce Client 설정
        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeoutMillis));

        if (useSsl) {
            clientConfigBuilder.useSsl();
        }

        LettuceClientConfiguration clientConfig = clientConfigBuilder.build();

        // LettuceConnectionFactory 생성
        return new LettuceConnectionFactory(clusterConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 키, 값 직렬화 (문자열)
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}

