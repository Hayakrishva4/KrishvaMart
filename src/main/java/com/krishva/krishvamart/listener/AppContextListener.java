package com.krishva.krishvamart.listener;

import com.krishva.krishvamart.util.ConfigResolver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns the single HikariCP {@link DataSource} for the application lifetime.
 * Section 2, Rule 5: no DriverManager.getConnection() calls anywhere outside
 * this listener. DAOs pull connections exclusively from the DataSource this
 * class stores in the ServletContext.
 *
 * Configuration resolves environment variables first (see
 * {@link ConfigResolver}), so the same WAR can be deployed unmodified to a
 * cloud platform (Render, Railway, Fly.io, AWS, etc.) by setting DB_URL /
 * DB_USER / DB_PASSWORD as environment variables there - no rebuild, no
 * secrets committed to config.properties.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(AppContextListener.class);
    public static final String DATASOURCE_ATTR = "krishvamart.datasource";

    private HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ConfigResolver config = ConfigResolver.load();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.get("db.url", "jdbc:h2:tcp://localhost:9092/./data/krishvamart"));
        hikariConfig.setUsername(config.get("db.user", "sa"));
        hikariConfig.setPassword(config.get("db.password", ""));
        hikariConfig.setDriverClassName("org.h2.Driver");
        hikariConfig.setMaximumPoolSize(config.getInt("db.pool.max.size", 10));
        hikariConfig.setMinimumIdle(config.getInt("db.pool.min.idle", 2));
        hikariConfig.setPoolName("KrishvaMartPool");

        this.dataSource = new HikariDataSource(hikariConfig);
        sce.getServletContext().setAttribute(DATASOURCE_ATTR, dataSource);
        SchemaInitializer.run(dataSource, config);
        sce.getServletContext().setAttribute(ServiceRegistry.ATTR, new ServiceRegistry(dataSource, config));
        LOG.info("HikariCP pool initialized for {}", hikariConfig.getJdbcUrl());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOG.info("HikariCP pool closed");
        }
    }
}
