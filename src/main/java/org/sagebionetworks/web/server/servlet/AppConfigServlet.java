package org.sagebionetworks.web.server.servlet;

import com.google.gwt.thirdparty.guava.common.base.Supplier;
import com.google.gwt.thirdparty.guava.common.base.Suppliers;
import com.google.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.shared.WebConstants;
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient;
import software.amazon.awssdk.services.appconfigdata.model.BadRequestException;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationRequest;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse;
import software.amazon.awssdk.services.appconfigdata.model.InternalServerException;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionResponse;

public class AppConfigServlet extends HttpServlet {

  public AppConfigDataClient appConfigDataClient;
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
    AppConfigDataClient appConfigDataClient,
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
    startConfigurationSession();
    initializeConfigSupplier();
  }

  public void startConfigurationSession() {
    try {
      String stack = stackConfiguration.getStack();
      String stackInstance = stackConfiguration.getStackInstance();
      StartConfigurationSessionRequest sessionRequest =
        StartConfigurationSessionRequest
          .builder()
          .applicationIdentifier(
            stack + "-" + stackInstance + "-portal-AppConfigApp"
          )
          .environmentIdentifier(
            stack + "-" + stackInstance + "-portal-environment"
          )
          .configurationProfileIdentifier(
            stack + "-" + stackInstance + "-portal-configurations"
          )
          .build();
      StartConfigurationSessionResponse sessionResponse =
        appConfigDataClient.startConfigurationSession(sessionRequest);
      configurationToken = sessionResponse.initialConfigurationToken();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Error starting configuration session", e);
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

  public JSONObjectAdapter getLastConfigValueOrDefault() {
    if (lastConfigValue != null) {
      return lastConfigValue;
    }
    try {
      return new JSONObjectAdapterImpl(DEFAULT_CONFIG_VALUE);
    } catch (JSONObjectAdapterException e) {
      logger.log(
        Level.SEVERE,
        "JSONObjectAdapterException occurred in default configuration",
        e
      );
      throw new RuntimeException(e);
    }
  }

  public JSONObjectAdapter getLatestConfiguration() {
    try {
      if (configurationToken == null) {
        logger.log(
          Level.SEVERE,
          "The configuration token is null, the last config value will be returned." +
          " This usually means that the initial call to initialize the configuration session failed."
        );
        return getLastConfigValueOrDefault();
      }
      GetLatestConfigurationRequest latestConfigRequest =
        GetLatestConfigurationRequest
          .builder()
          .configurationToken(configurationToken)
          .build();
      GetLatestConfigurationResponse latestConfigResponse =
        appConfigDataClient.getLatestConfiguration(latestConfigRequest);
      configurationToken = latestConfigResponse.nextPollConfigurationToken();
      String newConfigString = latestConfigResponse
        .configuration()
        .asUtf8String();

      if (!newConfigString.isEmpty()) {
        lastConfigValue = new JSONObjectAdapterImpl(newConfigString);
      }
    } catch (BadRequestException | InternalServerException e) {
      // Invalid token or server error, re-initialize the session to try to recover.
      logger.log(
        Level.SEVERE,
        "Failed to get latest configuration, returning last or default configuration and attempting to re-initialize the session.",
        e
      );
      initializeAppConfigClient();
      return getLastConfigValueOrDefault();
    } catch (Exception e) {
      logger.log(
        Level.SEVERE,
        "Failed to get or parse latest configuration, returning last or default configuration.",
        e
      );
      return getLastConfigValueOrDefault();
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
        appConfigDataClient.close();
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Failed to shutdown AppConfigDataClient", e);
      }
    }
    super.destroy();
  }
}
