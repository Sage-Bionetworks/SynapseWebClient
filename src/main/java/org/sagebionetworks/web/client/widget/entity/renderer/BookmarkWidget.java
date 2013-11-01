package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BookmarkWidget implements BookmarkWidgetView.Presenter, WidgetRendererPresenter {
	private BookmarkWidgetView view;
	private Map<String, String> descriptor;
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Inject
	public BookmarkWidget(BookmarkWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired) {
		descriptor = widgetDescriptor;
		view.configure(descriptor.get(WidgetConstants.BOOKMARK_KEY), descriptor.get(WidgetConstants.TEXT_KEY));
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

}
