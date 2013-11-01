package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableOfContentsWidget implements TableOfContentsWidgetView.Presenter, WidgetRendererPresenter {
	
	private TableOfContentsWidgetView view;
	private Map<String, String> descriptor;
	
	@Inject
	public TableOfContentsWidget(TableOfContentsWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		view.configure();
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
