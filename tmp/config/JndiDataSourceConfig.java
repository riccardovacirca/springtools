package dev.myapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqlite.SQLiteDataSource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Crea un DataSource SQLite e lo registra in JNDI come:
 *   java:comp/env/jdbc/hello
 *
 * Compatibile con la classe DB("jdbc/hello")
 */
@Configuration
public class JndiDataSourceConfig {

    private static final String JNDI_NAME = "java:comp/env/jdbc/hello";

    @Bean
    public DataSource dataSource() throws NamingException {
        // 1. Crea DataSource SQLite
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:data/hello.db");

        // 2. Registra in JNDI
        InitialContext ctx = new InitialContext();
        try {
            // Crea contesto java:comp/env se non esiste
            ctx.createSubcontext("java:comp");
        } catch (NamingException ignored) {}

        try {
            ctx.createSubcontext("java:comp/env");
        } catch (NamingException ignored) {}

        ctx.bind(JNDI_NAME, ds);
        ctx.close();

        return ds;
    }
}
