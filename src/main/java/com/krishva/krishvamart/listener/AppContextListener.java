package com.krishva.krishvamart.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krishva.krishvamart.util.ConfigResolver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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
