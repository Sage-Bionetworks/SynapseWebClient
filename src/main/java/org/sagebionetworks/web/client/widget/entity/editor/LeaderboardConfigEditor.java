package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class LeaderboardConfigEditor implements WidgetEditorPresenter {
	
	QueryTableConfigEditor queryTableConfigEditor;
	@Inject
	public LeaderboardConfigEditor(QueryTableConfigEditor queryTableConfigEditor) {
		this.queryTableConfigEditor = queryTableConfigEditor;
		queryTableConfigEditor.setServicePrefix(ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
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
