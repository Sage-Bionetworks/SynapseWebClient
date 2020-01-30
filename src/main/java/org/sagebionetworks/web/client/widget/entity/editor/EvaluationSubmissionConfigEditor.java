package org.sagebionetworks.web.client.widget.entity.editor;

import static org.sagebionetworks.web.shared.WidgetConstants.BUTTON_TEXT_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.EVALUATION_ID_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.FORM_CONTAINER_ID_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.JSON_SCHEMA_ID_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.PROJECT_ID_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.UI_SCHEMA_ID_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.UNAVAILABLE_MESSAGE;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
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
		// update widget descriptor from the view
		view.checkParams();
		descriptor.put(BUTTON_TEXT_KEY, view.getButtonText());
		if (view.isChallengeProjectIdSelected()) {
			descriptor.put(PROJECT_ID_KEY, view.getChallengeProjectId());
			descriptor.remove(EVALUATION_ID_KEY);
		} else {
			descriptor.remove(PROJECT_ID_KEY);
			descriptor.put(EVALUATION_ID_KEY, view.getEvaluationQueueId());
		}
		if (view.isFormSubmission()) {
			descriptor.put(FORM_CONTAINER_ID_KEY, view.getFormContainerId());
			descriptor.put(JSON_SCHEMA_ID_KEY, view.getFormJsonSchemaId());
			descriptor.put(UI_SCHEMA_ID_KEY, view.getFormUiSchemaId());
		} else {
			descriptor.remove(FORM_CONTAINER_ID_KEY);
			descriptor.remove(JSON_SCHEMA_ID_KEY);
			descriptor.remove(UI_SCHEMA_ID_KEY);
		}
		descriptor.put(UNAVAILABLE_MESSAGE, view.getUnavailableMessage());
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
