package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadge;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListWidget implements WidgetRendererPresenter {
	
	private EntityListWidgetView view;
	private Map<String, String> descriptor;
	private PortalGinInjector portalGinInjector;
	
	@Inject
	public EntityListWidget(EntityListWidgetView view,
			PortalGinInjector portalGinInjector) {
		this.view = view;		
		this.portalGinInjector = portalGinInjector;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey,  Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		if (widgetDescriptor == null) throw new IllegalArgumentException("Descriptor can not be null");
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		
		boolean showDescription = true;
		if (descriptor.containsKey(WidgetConstants.ENTITYLIST_WIDGET_SHOW_DESCRIPTION_KEY)) {
			showDescription = Boolean.parseBoolean(descriptor.get(WidgetConstants.ENTITYLIST_WIDGET_SHOW_DESCRIPTION_KEY));
		}
		view.clearRows();
		
		List<EntityGroupRecord> records = EntityListUtil.parseRecords(descriptor.get(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY));
		if(records != null && records.size() > 0) {
			view.setTableVisible(true);
			view.setEmptyUiVisible(false);
			view.setDescriptionHeaderVisible(showDescription);
			for (EntityGroupRecord entityGroupRecord : records) {
				EntityListRowBadge badge = portalGinInjector.getEntityListRowBadge();
				badge.configure(entityGroupRecord.getEntityReference());
				badge.setDescriptionVisible(showDescription);
				badge.setNote(entityGroupRecord.getNote());
				view.addRow(badge.asWidget());
			}
		} else {
			view.setTableVisible(false);
			view.setEmptyUiVisible(true);
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
