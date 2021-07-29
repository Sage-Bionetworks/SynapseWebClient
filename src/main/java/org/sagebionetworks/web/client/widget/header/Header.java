package org.sagebionetworks.web.client.widget.header;

import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DateTimeUtilsImpl;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class Header implements HeaderView.Presenter, IsWidget {

	public static final String WWW_SYNAPSE_ORG = "www.synapse.org";

	private HeaderView view;
	private SynapseJSNIUtils synapseJSNIUtils;
	CookieProvider cookies;
	public static boolean isShowingPortalAlert = false;
	public static JSONObjectAdapter portalAlertJson = null;

	@Inject
	public Header(HeaderView view, SynapseJSNIUtils synapseJSNIUtils, EventBus eventBus, CookieProvider cookies, JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.cookies = cookies;
		this.synapseJSNIUtils = synapseJSNIUtils;
		view.clear();
		
		view.setPresenter(this);
		initStagingAlert();
		view.getEventBinder().bindEventHandlers(this, eventBus);
		if (cookies.getCookie(CookieKeys.COOKIES_ACCEPTED) == null) {
			view.setCookieNotificationVisible(true);
		} else {
			view.setCookieNotificationVisible(false);
		}
		// portal alert state sticks around for entire app session
		String portalAlertString = cookies.getCookie(CookieKeys.PORTAL_CONFIG);
		isShowingPortalAlert = portalAlertString != null;
		if (isShowingPortalAlert) {
			cookies.removeCookie(CookieKeys.PORTAL_CONFIG);
			try {
				portalAlertJson = jsonObjectAdapter.createNew(portalAlertString);
			} catch (JSONObjectAdapterException e) {
				synapseJSNIUtils.consoleError(e);
			}
		} else {
			portalAlertJson = null;
		}
		view.setPortalAlertVisible(isShowingPortalAlert, portalAlertJson);

	}

	public void initStagingAlert() {
		String hostName = synapseJSNIUtils.getCurrentHostName().toLowerCase();
		boolean visible = !hostName.contains(WWW_SYNAPSE_ORG);
		view.setStagingAlertVisible(visible);
	}

	public void configure() {
		refresh();
	}

	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}

	public void refresh() {
		view.setPortalAlertVisible(isShowingPortalAlert, portalAlertJson);
		view.refresh();
	}

	@Override
	public void onCookieNotificationDismissed() {
		view.setCookieNotificationVisible(false);
		cookies.setCookie(CookieKeys.COOKIES_ACCEPTED, Boolean.TRUE.toString(), DateTimeUtilsImpl.getYearFromNow());
	}
}
