package org.sagebionetworks.web.client.widget.statistics;

import static org.sagebionetworks.web.shared.WebConstants.REPO_SERVICE_URL_KEY;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StatisticsPlotWidgetViewImpl implements StatisticsPlotWidgetView, IsWidget {
	public static final String ROOT_PORTAL_URL = Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/";
	public static final String GOOGLE_OAUTH_CALLBACK_URL = ROOT_PORTAL_URL + "Portal/oauth2callback?oauth2provider=GOOGLE_OAUTH_2_0";
	public static final String GOOGLE_OAUTH_WITH_STATE_CALLBACK_URL = GOOGLE_OAUTH_CALLBACK_URL + "&state=";

	public interface StatisticsPlotWidgetViewImplUiBinder extends UiBinder<Widget, StatisticsPlotWidgetViewImpl> {
	}

	@UiField
	Div srcContainer;
	@UiField
	Button closeButton;
	@UiField
	Modal statsPlotModal;
	Widget widget;
	SynapseJSNIUtils jsniUtils;
	String endpoint;
	boolean isConfigured = false;

	@Inject
	public StatisticsPlotWidgetViewImpl(StatisticsPlotWidgetViewImplUiBinder binder, SynapseJSNIUtils jsniUtils, SynapseProperties synapseProperties) {
		widget = binder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
		endpoint = synapseProperties.getSynapseProperty(REPO_SERVICE_URL_KEY);
		widget.addAttachHandler(event -> {
			if (!event.isAttached()) {
				// detach event, clean up react component
				jsniUtils.unmountComponentAtNode(srcContainer.getElement());
			}
		});
		closeButton.addClickHandler(event -> {
			statsPlotModal.hide();
		});
	}

	@Override
	public void configureAndShow(String projectId, String sessionToken) {
		if (isConfigured) {
			jsniUtils.unmountComponentAtNode(srcContainer.getElement());
		}
		_createSRCWidget(srcContainer.getElement(), projectId, sessionToken, endpoint);
		isConfigured = true;
		statsPlotModal.show();
	}

	private static native void _createSRCWidget(Element el, String projectId, String sessionToken, String fullRepoEndpoint) /*-{
		try {
			// URL.host returns the domain (that is the hostname) followed by (if a port was specified) a ':' and the port of the URL
			var repoURL = new URL(fullRepoEndpoint);
			var rootRepoEndpoint = repoURL.protocol + '//' + repoURL.host;

			var props = {
				token : sessionToken,
				request : {
					concreteType : 'org.sagebionetworks.repo.model.statistics.ProjectFilesStatisticsRequest',
					objectId : projectId,
					fileDownloads : true,
					fileUploads : true
				},
				endpoint : rootRepoEndpoint,
				title : 'Project Statistics'
			}
			$wnd.ReactDOM
					.render($wnd.React.createElement(
							$wnd.SRC.SynapseComponents.StatisticsPlot, props,
							null), el);
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
