package com.db.flare.flareifti.configuration;

import com.google.common.collect.ImmutableMap;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.support.ExitCodeMapper;
import org.springframework.batch.core.launch.support.SimpleJvmExitCodeMapper;
import org.springframework.batch.core.listener.CompositeJobExecutionListener;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@Lazy
public class JobConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "flare.base.data-source", ignoreUnknownFields = false)
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public ExitCodeMapper exitCodeMapper() {
        final SimpleJvmExitCodeMapper exitCodeMapper = new SimpleJvmExitCodeMapper();
        exitCodeMapper.setMapping(ImmutableMap.of("NONE_PROCESSED", 3));
        return exitCodeMapper;
    }

}
