package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class IntendedDataUseReportWidget implements WidgetRendererPresenter {
	public static final String ACCESS_RESTRICTION_ID = "accessRestrictionId";
	IntendedDataUseReportWidgetView view;
	
	@Inject
	public IntendedDataUseReportWidget(IntendedDataUseReportWidgetView view) {
		this.view = view;
	}

	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		String arId = widgetDescriptor.get(ACCESS_RESTRICTION_ID).trim();
		view.render(arId);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
