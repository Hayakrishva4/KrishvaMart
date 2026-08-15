package com.krishva.krishvamart.listener;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 @WebListener
 public class AppContextListener implements ServletContextListener 
 {
   private static final Logger LOG = LoggerFactory.getLogger(AppContextListener.class);
   public static final String DATASOURCE_ATTR = "krishvamart.datasource";
   private HikariDataSource dataSource;
    @Override
    public void contextInitialized(ServletContextEvent sce) 
    {
     Properties props = loadConfig();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url", "jdbc:h2:tcp://localhost:9092/./data/krishvamart"));
        config.setUsername(props.getProperty("db.user", "sa"));
        config.setPassword(props.getProperty("db.password", ""));
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maxSize", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "2")));
        config.setPoolName("KrishvaMartPool");
        this.dataSource = new HikariDataSource(config);
        sce.getServletContext().setAttribute(DATASOURCE_ATTR, dataSource);
        sce.getServletContext().setAttribute(ServiceRegistry.ATTR, new ServiceRegistry(dataSource));
        LOG.info("HikariCP pool initialized for {}", config.getJdbcUrl());
    }
    @Override
    public void contextDestroyed(ServletContextEvent sce) 
     {
      if (dataSource != null && !dataSource.isClosed()) 
       {
         dataSource.close();
          LOG.info("HikariCP pool closed");
       }
     }
    private Properties loadConfig() 
     {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) 
         {
          if (in != null) 
          { 
           props.load(in);
          } 
          else
           {
             LOG.warn("config.properties not found on classpath - using default local H2 settings");
           }
         }
         catch (IOException e)
          {
            LOG.error("Failed to read config.properties, using defaults", e);
          }
        return props;
     }
 }