package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.TeamHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationSubmitterViewImpl implements EvaluationSubmitterView {
	public interface EvaluationSubmitterViewImplUiBinder extends UiBinder<Widget, EvaluationSubmitterViewImpl> {}
	private Presenter presenter;
	private EvaluationList evaluationList;
	private EntityFinder entityFinder;
	private boolean showEntityFinder;
	private Reference selectedReference;
	
	Widget widget;
	@UiField
	Modal modal1;
	@UiField
	Modal modal2;
	@UiField
	Button nextButton;
	@UiField
	Button okButton;
	@UiField
	Button entityFinderButton;
	@UiField
	SimplePanel evaluationListContainer;
	@UiField
	TextBox submissionNameField;
	@UiField
	Heading selectedText;
	@UiField
	FormGroup entityFinderUI;
	@UiField
	Select teamComboBox;
	@UiField
	Radio isIndividualRadioButton;
	
	@Inject
	public EvaluationSubmitterViewImpl(EvaluationSubmitterViewImplUiBinder binder, EntityFinder entityFinder, EvaluationList evaluationList) {
		widget = binder.createAndBindUi(this);
		this.entityFinder = entityFinder;
		this.evaluationList = evaluationList;
		evaluationListContainer.setWidget(evaluationList.asWidget());
		initClickHandlers();
	}
	
	public void initClickHandlers() {
		nextButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Evaluation evaluation = evaluationList.getSelectedEvaluation();
				if (evaluation != null) {
					if (showEntityFinder) {
						if (selectedReference == null || selectedReference.getTargetId() == null) {
							//invalid, return.
							showErrorMessage(DisplayConstants.NO_ENTITY_SELECTED);
							return;
						}
					}
					presenter.nextClicked(selectedReference, submissionNameField.getValue(), evaluation);
				} else {
					showErrorMessage(DisplayConstants.NO_EVALUATION_SELECTED);
				}
			}
		});
		
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.doneClicked(teamComboBox.getValue());
			}
		});
		
		entityFinderButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				entityFinder.configure(true, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						if(selected.getTargetId() != null) {					
							selectedReference = selected;
							selectedText.setText(DisplayUtils.createEntityVersionString(selected));
							entityFinder.hide();
						} else {
							showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});
				entityFinder.show();
			}
		});
	}
	
	@Override
	public boolean isIndividual() {
		return isIndividualRadioButton.isActive();
	}
	
	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public void showLoading() {
	}
	
	@Override
	public void clear() {
		selectedReference = null;
		selectedText.setText("");
	}
	
	@Override
	public void showSubmissionAcceptedDialogs(String message) {
		DisplayUtils.showInfoDialog(DisplayConstants.THANK_YOU_FOR_SUBMISSION, SafeHtmlUtils.htmlEscape(message), null);
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void showModal1(boolean showEntityFinder, List<Evaluation> evaluations) {
		clear();
		entityFinderUI.setVisible(showEntityFinder);
		evaluationList.configure(evaluations);
        this.showEntityFinder = showEntityFinder;
        modal1.show();
	}
	
	
	@Override
	public void hideModal1() {
		modal1.hide();	
	}
	
	@Override
	public void showModal2(List<TeamHeader> availableTeams) {
		teamComboBox.clear();
		isIndividualRadioButton.setActive(true);
		
		for (TeamHeader teamHeader : availableTeams) {
			Option teamOption = new Option();
			teamOption.setText(teamHeader.getName());
			teamComboBox.add(teamOption);
		}
		modal2.show();
	}
	@Override
	public void hideModal2() {
		modal2.hide();
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
}
