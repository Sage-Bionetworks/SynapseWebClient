package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EmptyWidget implements EmptyWidgetView.Presenter, WidgetRendererPresenter {

	private EmptyWidgetView view;

	@Inject
	public EmptyWidget(EmptyWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
