package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LeaderboardConfigEditor implements WidgetEditorPresenter {

	public static final String LEADERBOARD_QUERY_PLACEHOLDER = "select objectId, createdOn, entityId, team, annotation1 from evaluation_12345";

	QueryTableConfigEditor queryTableConfigEditor;

	@Inject
	public LeaderboardConfigEditor(QueryTableConfigEditor queryTableConfigEditor) {
		this.queryTableConfigEditor = queryTableConfigEditor;
		queryTableConfigEditor.setServicePrefix(ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX);
		queryTableConfigEditor.setQueryPlaceholder(LEADERBOARD_QUERY_PLACEHOLDER);
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		widgetDescriptor.put(WidgetConstants.API_TABLE_WIDGET_QUERY_TABLE_RESULTS, Boolean.TRUE.toString());
		queryTableConfigEditor.configure(wikiKey, widgetDescriptor, dialogCallback);
	}

	public void clearState() {
		queryTableConfigEditor.clearState();
	}

	@Override
	public Widget asWidget() {
		return queryTableConfigEditor.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		queryTableConfigEditor.updateDescriptorFromView();
	}


	@Override
	public String getTextToInsert() {
		return queryTableConfigEditor.getTextToInsert();
	}

	@Override
	public List<String> getNewFileHandleIds() {
		return queryTableConfigEditor.getNewFileHandleIds();
	}

	@Override
	public List<String> getDeletedFileHandleIds() {
		return queryTableConfigEditor.getDeletedFileHandleIds();
	}
	/*
	 * Private Methods
	 */
}
