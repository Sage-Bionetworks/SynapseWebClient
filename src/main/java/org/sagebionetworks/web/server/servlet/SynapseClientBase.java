package org.sagebionetworks.web.server.servlet;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.server.StackEndpoints;

@SuppressWarnings("serial")
public class SynapseClientBase
  extends RemoteServiceServlet
  implements TokenProvider, RequestHostProvider {

  // This will be appended to the User-Agent header.
  public static final String PORTAL_USER_AGENT =
    "Synapse-Web-Client/" + PortalVersionHolder.getVersionInfo();

  public static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

  private static class PortalVersionHolder {

    private static String versionInfo = "";

    static {
      InputStream s =
        SynapseClientBase.class.getResourceAsStream("/version-info.properties");
      Properties prop = new Properties();
      try {
        prop.load(s);
      } catch (IOException e) {
        throw new RuntimeException("version-info.properties file not found", e);
      }
      versionInfo = prop.getProperty("org.sagebionetworks.portal.version");
    }

    private static String getVersionInfo() {
      return versionInfo;
    }
  }

  private TokenProvider tokenProvider = this;
  private RequestHostProvider requestHostProvider = this;
  AdapterFactory adapterFactory = new AdapterFactoryImpl();

  /**
   * Injected with Gin
   */
  private SynapseProvider synapseProvider = new SynapseProviderImpl();

  /**
   * This allows tests provide mock org.sagebionetworks.client.SynapseClient ojbects
   *
   * @param provider
   */
  public void setSynapseProvider(SynapseProvider provider) {
    this.synapseProvider = provider;
  }

  /**
   * This allows integration tests to override the token provider.
   *
   * @param tokenProvider
   */
  public void setTokenProvider(TokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  /**
   * Validate that the service is ready to go. If any of the injected data is missing then it cannot
   * run. Public for tests.
   */
  public void validateService() {
    if (synapseProvider == null) throw new IllegalStateException(
      "The SynapseProvider was not set"
    );
    if (tokenProvider == null) {
      throw new IllegalStateException("The token provider was not set");
    }
  }

  @Override
  public String getToken() {
    // By default, we get the token from the request cookies.
    return UserDataProvider.getThreadLocalUserToken(
      this.getThreadLocalRequest()
    );
  }

  @Override
  public String getRequestHost() {
    return UserDataProvider.getThreadLocalRequestHost(
      this.getThreadLocalRequest()
    );
  }

  protected org.sagebionetworks.client.SynapseClient createSynapseClient() {
    return createSynapseClient(
      requestHostProvider.getRequestHost(),
      tokenProvider.getToken()
    );
  }

  public org.sagebionetworks.client.SynapseClient createAnonymousSynapseClient() {
    return createSynapseClient(requestHostProvider.getRequestHost(), null);
  }

  /**
   * The org.sagebionetworks.client.SynapseClient client is stateful so we must create a new one for
   * each request
   */
  public org.sagebionetworks.client.SynapseClient createSynapseClient(
    String requestHost,
    String accessToken
  ) {
    // SynapseClient is stateful, so always create a new one
    org.sagebionetworks.client.SynapseClient synapseClient = synapseProvider.createNewClient(
      requestHost
    );
    if (accessToken != null) {
      synapseClient.setBearerAuthorizationToken(accessToken);
    }
    // Append the portal's version information to the user agent.
    synapseClient.appendUserAgent(PORTAL_USER_AGENT);
    if (this.getThreadLocalRequest() != null) {
      synapseClient.setUserIpAddress(
        getIpAddress(this.getThreadLocalRequest())
      );
    }
    return synapseClient;
  }

  public static String getIpAddress(HttpServletRequest httpServletRequest) {
    String xForwardedForHeaderVal = httpServletRequest.getHeader(
      X_FORWARDED_FOR_HEADER
    );
    return xForwardedForHeaderVal == null
      ? httpServletRequest.getRemoteAddr()
      : xForwardedForHeaderVal;
  }

  @Override
  protected SerializationPolicy doGetSerializationPolicy(
    HttpServletRequest request,
    String moduleBaseURL,
    String strongName
  ) {
    // true client side relative location is the app name
    String newModuleBaseURL = moduleBaseURL;
    try {
      URL url = new URL(moduleBaseURL);
      StringBuilder builder = new StringBuilder();
      builder.append(url.getProtocol());
      builder.append("://");
      builder.append(url.getHost());
      builder.append("/Portal/");
      newModuleBaseURL = builder.toString();
    } catch (MalformedURLException ex) {
      // we have no affect
    }

    return super.doGetSerializationPolicy(
      request,
      newModuleBaseURL,
      strongName
    );
  }
}
