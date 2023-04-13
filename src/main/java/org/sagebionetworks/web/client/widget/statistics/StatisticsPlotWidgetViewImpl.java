package org.sagebionetworks.web.client.widget.statistics;

import static org.sagebionetworks.web.shared.WebConstants.REPO_SERVICE_URL_KEY;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsni.FullContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class StatisticsPlotWidgetViewImpl
  implements StatisticsPlotWidgetView, IsWidget {

  public static final String ROOT_PORTAL_URL =
    Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/";
  public static final String GOOGLE_OAUTH_CALLBACK_URL =
    ROOT_PORTAL_URL + "Portal/oauth2callback?oauth2provider=GOOGLE_OAUTH_2_0";
  public static final String GOOGLE_OAUTH_WITH_STATE_CALLBACK_URL =
    GOOGLE_OAUTH_CALLBACK_URL + "&state=";

  public interface StatisticsPlotWidgetViewImplUiBinder
    extends UiBinder<Widget, StatisticsPlotWidgetViewImpl> {}

  @UiField
  ReactComponentDiv srcContainer;

  @UiField
  Button closeButton;

  @UiField
  Modal statsPlotModal;

  Widget widget;
  SynapseJSNIUtils jsniUtils;
  SynapseReactClientFullContextPropsProvider propsProvider;
  String endpoint;

  @Inject
  public StatisticsPlotWidgetViewImpl(
    StatisticsPlotWidgetViewImplUiBinder binder,
    SynapseJSNIUtils jsniUtils,
    SynapseProperties synapseProperties,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    widget = binder.createAndBindUi(this);
    this.jsniUtils = jsniUtils;
    this.propsProvider = propsProvider;
    endpoint = synapseProperties.getSynapseProperty(REPO_SERVICE_URL_KEY);
    closeButton.addClickHandler(event -> {
      statsPlotModal.hide();
    });
  }

  @Override
  public void configureAndShow(String projectId, String accessToken) {
    _createSRCWidget(
      srcContainer,
      projectId,
      accessToken,
      endpoint,
      propsProvider.getJsniContextProps()
    );
    statsPlotModal.show();
  }

  private static native void _createSRCWidget(
    ReactComponentDiv reactComponentDiv,
    String projectId,
    String accessToken,
    String fullRepoEndpoint,
    FullContextProviderPropsJSNIObject wrapperProps
  ) /*-{
		try {
			// URL.host returns the domain (that is the hostname) followed by (if a port was specified) a ':' and the port of the URL
			var repoURL = new URL(fullRepoEndpoint);
			var rootRepoEndpoint = repoURL.protocol + '//' + repoURL.host;

			var props = {
				request : {
					concreteType : 'org.sagebionetworks.repo.model.statistics.ProjectFilesStatisticsRequest',
					objectId : projectId,
					fileDownloads : true,
					fileUploads : true
				},
				endpoint : rootRepoEndpoint,
				title : 'Project Statistics'
			}

			var component = $wnd.React.createElement($wnd.SRC.SynapseComponents.StatisticsPlot, props, null)
			var wrapper = $wnd.React.createElement($wnd.SRC.SynapseContext.FullContextProvider, wrapperProps, component)
			reactComponentDiv.@org.sagebionetworks.web.client.widget.ReactComponentDiv::render(Lorg/sagebionetworks/web/client/jsinterop/ReactNode;)(wrapper);
		} catch (err) {
			console.error(err);
		}
	}-*/;

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void clear() {}

  @Override
  public void setVisible(boolean visible) {
    widget.setVisible(visible);
  }
}
