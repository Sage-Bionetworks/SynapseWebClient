package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ShinySiteWidget implements ShinySiteWidgetView.Presenter, WidgetRendererPresenter {
	
	private static final String[] VALID_URL_BASES = { "http://glimmer.rstudio.com", "http://shiny.synapse.org" };
	private ShinySiteWidgetView view;
	private Map<String, String> descriptor;
	
	@Inject
	public ShinySiteWidget(ShinySiteWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		int height = getHeightFromDescriptor(descriptor);
		String siteUrl = descriptor.get(WidgetConstants.SHINYSITE_SITE_KEY);
		if(isValidShinySite(siteUrl))
			view.configure(siteUrl, height);
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
