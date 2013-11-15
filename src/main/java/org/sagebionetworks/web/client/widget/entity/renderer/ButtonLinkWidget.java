package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ButtonLinkWidget implements ButtonLinkWidgetView.Presenter, WidgetRendererPresenter {
	
	private ButtonLinkWidgetView view;
	private Map<String,String> descriptor;
	
	@Inject
	public ButtonLinkWidget(ButtonLinkWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired) {
		this.descriptor = widgetDescriptor;
		String url = descriptor.get(WidgetConstants.LINK_URL_KEY);
		String buttonText = descriptor.get(WidgetConstants.TEXT_KEY);
		view.configure(wikiKey, buttonText, url);
		descriptor = widgetDescriptor;
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
