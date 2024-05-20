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

  private String lastConfigValue = "";

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
    initializeAppConfigClient();
  }

  public void initializeAppConfigClient() {
    if (appConfigDataClient == null) {
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
      configurationToken = null;
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
      String newConfigValue = new String(
        configData.array(),
        java.nio.charset.StandardCharsets.UTF_8
      ); // This may be empty if the client already has the latest version of the configuration.
      if (!newConfigValue.isEmpty()) {
        lastConfigValue = newConfigValue;
      }
      return lastConfigValue;
    } catch (Exception e) {
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
