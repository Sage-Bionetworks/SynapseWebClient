package org.sagebionetworks.web.client;

import static org.sagebionetworks.web.shared.WebConstants.ONESAGE_ACCOUNT_SETTINGS_PATH;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import org.sagebionetworks.web.shared.WebConstants;

public class OneSageUtilsImpl implements OneSageUtils {

  private final GWTWrapper gwtWrapper;

  @Inject
  public OneSageUtilsImpl(GWTWrapper gwtWrapper) {
    this.gwtWrapper = gwtWrapper;
  }

  private String getHostForOneSage() {
    // SWC-6533: We do not want to stack hop for Prod and Staging
    switch (gwtWrapper.getHostName().toLowerCase()) {
      case "staging.synapse.org":
        return "https://staging.accounts.synapse.org";
      case "portal-dev.dev.sagebase.org":
        return "https://accounts-dev.dev.sagebase.org";
      case "localhost":
      case "127.0.0.1":
        return "http://" + Window.Location.getHostName() + ":3000";
      default:
        return "https://accounts.synapse.org";
    }
  }

  public String getAppIdForOneSage() {
    switch (gwtWrapper.getHostName().toLowerCase()) {
      case "staging.synapse.org":
        return "staging.synapse.org";
      case "portal-dev.dev.sagebase.org":
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
      getHostForOneSage() +
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
}
