package com.survival.Config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
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

// DB 분리를 위한 config -- 서바이벌 Entity들이 survivial_db에 생성되도록 도움

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "survivalEntityManagerFactory",
        transactionManagerRef = "survivalTransactionManager",
        basePackages = {"com.survival.repository"}
)

public class SurvivalDBConfig {

    // survival db 정보로 datasource 생성
    @Bean(name = "survivalDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.survival")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    // survival 엔티티 매니저 생성
    @Bean(name = "survivalEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("survivalDataSource") DataSource dataSource) {

        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");

        return builder
                .dataSource(dataSource)
                .packages("com.survival.Entity")
                .persistenceUnit("survival")
                .properties(properties)
                .build();
    }

    // survival db 트랜잭션 관리자 생성
    @Bean(name = "survivalTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("survivalEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}