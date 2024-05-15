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
  String stackInstance;
  String stack;
  StackConfiguration stackConfiguration;
  private String defaultConfigValue = "{\"default configuration\":\"true\"}";

  private static Logger logger = Logger.getLogger(
    LinkedInServiceImpl.class.getName()
  );

  @Inject
  public AppConfigServlet(
    AWSAppConfigData appConfigDataClient,
    StackConfiguration stackConfiguration
  ) {
    this.appConfigDataClient = appConfigDataClient;
    this.stackConfiguration = stackConfiguration;
  }

  /*
       Initialization Order: The code initializes the appConfigDataClient and starts the configuration
       session in the init() method of the servlet. It should handle scenarios where the servlet may
       be reinitialized or reloaded during the application lifecycle, potentially leading to multiple
       configuration sessions or resource leaks.
    */
  @Override
  public void init() throws ServletException {
    super.init();
    stackInstance = stackConfiguration.getStackInstance();
    stack = stackConfiguration.getStack();
    initializeAppConfigClient();
  }

  private void initializeAppConfigClient() {
    if (appConfigDataClient == null) { // is this even needed if I use injection?
      appConfigDataClient = AwsClientFactory.createAppConfigClient();
    }
    startConfigurationSession();
    initializeConfigSupplier();
  }

  public void startConfigurationSession() {
    StartConfigurationSessionRequest sessionRequest =
      new StartConfigurationSessionRequest()
        .withApplicationIdentifier(
          stack + "-" + stackInstance + "-AppConfigApp"
        )
        .withEnvironmentIdentifier(stack + "-" + stackInstance + "-environment")
        .withConfigurationProfileIdentifier(
          stack + "-" + stackInstance + "-configurations"
        );
    StartConfigurationSessionResult sessionResponse =
      appConfigDataClient.startConfigurationSession(sessionRequest);
    configurationToken = sessionResponse.getInitialConfigurationToken();
  }

  public void initializeConfigSupplier() {
    configSupplier =
      Suppliers.memoizeWithExpiration(
        this::getLatestConfiguration,
        5,
        TimeUnit.MINUTES
      );
  }

  private String getLatestConfiguration() {
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
      ); //JSON String
    } catch (Exception e) {
      // log the error
      logger.log(Level.SEVERE, e.getMessage(), e);
      return defaultConfigValue;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
      initializeAppConfigClient();
      String configValue = configSupplier.get();
      response.setContentType("application/json");
      response.getWriter().write(configValue);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response
        .getWriter()
        .write("Error retrieving configuration: " + e.getMessage());
    }
  }

  @Override
  public void destroy() {
    if (appConfigDataClient != null) {
      appConfigDataClient.shutdown();
    }
    super.destroy();
  }
}
