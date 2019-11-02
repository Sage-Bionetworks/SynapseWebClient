package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableQueryResultWikiEditor implements WidgetEditorPresenter {

	private TableQueryResultWikiView view;
	private Map<String, String> descriptor;

	@Inject
	public TableQueryResultWikiEditor(TableQueryResultWikiView view) {
		this.view = view;
		view.initView();
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		String sql = descriptor.get(WidgetConstants.TABLE_QUERY_KEY);
		if (sql != null)
			view.setSql(sql);
		boolean isQueryVisible = true;
		String isQueryVisibleString = descriptor.get(WidgetConstants.QUERY_VISIBLE);
		if (isQueryVisibleString != null) {
			isQueryVisible = Boolean.parseBoolean(isQueryVisibleString);
		}
		view.setQueryVisible(isQueryVisible);
	}

	public void clearState() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		// update widget descriptor from the view
		String sql = view.getSql();
		if (!DisplayUtils.isDefined(sql)) {
			throw new IllegalArgumentException("Query is required");
		}
		descriptor.put(WidgetConstants.TABLE_QUERY_KEY, sql);
		descriptor.put(WidgetConstants.QUERY_VISIBLE, view.isQueryVisible().toString());
	}


	@Override
	public String getTextToInsert() {
		return null;
	}

	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}

	@Override
	public List<String> getDeletedFileHandleIds() {
		return null;
	}
	/*
	 * Private Methods
	 */
}
