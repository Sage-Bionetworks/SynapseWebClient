package org.sagebionetworks.web.client;

import static org.sagebionetworks.web.shared.WebConstants.ONESAGE_ACCOUNT_SETTINGS_PATH;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import org.sagebionetworks.web.shared.WebConstants;

public class OneSageUtilsImpl implements OneSageUtils {

  private final GWTWrapper gwtWrapper;
  private final SynapseProperties synapseProperties;

  @Inject
  public OneSageUtilsImpl(
    GWTWrapper gwtWrapper,
    SynapseProperties synapseProperties
  ) {
    this.gwtWrapper = gwtWrapper;
    this.synapseProperties = synapseProperties;
  }

  private String getOriginForOneSage() {
    // SWC-6533: We do not want to stack hop for Prod and Staging

    if (synapseProperties.getIsDevMode()) {
      // If in dev mode, redirect to port 3000 (might be remote host)
      return getOrigin(
        Window.Location.getProtocol(),
        Window.Location.getHostName(),
        "3000"
      );
    }

    switch (gwtWrapper.getHostName().toLowerCase()) {
      case "staging.synapse.org":
        return "https://staging.accounts.synapse.org";
      case "dev.synapse.org":
        return "https://dev.accounts.synapse.org";
      case "localhost":
      case "127.0.0.1":
        return getOrigin(
          Window.Location.getProtocol(),
          Window.Location.getHostName(),
          "3000"
        );
      default:
        return "https://accounts.synapse.org";
    }
  }

  public String getAppIdForOneSage() {
    if (synapseProperties.getIsDevMode()) {
      // If in dev mode, always use the `localhost` app.
      return "localhost";
    }

    switch (gwtWrapper.getHostName().toLowerCase()) {
      case "staging.synapse.org":
        return "staging.synapse.org";
      case "dev.synapse.org":
        return "dev.synapse.org";
      case "localhost":
      case "127.0.0.1":
        return "localhost";
      default:
        return "synapse.org";
    }
  }

  public String getOneSageURL() {
    return getOneSageURL("/");
  }

  /**
   * Based on the current hostname, generate a URL pointing to an instance of OneSage with an appropriate appId search param.
   * @param path
   * @return a String representation of the OneSage URL
   */
  public String getOneSageURL(String path) {
    return (
      getOriginForOneSage() +
      path +
      "?" +
      WebConstants.ONESAGE_SYNAPSE_APPID_QUERY_PARAM_KEY +
      "=" +
      getAppIdForOneSage()
    );
  }

  public String getAccountSettingsURL() {
    return getOneSageURL(ONESAGE_ACCOUNT_SETTINGS_PATH);
  }

  private String getOrigin(String protocol, String hostname, String port) {
    StringBuilder origin = new StringBuilder();
    origin.append(protocol);
    origin.append("//");
    origin.append(hostname);
    if (port != null) {
      origin.append(":");
      origin.append(port);
    }
    return origin.toString();
  }
}
