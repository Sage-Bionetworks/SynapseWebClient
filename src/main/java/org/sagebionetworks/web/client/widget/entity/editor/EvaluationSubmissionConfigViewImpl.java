package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
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
	TextBox unavailableMessageField;
	@UiField
	TextBox buttonTextField;

	@UiField
	Button findProjectButton;
	
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
		if (projectId != null)
			challengeProjectField.setValue(projectId);
		text = descriptor.get(WidgetConstants.BUTTON_TEXT_KEY);
		if (text != null)
			buttonTextField.setValue(text);
	}

	@Override
	public void checkParams() throws IllegalArgumentException {
		if ("".equals(challengeProjectField.getValue()))
			throw new IllegalArgumentException(DisplayConstants.ERROR_SELECT_CHALLENGE_PROJECT);
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
}
