package com.survival.Config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        // community DB가 사용할 Repository 패키지 경로 지정
        basePackages = {"com.codingrecipe.board.repository"},
        entityManagerFactoryRef = "communityEntityManagerFactory",
        transactionManagerRef = "communityTransactionManager"
)
public class CommunityDBConfig {

    // 1단계: community DB 설정 정보 읽기
    @Bean(name = "communityProperties")
    @ConfigurationProperties(prefix = "spring.datasource.community")
    public DataSourceProperties communityProperties() {
        return new DataSourceProperties();
    }

    // 2단계: community DB의 DataSource 생성
    @Bean(name = "communityDataSource")
    public DataSource communityDataSource(@Qualifier("communityProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    // community DB의 EntityManagerFactory
    @Bean(name = "communityEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("communityDataSource") DataSource dataSource) {

        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        return builder
                .dataSource(dataSource)
                // community DB가 사용할 Entity 패키지 경로 지정
                .packages("com.codingrecipe.board.domain")
                .persistenceUnit("community")
                .properties(properties)
                .build();
    }

    // community DB의 TransactionManager 생성
    @Bean(name = "communityTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("communityEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}