package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationSubmissionConfigViewImpl implements EvaluationSubmissionConfigView {
	public interface EvaluationSubmissionConfigViewImplUiBinder extends UiBinder<Widget, EvaluationSubmissionConfigViewImpl> {}
	@UiField
	TextBox challengeProjectField;
	@UiField
	TextBox evaluationQueueIdField;
	@UiField
	TextBox unavailableMessageField;
	@UiField
	TextBox buttonTextField;
	@UiField
	Radio challengeRadioOption;
	@UiField
	Radio evaluationQueueOption;
	@UiField
	Div challengeProjectUi;
	@UiField
	Div evaluationQueueUi;
	@UiField
	Button findProjectButton;
	
	// form-based submission params
	@UiField
	Radio submitEntityOption;
	@UiField
	Radio submitForm;
	@UiField
	Div formUi;
	@UiField
	TextBox formContainerId;
	@UiField
	Button findFormContainerButton;
	@UiField
	TextBox schemaFileSynIdField;
	@UiField
	Button findSchemaFileButton;
	@UiField
	TextBox uiSchemaFileSynIdField;
	@UiField
	Button findUiSchemaFileButton;

	Widget widget;
	
	EntityFinder entityFinder;
	
	@Inject
	public EvaluationSubmissionConfigViewImpl(EvaluationSubmissionConfigViewImplUiBinder binder,
			EntityFinder entityFinder) {
		widget = binder.createAndBindUi(this);
		this.entityFinder = entityFinder;
		
		findProjectButton.addClickHandler(event-> {
			entityFinder.configure(EntityFilter.PROJECT, false, selectedRef -> {
				challengeProjectField.setValue(selectedRef.getTargetId());
				entityFinder.hide();
			});
			entityFinder.show();
		});
		
		findFormContainerButton.addClickHandler(event-> {
			entityFinder.configure(EntityFilter.CONTAINER, false, selectedRef -> {
				formContainerId.setValue(selectedRef.getTargetId());
				entityFinder.hide();
			});
			entityFinder.show();
		});
		findSchemaFileButton.addClickHandler(event-> {
			entityFinder.configure(EntityFilter.FILE, false, selectedRef -> {
				schemaFileSynIdField.setValue(selectedRef.getTargetId());
				entityFinder.hide();
			});
			entityFinder.show();
		});
		findUiSchemaFileButton.addClickHandler(event-> {
			entityFinder.configure(EntityFilter.FILE, false, selectedRef -> {
				uiSchemaFileSynIdField.setValue(selectedRef.getTargetId());
				entityFinder.hide();
			});
			entityFinder.show();
		});
		
		challengeRadioOption.addClickHandler(event -> {
			setChallengeProjectUIVisible(true);
		});
		evaluationQueueOption.addClickHandler(event -> {
			setChallengeProjectUIVisible(false);
		});
		submitForm.addClickHandler(event -> {
			formUi.setVisible(true);
		});
		submitEntityOption.addClickHandler(event -> {
			formUi.setVisible(false);
		});
	}
	
	private void setChallengeProjectUIVisible(boolean visible) {
		challengeProjectUi.setVisible(visible);
		evaluationQueueUi.setVisible(!visible);
	}
	
	@Override
	public void initView() {
		clear();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> descriptor) {
		String text = descriptor.get(WidgetConstants.UNAVAILABLE_MESSAGE);
		if (text != null)
			unavailableMessageField.setValue(text);
		String projectId = descriptor.get(WidgetConstants.PROJECT_ID_KEY);
		if (projectId != null) {
			challengeProjectField.setValue(projectId);
			challengeRadioOption.setValue(true);
			setChallengeProjectUIVisible(true);
		}
		String evalId = descriptor.get(WidgetConstants.JOIN_WIDGET_EVALUATION_ID_KEY);
		if (evalId != null) {
			evaluationQueueIdField.setValue(evalId);
			evaluationQueueOption.setValue(true);
			setChallengeProjectUIVisible(false);
		}
		
		text = descriptor.get(WidgetConstants.BUTTON_TEXT_KEY);
		if (text != null)
			buttonTextField.setValue(text);
	}

	@Override
	public void checkParams() throws IllegalArgumentException {
		if (challengeRadioOption.getValue() && "".equals(challengeProjectField.getValue()))
			throw new IllegalArgumentException(DisplayConstants.ERROR_SELECT_CHALLENGE_PROJECT);
		if (evaluationQueueOption.getValue() && "".equals(evaluationQueueIdField.getValue()))
			throw new IllegalArgumentException(DisplayConstants.ERROR_SET_EVALUATION_QUEUE_ID);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}	
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
		challengeProjectField.setValue("");
	}

	@Override
	public String getButtonText() {
		return buttonTextField.getValue();
	}
	@Override
	public String getChallengeProjectId() {
		return challengeProjectField.getValue();
	}
	@Override
	public String getUnavailableMessage() {
		return unavailableMessageField.getValue();
	}
	@Override
	public String getEvaluationQueueId() {
		return evaluationQueueIdField.getValue();
	}
	@Override
	public boolean isChallengeProjectIdSelected() {
		return challengeRadioOption.getValue();
	}
}
