package com.codingrecipe.board.Config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

// DB 분리를 위한 config -- 커뮤니티 Entity들이 community_db에 생성되도록 도움

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "communityEntityManagerFactory",
        transactionManagerRef = "communityTransactionManager",
        basePackages = {"com.codingrecipe.board.repository"}
)
public class CommunityDBConfig {

    @Primary
    @Bean(name = "communityDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.community")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "communityEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("communityDataSource") DataSource dataSource) {

        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");

        return builder
                .dataSource(dataSource)
                .packages("com.AB.C.domain")
                .persistenceUnit("community")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "communityTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("communityEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}