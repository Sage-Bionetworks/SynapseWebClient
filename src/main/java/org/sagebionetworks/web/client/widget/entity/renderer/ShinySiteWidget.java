package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ShinySiteWidget implements ShinySiteWidgetView.Presenter, WidgetRendererPresenter {
	
	private static final String[] VALID_URL_BASES = { "http://glimmer.rstudio.com", "http://shiny.synapse.org", "https://shiny.synapse.org", "http://spark.rstudio.com/" };
	private ShinySiteWidgetView view;
	private Map<String, String> descriptor;
	private AuthenticationController authenticationController;
	
	@Inject
	public ShinySiteWidget(ShinySiteWidgetView view, AuthenticationController authenticationController) {
		this.view = view;
		this.authenticationController = authenticationController;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		int height = getHeightFromDescriptor(descriptor);
		String siteUrl = descriptor.get(WidgetConstants.SHINYSITE_SITE_KEY);
		if(isValidShinySite(siteUrl)) {
			boolean includePrincipleId = isIncludePrincipalId(descriptor);
			//if we should include the current user's principal id, then append ?principal=<principal> to the siteUrl
			if (includePrincipleId && authenticationController.isLoggedIn()) {
				String delimiter = siteUrl.contains("?") ? "&" : "?";
				siteUrl = siteUrl + delimiter + "principalId=" + authenticationController.getCurrentUserPrincipalId();
			}
			view.configure(siteUrl, height);
		}
			
		else 
			view.showInvalidSiteUrl(siteUrl);
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public static int getHeightFromDescriptor(Map<String, String> descriptor) {
		int height = WidgetConstants.SHINYSITE_DEFAULT_HEIGHT_PX; // default
		if(descriptor.containsKey(WidgetConstants.SHINYSITE_HEIGHT_KEY)) {
			try {
				height = Integer.parseInt(descriptor.get(WidgetConstants.SHINYSITE_HEIGHT_KEY));
			} catch (NumberFormatException e) {
				// fall back to default
			}
		}
		return height;
	}
	
	public static boolean isIncludePrincipalId(Map<String, String> descriptor) {
		boolean isIncludePrincipleId = false; // default
		if(descriptor.containsKey(WidgetConstants.SHINYSITE_INCLUDE_PRINCIPAL_ID_KEY)) {
			try {
				isIncludePrincipleId = Boolean.parseBoolean(descriptor.get(WidgetConstants.SHINYSITE_INCLUDE_PRINCIPAL_ID_KEY));
			} catch (Throwable e) {
				// fall back to default
			}
		}
		return isIncludePrincipleId;
	}


	public static boolean isValidShinySite(String siteUrl) {
		if(siteUrl != null) {
			for(String base : VALID_URL_BASES) {
				// starts with one of the valid url bases?				
				if(siteUrl.toLowerCase().startsWith(base)) return true;
			}
		}
		return false;
	}

	
}
