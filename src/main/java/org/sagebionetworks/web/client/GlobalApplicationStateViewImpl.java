package org.sagebionetworks.web.client;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.options.DialogOptions;

public class GlobalApplicationStateViewImpl
  implements GlobalApplicationStateView {

  private static final int UNLIMITED_TIME = 0;

  @Override
  public void showVersionOutOfDateGlobalMessage() {
    DisplayUtils.showErrorToast(
      DisplayConstants.NEW_VERSION_AVAILABLE +
      DisplayConstants.NEW_VERSION_INSTRUCTIONS,
      UNLIMITED_TIME
    );
    loadNewVersion();
  }

  @Override
  public void showGetVersionError(String error) {
    DisplayUtils.showErrorToast(
      "Unable to determine the Synapse version. Please refresh the page to get the latest version. " +
      error,
      5000
    );
    loadNewVersion();
  }

  @Override
  public void initGlobalViewProperties() {
    DialogOptions options = DialogOptions.newOptions("");
    options.setAnimate(false);
    Bootbox.setDefaults(options);
  }

  @Override
  public void initSRCEndpoints(String repoEndpoint, String portalEndpoint) {
    _initSRCEndpoints(repoEndpoint, portalEndpoint);
  }

  private static final native void _initSRCEndpoints(
    String repoEndpoint,
    String portalEndpoint
  ) /*-{
		try {
			$wnd.SRC.OVERRIDE_ENDPOINT_CONFIG = {
				REPO : repoEndpoint,
				PORTAL : portalEndpoint,
			}
		} catch (err) {
			console.error(err);
		}
	}-*/;

  public void loadNewVersion() {
    // reload, after a (30 minute) delay
    Timer timer = new Timer() {
      public void run() {
        Window.Location.reload();
      }
    };
    timer.schedule(1000 * 60 * 30);
  }

  @Override
  public void back() {
    History.back();
  }

  @Override
  public void addNativePreviewHandler(NativePreviewHandler handler) {
    Event.addNativePreviewHandler(handler);
  }
}
