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
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.shared.WebConstants;

public class AppConfigServlet extends HttpServlet {

  public AWSAppConfigData appConfigDataClient;
  public Supplier<JSONObjectAdapter> configSupplier;
  public String configurationToken;
  private StackConfiguration stackConfiguration;
  private final String DEFAULT_CONFIG_VALUE = "{}";
  private JSONObjectAdapter lastConfigValue;

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
      String stack = stackConfiguration.getStack();
      String stackInstance = stackConfiguration.getStackInstance();
      StartConfigurationSessionRequest sessionRequest =
        new StartConfigurationSessionRequest()
          .withApplicationIdentifier(
            stack + "-" + stackInstance + "-portal-AppConfigApp"
          )
          .withEnvironmentIdentifier(
            stack + "-" + stackInstance + "-portal-environment"
          )
          .withConfigurationProfileIdentifier(
            stack + "-" + stackInstance + "-portal-configurations"
          );
      StartConfigurationSessionResult sessionResponse =
        appConfigDataClient.startConfigurationSession(sessionRequest);
      configurationToken = sessionResponse.getInitialConfigurationToken();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error starting configuration session", e);
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

  public JSONObjectAdapter getLatestConfiguration() {
    try {
      if (configurationToken == null) {
        logger.log(Level.SEVERE, "returning default config");
        return new JSONObjectAdapterImpl(DEFAULT_CONFIG_VALUE);
      }
      GetLatestConfigurationRequest latestConfigRequest =
        new GetLatestConfigurationRequest()
          .withConfigurationToken(configurationToken);
      GetLatestConfigurationResult latestConfigResponse =
        appConfigDataClient.getLatestConfiguration(latestConfigRequest);
      configurationToken = latestConfigResponse.getNextPollConfigurationToken();
      ByteBuffer readOnlyConfigData = latestConfigResponse
        .getConfiguration()
        .asReadOnlyBuffer();
      byte[] bytes = new byte[readOnlyConfigData.remaining()];
      readOnlyConfigData.get(bytes);
      String newConfigString = new String(
        bytes,
        java.nio.charset.StandardCharsets.UTF_8
      );

      if (!newConfigString.isEmpty()) {
        lastConfigValue = new JSONObjectAdapterImpl(newConfigString);
      }
    } catch (Exception e) {
      try {
        logger.log(
          Level.SEVERE,
          "Failed to get or parse latest configuration, returning default configuration",
          e
        );
        return new JSONObjectAdapterImpl(DEFAULT_CONFIG_VALUE);
      } catch (JSONObjectAdapterException exeption) {
        logger.log(
          Level.SEVERE,
          "JSONObjectAdapterException occurred in default configuration",
          e
        );
      }
    }
    return lastConfigValue;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    response.setHeader(
      WebConstants.CACHE_CONTROL_KEY,
      WebConstants.CACHE_CONTROL_VALUE_NO_CACHE
    );
    try {
      JSONObjectAdapter configValue = configSupplier.get();
      response.setContentType("application/json");
      response.getWriter().write(configValue.toString());
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
