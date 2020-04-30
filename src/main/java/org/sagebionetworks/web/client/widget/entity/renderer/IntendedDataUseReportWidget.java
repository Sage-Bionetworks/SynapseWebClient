package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.accessrequirements.IntendedDataUseGenerator;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class IntendedDataUseReportWidget implements WidgetRendererPresenter {
	private DivView view;
	private Map<String, String> descriptor;
	public static final String ACCESS_RESTRICTION_ID = "accessRestrictionId";
	IntendedDataUseGenerator iduGenerator;
	MarkdownWidget mdWidget;
	@Inject
	public IntendedDataUseReportWidget(DivView view, IntendedDataUseGenerator iduGenerator, MarkdownWidget mdWidget) {
		this.view = view;
		this.iduGenerator = iduGenerator;
		this.mdWidget = mdWidget;
		view.add(mdWidget);
		
	}

	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		mdWidget.setLoadingVisible(true);
		String arId = descriptor.get(ACCESS_RESTRICTION_ID).trim();
		iduGenerator.gatherAllSubmissions(arId, md -> {
			mdWidget.configure(md);
		});
		descriptor = widgetDescriptor;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
