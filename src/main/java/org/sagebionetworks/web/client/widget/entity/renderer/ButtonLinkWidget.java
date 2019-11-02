package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget.isIncludePrincipalId;
import java.util.Map;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ButtonLinkWidget implements WidgetRendererPresenter {

	public static final String SYNAPSE_USER_ID_QUERY_PARAM = "synapseUserId=";
	private ButtonLinkWidgetView view;
	private AuthenticationController authController;
	private Map<String, String> descriptor;
	private GWTWrapper gwt;
	public static final String LINK_OPENS_NEW_WINDOW = "openNewWindow";
	public static final String WIDTH = "width";
	AppPlaceHistoryMapper appPlaceHistoryMapper;
	SynapseJavascriptClient jsClient;

	@Inject
	public ButtonLinkWidget(ButtonLinkWidgetView view, GWTWrapper gwt, AuthenticationController authController, AppPlaceHistoryMapper appPlaceHistoryMapper, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.gwt = gwt;
		this.authController = authController;
		this.appPlaceHistoryMapper = appPlaceHistoryMapper;
		this.jsClient = jsClient;
	}

	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		String url = descriptor.get(WidgetConstants.LINK_URL_KEY);
		String buttonText = descriptor.get(WidgetConstants.TEXT_KEY);

		// SWC-4234: if Synapse place, add entity id to entity path cache up front
		if (url.contains("#!Synapse:")) {
			Synapse synapsePlace = (Synapse) appPlaceHistoryMapper.getPlace(url.substring(url.indexOf('!')));
			jsClient.populateEntityBundleCache(synapsePlace.getEntityId());
		}

		if (isIncludePrincipalId(descriptor) && authController.isLoggedIn()) {
			String prefix = url.contains("?") ? "&" : "?";
			url += prefix + SYNAPSE_USER_ID_QUERY_PARAM + authController.getCurrentUserPrincipalId();
		}
		boolean isHighlight = false;
		if (descriptor.containsKey(WebConstants.HIGHLIGHT_KEY)) {
			isHighlight = Boolean.parseBoolean(descriptor.get(WebConstants.HIGHLIGHT_KEY));
		}
		// determine if link should be opened in a new window
		boolean openInNewWindow;
		// check for optional parameter
		if (descriptor.containsKey(LINK_OPENS_NEW_WINDOW)) {
			openInNewWindow = Boolean.parseBoolean(descriptor.get(LINK_OPENS_NEW_WINDOW));
		} else {
			// param not present, make a smart choice
			openInNewWindow = isOpenInNewWindow(url);
		}

		view.configure(wikiKey, buttonText, url, isHighlight, openInNewWindow);
		if (descriptor.containsKey(WIDTH)) {
			view.setWidth(descriptor.get(WIDTH));
		} else {
			view.setSize(ButtonSize.LARGE);
		}
		if (descriptor.containsKey(WidgetConstants.ALIGNMENT_KEY)) {
			view.addStyleNames(ImageWidget.getAlignmentStyleNames(descriptor.get(WidgetConstants.ALIGNMENT_KEY)));
		}
		descriptor = widgetDescriptor;
	}

	public boolean isOpenInNewWindow(String url) {
		return url != null && !(url.startsWith("#!") || url.startsWith(gwt.getHostPrefix()));
	}

	@SuppressWarnings("unchecked")
	public void clearState() {}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
