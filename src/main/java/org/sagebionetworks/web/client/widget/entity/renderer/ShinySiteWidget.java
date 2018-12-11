package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ShinySiteWidget implements WidgetRendererPresenter {
	
	//Note: *.synapse.org is also in the whitelist
	private static final String[] VALID_URL_BASES = { 
		"http://spark.rstudio.com/",
		"https://s3.amazonaws.com/static.synapse.org/",
		"https://docs.google.com/a/sagebase.org/forms/",
		"https://calendar.google.com/calendar/"
		};
	private IFrameView view;
	private Map<String, String> descriptor;
	private AuthenticationController authenticationController;
	private SynapseJSNIUtils jsniUtils;
	@Inject
	public ShinySiteWidget(IFrameView view, 
			AuthenticationController authenticationController,
			SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.jsniUtils = jsniUtils;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		int height = getHeightFromDescriptor(descriptor);
		String siteUrl = descriptor.get(WidgetConstants.SHINYSITE_SITE_KEY);
		if(isValidShinySite(siteUrl, jsniUtils)) {
			boolean includePrincipleId = isIncludePrincipalId(descriptor);
			//if we should include the current user's principal id, then append ?principal=<principal> to the siteUrl
			if (includePrincipleId && authenticationController.isLoggedIn()) {
				String delimiter = siteUrl.contains("?") ? "&" : "?";
				siteUrl = siteUrl + delimiter + "principalId=" + authenticationController.getCurrentUserPrincipalId();
			}
			view.configure(siteUrl, height);
		}
			
		else 
			view.showError(siteUrl + DisplayConstants.INVALID_SHINY_SITE);
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
		if(descriptor.containsKey(WidgetConstants.HEIGHT_KEY)) {
			try {
				height = Integer.parseInt(descriptor.get(WidgetConstants.HEIGHT_KEY));
			} catch (NumberFormatException e) {
				// fall back to default
			}
		}
		return height;
	}
	
	public static boolean isIncludePrincipalId(Map<String, String> descriptor) {
		boolean isIncludePrincipleId = false; // default
		if(descriptor.containsKey(WidgetConstants.INCLUDE_PRINCIPAL_ID_KEY)) {
			try {
				isIncludePrincipleId = Boolean.parseBoolean(descriptor.get(WidgetConstants.INCLUDE_PRINCIPAL_ID_KEY));
			} catch (Throwable e) {
				// fall back to default
			}
		}
		return isIncludePrincipleId;
	}


	public static boolean isValidShinySite(String siteUrl, SynapseJSNIUtils jsniUtils) {
		if(siteUrl != null) {
			String hostName = jsniUtils.getHostname(siteUrl.toLowerCase());
			if (hostName != null && 
				(hostName.endsWith(".synapse.org") || hostName.endsWith(".sagebase.org"))) {
				return true;
			}
			for(String base : VALID_URL_BASES) {
				// starts with one of the valid url bases?				
				if(siteUrl.toLowerCase().startsWith(base)) return true;
			}
		}
		return false;
	}

	
}
