package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BacktickWidget implements BacktickWidgetView.Presenter, WidgetRendererPresenter {
	private BacktickWidgetView view;
	private Map<String, String> descriptor;
	
	@Inject
	public BacktickWidget(BacktickWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor) {
		descriptor = widgetDescriptor;
		view.configure(descriptor.get(WidgetConstants.TEXT_KEY));
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

}
