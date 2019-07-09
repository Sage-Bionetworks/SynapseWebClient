package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationSubmissionConfigEditor implements WidgetEditorPresenter {
	private Map<String, String> descriptor;
	private EvaluationSubmissionConfigView view;
	
	@Inject
	public EvaluationSubmissionConfigEditor(EvaluationSubmissionConfigView view) {
		this.view = view;
		view.initView();
	}
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		view.configure(wikiKey, widgetDescriptor);
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
		//update widget descriptor from the view
		view.checkParams();
		descriptor.put(WidgetConstants.BUTTON_TEXT_KEY, view.getButtonText());
		if (view.isChallengeProjectIdSelected()) {
			descriptor.put(WidgetConstants.PROJECT_ID_KEY, view.getChallengeProjectId());
			descriptor.remove(WidgetConstants.JOIN_WIDGET_EVALUATION_ID_KEY);
		} else {
			descriptor.remove(WidgetConstants.PROJECT_ID_KEY);
			descriptor.put(WidgetConstants.JOIN_WIDGET_EVALUATION_ID_KEY, view.getEvaluationQueueId());
		}
		descriptor.put(WidgetConstants.UNAVAILABLE_MESSAGE, view.getUnavailableMessage());
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
}
