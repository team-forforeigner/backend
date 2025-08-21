package com.survival.Config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
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
        basePackages = {"com.codingrecipe.board.repository"},
        entityManagerFactoryRef = "communityEntityManagerFactory",
        transactionManagerRef = "communityTransactionManager"
)
public class CommunityDBConfig {

    @Bean(name = "communityProperties")
    @ConfigurationProperties(prefix = "spring.datasource.community")
    public DataSourceProperties communityProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "communityDataSource")
    public DataSource communityDataSource(@Qualifier("communityProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "communityEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("communityDataSource") DataSource dataSource) {

        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        return builder
                .dataSource(dataSource)
                .packages("com.codingrecipe.board.domain")
                .persistenceUnit("community")
                .properties(properties)
                .build();
    }

    @Bean(name = "communityTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("communityEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }

    @Bean
    public JdbcTemplate communityJdbcTemplate(@Qualifier("communityDataSource") DataSource communityDataSource) {
        return new JdbcTemplate(communityDataSource);
    }

    @Bean
    public InitializingBean communityDataInitializer(@Qualifier("communityJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return () -> {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("data-community.sql"));
            populator.execute(jdbcTemplate.getDataSource());
        };
    }
}