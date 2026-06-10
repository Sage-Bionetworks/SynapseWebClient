package org.sagebionetworks.web.client.widget.footer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.security.AuthenticationController;

@Singleton
public class Footer implements FooterView.Presenter, IsWidget {

  public static final String UNKNOWN = "unknown";
  private FooterView view;
  GlobalApplicationState globalAppState;
  AuthenticationController authController;
  GWTWrapper gwt;
  SynapseJSNIUtils jsniUtils;

  @Inject
  public Footer(
    FooterView view,
    GlobalApplicationState globalAppState,
    AuthenticationController authController,
    GWTWrapper gwt,
    SynapseJSNIUtils jsniUtils
  ) {
    this.view = view;
    this.globalAppState = globalAppState;
    this.authController = authController;
    this.gwt = gwt;
    this.jsniUtils = jsniUtils;
    view.setPresenter(this);
    init();
  }

  public void init() {
    globalAppState.checkVersionCompatibility(
      new AsyncCallback<VersionState>() {
        @Override
        public void onSuccess(VersionState state) {
          if (state == null || state.getVersion() == null) {
            onFailure(null);
            return;
          }
          String versions = state.getVersion();
          String[] vals = versions.split(",");
          if (vals.length == 2) {
            view.setVersion(vals[0], vals[1], SRC.SynapseReactClientVersion);
          } else {
            onFailure(null);
            return;
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          view.setVersion(UNKNOWN, UNKNOWN, SRC.SynapseReactClientVersion);
        }
      }
    );
    view.refresh();
  }

  public Widget asWidget() {
    return view.asWidget();
  }

  public void refresh() {
    view.refresh();
  }
}
