package org.sagebionetworks.web.client.widget.evaluation;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormControlStatic;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.SubmissionQuota;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class EvaluationRowWidget implements IsWidget {
	@UiField
	Text evaluationNameText;
	@UiField
	Button shareButton;
	@UiField
	Button editButton;
	@UiField
	Button deleteButton;
	@UiField
	FormControlStatic descriptionField;
	@UiField
	FormControlStatic submissionInstructionsField;
	@UiField
	FormControlStatic submissionReceiptField;
	@UiField
	FormControlStatic createdOnDiv;
	@UiField
	Div createdByDiv;
	
	@UiField
	Panel quotaUI;
	@UiField
	FormControlStatic roundStart;
	@UiField
	FormControlStatic submissionLimitField;
	@UiField
	FormControlStatic numberOfRoundsField;
	@UiField
	FormControlStatic roundDurationField;
	
	Widget widget;
	UserBadge userBadge;
	
	public interface Binder extends UiBinder<Widget, EvaluationRowWidget> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	private Evaluation evaluation;
	private EvaluationActionHandler handler;
	private DateTimeUtils dateTimeUtils;
	
	public interface EvaluationActionHandler {
		void onEditClicked(Evaluation evaluation);
		void onShareClicked(Evaluation evaluation);
		void onDeleteClicked(Evaluation evaluation);
	}
	
	@Inject
	public EvaluationRowWidget(UserBadge userBadge, DateTimeUtils dateTimeUtils) {
		this.userBadge = userBadge;
		this.dateTimeUtils= dateTimeUtils;
		widget = uiBinder.createAndBindUi(this);
		shareButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handler.onShareClicked(evaluation);
			}
		});
		editButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handler.onEditClicked(evaluation);
			}
		});
		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.showConfirmDialog("Delete Evaluation Queue?", DisplayConstants.CONFIRM_DELETE_EVAL_QUEUE + evaluation.getName(), 
					new Callback() {
						@Override
						public void invoke() {
							handler.onDeleteClicked(evaluation);
						}
					});
			}
		});
		createdByDiv.add(userBadge);
	}
	
	public void configure(Evaluation evaluation, EvaluationActionHandler handler) {
		this.evaluation = evaluation;
		this.handler = handler;
		String evaluationText = evaluation.getName() + " (" + evaluation.getId() + ")";
		evaluationNameText.setText(evaluationText);
		userBadge.configure(evaluation.getOwnerId());
		descriptionField.setText(evaluation.getDescription());
		submissionInstructionsField.setText(evaluation.getSubmissionInstructionsMessage());
		submissionReceiptField.setText(evaluation.getSubmissionReceiptMessage());
		createdOnDiv.setText(dateTimeUtils.getDateString(evaluation.getCreatedOn()));
		SubmissionQuota quota = evaluation.getQuota();
		quotaUI.setVisible(quota != null);
		if (quota != null) {
			roundStart.setText(dateTimeUtils.getDateString(quota.getFirstRoundStart()));
			submissionLimitField.setText(quota.getSubmissionLimit().toString());
			numberOfRoundsField.setText(quota.getNumberOfRounds().toString());
			roundDurationField.setText(quota.getRoundDurationMillis().toString());
		}
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
}
