package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ButtonLinkWidget implements ButtonLinkWidgetView.Presenter, WidgetRendererPresenter {
	
	private ButtonLinkWidgetView view;
	private Map<String,String> descriptor;
	private GWTWrapper gwt;
	public static final String LINK_OPENS_NEW_WINDOW = "openNewWindow";
	
	@Inject
	public ButtonLinkWidget(ButtonLinkWidgetView view, GWTWrapper gwt) {
		this.view = view;
		this.gwt = gwt;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		String url = descriptor.get(WidgetConstants.LINK_URL_KEY);
		String buttonText = descriptor.get(WidgetConstants.TEXT_KEY);
		boolean isHighlight = false;
		if (descriptor.containsKey(WebConstants.HIGHLIGHT_KEY)){
			isHighlight = Boolean.parseBoolean(descriptor.get(WebConstants.HIGHLIGHT_KEY));
		}
		//determine if link should be opened in a new window
		boolean openInNewWindow;
		//check for optional parameter
		if (descriptor.containsKey(LINK_OPENS_NEW_WINDOW)){
			openInNewWindow = Boolean.parseBoolean(descriptor.get(LINK_OPENS_NEW_WINDOW));
		} else {
			//param not present, make a smart choice
			openInNewWindow = isOpenInNewWindow(url);
		}
		
		view.configure(wikiKey, buttonText, url, isHighlight, openInNewWindow);
		descriptor = widgetDescriptor;
	}
	
	public boolean isOpenInNewWindow(String url) {
		return url != null && !(url.startsWith("#!") || url.startsWith(gwt.getHostPrefix())); 
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

		/*
	 * Private Methods
	 */
}
