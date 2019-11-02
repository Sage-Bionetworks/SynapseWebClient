package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SelectableItemList;
import org.sagebionetworks.web.client.widget.SelectableListItem;
import org.sagebionetworks.web.client.widget.SelectableListView;
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
	boolean isSelectable, showDescription;
	Callback selectionChangedCallback;
	SelectableListView selectionView;
	SelectableItemList selectableItemList;
	Callback refreshCallback;

	@Inject
	public EntityListWidget(EntityListWidgetView view, PortalGinInjector portalGinInjector) {
		this.view = view;
		this.portalGinInjector = portalGinInjector;
		isSelectable = false;
		selectableItemList = new SelectableItemList();
		refreshCallback = new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		};
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		if (widgetDescriptor == null)
			throw new IllegalArgumentException("Descriptor can not be null");
		// set up view based on descriptor parameters
		descriptor = widgetDescriptor;

		showDescription = true;
		if (descriptor.containsKey(WidgetConstants.ENTITYLIST_WIDGET_SHOW_DESCRIPTION_KEY)) {
			showDescription = Boolean.parseBoolean(descriptor.get(WidgetConstants.ENTITYLIST_WIDGET_SHOW_DESCRIPTION_KEY));
		}
		view.setDescriptionHeaderVisible(showDescription);
		List<EntityGroupRecord> records = EntityListUtil.parseRecords(descriptor.get(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY));
		selectableItemList.clear();
		view.clearRows();
		if (records != null && records.size() > 0) {
			view.setTableVisible(true);
			view.setEmptyUiVisible(false);
			for (EntityGroupRecord entityGroupRecord : records) {
				addRecord(entityGroupRecord);
			}
		} else {
			view.setTableVisible(false);
			view.setEmptyUiVisible(true);
		}
		selectableItemList.checkSelectionState();
	}

	public void setSelectionChangedCallback(Callback selectionChangedCallback) {
		this.selectionChangedCallback = selectionChangedCallback;
	}

	public void addRecord(EntityGroupRecord entityGroupRecord) {
		EntityListRowBadge badge = portalGinInjector.getEntityListRowBadge();
		badge.configure(entityGroupRecord.getEntityReference());
		badge.setDescriptionVisible(showDescription);
		badge.setNote(entityGroupRecord.getNote());
		badge.setIsSelectable(isSelectable);
		badge.setSelectionChangedCallback(new Callback() {
			@Override
			public void invoke() {
				selectableItemList.checkSelectionState();
				if (selectionChangedCallback != null) {
					selectionChangedCallback.invoke();
				}
			}
		});
		view.addRow(badge.asWidget());
		selectableItemList.add(badge);
		view.setTableVisible(true);
		view.setEmptyUiVisible(false);
	}

	public void setSelectable(SelectableListView selectionView) {
		isSelectable = true;
		this.selectionView = selectionView;
		selectableItemList.configure(refreshCallback, selectionView);
		selectionView.setSelectionToolbarHandler(selectableItemList);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public SelectableItemList getRowWidgets() {
		return selectableItemList;
	}

	public void refresh() {
		view.clearRows();
		for (SelectableListItem badge : selectableItemList) {
			view.addRow(((EntityListRowBadge) badge).asWidget());
		}
	}
}
