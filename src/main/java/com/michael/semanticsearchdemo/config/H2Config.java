package com.michael.semanticsearchdemo.config;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class H2Config {
    @Bean
    public JdbcTemplate h2JdbcTemplate() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:rag;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return new JdbcTemplate(ds);
    }

    @Bean
    public CommandLineRunner h2Init(JdbcTemplate h2JdbcTemplate) {
        return args -> {
            h2JdbcTemplate.execute("create table items(id int auto_increment primary key, name varchar(255), info varchar(255))");
            h2JdbcTemplate.update("insert into items(name, info) values('Alpha','First entry')");
            h2JdbcTemplate.update("insert into items(name, info) values('Beta','Second entry')");
        };
    }
}
