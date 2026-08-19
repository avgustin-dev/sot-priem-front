package kg.sot.reception.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Явный бин SpringLiquibase — не полагаемся на автоконфигурацию Boot, которая в этом
 * окружении почему-то не подхватывает миграции сама (аналогично ObjectMapper).
 */
@Configuration
public class LiquibaseConfig {

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.yaml");
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
