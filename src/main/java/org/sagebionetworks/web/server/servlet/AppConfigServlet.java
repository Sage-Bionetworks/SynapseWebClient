package org.sagebionetworks.web.server.servlet;

import com.amazonaws.services.appconfigdata.AWSAppConfigData;
import com.amazonaws.services.appconfigdata.model.GetLatestConfigurationRequest;
import com.amazonaws.services.appconfigdata.model.GetLatestConfigurationResult;
import com.amazonaws.services.appconfigdata.model.StartConfigurationSessionRequest;
import com.amazonaws.services.appconfigdata.model.StartConfigurationSessionResult;
import com.google.gwt.thirdparty.guava.common.base.Supplier;
import com.google.gwt.thirdparty.guava.common.base.Suppliers;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.aws.AwsClientFactory;

public class AppConfigServlet extends HttpServlet {

  public AWSAppConfigData appConfigDataClient;
  public Supplier<String> configSupplier;
  public String configurationToken;
  private StackConfiguration stackConfiguration;
  private final String defaultConfigValue =
    "{\"default configuration\":\"true\"}";

  private static final Logger logger = Logger.getLogger(
    AppConfigServlet.class.getName()
  );

  @Inject
  public AppConfigServlet(
    AWSAppConfigData appConfigDataClient,
    StackConfiguration stackConfiguration
  ) {
    this.appConfigDataClient = appConfigDataClient;
    this.stackConfiguration = stackConfiguration;
  }

  @Override
  public void init() throws ServletException {
    super.init();
    try {
      initializeAppConfigClient();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to initialize AppConfig client", e);
    }
  }

  public void initializeAppConfigClient() {
    if (appConfigDataClient == null) {
      logger.log(
        Level.WARNING,
        "AppConfigDataClient is being created manually, despite being expected to be injected."
      );
      appConfigDataClient = AwsClientFactory.createAppConfigClient();
    }
    startConfigurationSession();
    initializeConfigSupplier();
  }

  public void startConfigurationSession() {
    try {
      StartConfigurationSessionRequest sessionRequest =
        new StartConfigurationSessionRequest()
          .withApplicationIdentifier(
            stackConfiguration.getStack() +
            "-" +
            stackConfiguration.getStackInstance() +
            "-portal-AppConfigApp"
          )
          .withEnvironmentIdentifier(
            stackConfiguration.getStack() +
            "-" +
            stackConfiguration.getStackInstance() +
            "-portal-environment"
          )
          .withConfigurationProfileIdentifier(
            stackConfiguration.getStack() +
            "-" +
            stackConfiguration.getStackInstance() +
            "-portal-configurations"
          );
      StartConfigurationSessionResult sessionResponse =
        appConfigDataClient.startConfigurationSession(sessionRequest);
      configurationToken = sessionResponse.getInitialConfigurationToken();
    } catch (Exception e) {
      logger.log(
        Level.SEVERE,
        "Stack:" +
        stackConfiguration.getStack() +
        "Instance:" +
        stackConfiguration.getStackInstance() +
        "Error starting configuration session",
        e
      );
    }
  }

  public void initializeConfigSupplier() {
    configSupplier =
      Suppliers.memoizeWithExpiration(
        this::getLatestConfiguration,
        5,
        TimeUnit.MINUTES
      );
  }

  public String getLatestConfiguration() {
    if (configurationToken == null) {
      logger.log(
        Level.SEVERE,
        "Configuration token is null, returning default configuration"
      );
      return defaultConfigValue;
    }
    try {
      GetLatestConfigurationRequest latestConfigRequest =
        new GetLatestConfigurationRequest()
          .withConfigurationToken(configurationToken);
      GetLatestConfigurationResult latestConfigResponse =
        appConfigDataClient.getLatestConfiguration(latestConfigRequest);
      configurationToken = latestConfigResponse.getNextPollConfigurationToken();
      ByteBuffer configData = latestConfigResponse.getConfiguration();
      return new String(
        configData.array(),
        java.nio.charset.StandardCharsets.UTF_8
      );
    } catch (Exception e) {
      logger.log(
        Level.SEVERE,
        "Failed to retrieve latest configuration, returning default",
        e
      );
      return defaultConfigValue;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
      String configValue = configSupplier.get();
      response.setContentType("text/plain");
      response.getWriter().write(configValue);
    } catch (Exception e) {
      logger.log(
        Level.SEVERE,
        "Error processing GET request in AppConfigServlet",
        e
      );
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response
        .getWriter()
        .write(
          "{\"error\":\"Error retrieving configuration: " +
          e.getMessage() +
          "\"}"
        );
    }
  }

  @Override
  public void destroy() {
    if (appConfigDataClient != null) {
      try {
        appConfigDataClient.shutdown();
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Failed to shutdown AppConfigDataClient", e);
      }
    }
    super.destroy();
  }
}
