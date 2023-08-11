package dev.gmelon.plango.config;

import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager em;

//    @Bean
//    public JpaQueryFactory jpaQueryFactory() {
//        JpaQueryFactory
//    }

}
