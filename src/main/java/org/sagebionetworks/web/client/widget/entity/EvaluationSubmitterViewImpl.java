package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.InlineCheckBox;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationSubmitterViewImpl implements EvaluationSubmitterView {
	public interface Binder extends UiBinder<Widget, EvaluationSubmitterViewImpl> {}
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
	Div teamComboBoxContainer;
	@UiField
	Radio isIndividualRadioButton;
	@UiField
	Radio isTeamRadioButton;
	@UiField
	Div contributorsPanel;
	@UiField
	SimplePanel registerTeamDialogContainer;
	@UiField
	Anchor registerMyTeamLink;
	@UiField
	Anchor createNewTeamLink;
	@UiField
	Paragraph teamIneligibleHtml;
	@UiField
	Paragraph noTeamsFoundUI;
	
	private PortalGinInjector ginInjector;
	private RegisterTeamDialog registerTeamDialog;
	private Select teamComboBox;
	@Inject
	public EvaluationSubmitterViewImpl(
			Binder binder, 
			EntityFinder entityFinder, 
			EvaluationList evaluationList, 
			PortalGinInjector ginInjector,
			RegisterTeamDialog registerTeamDialog) {
		widget = binder.createAndBindUi(this);
		this.entityFinder = entityFinder;
		this.evaluationList = evaluationList;
		this.ginInjector = ginInjector;
		this.registerTeamDialog = registerTeamDialog;
		
		contributorsPanel.getElement().setAttribute("highlight-box-title", "Contributors");
		evaluationListContainer.setWidget(evaluationList.asWidget());
		registerTeamDialogContainer.setWidget(registerTeamDialog.asWidget());
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
					presenter.onNextClicked(selectedReference, submissionNameField.getValue(), evaluation);
				} else {
					showErrorMessage(DisplayConstants.NO_EVALUATION_SELECTED);
				}
			}
		});
		
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onDoneClicked();
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
		
		registerMyTeamLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRegisterTeamClicked();
			}
		});
		
		createNewTeamLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onNewTeamClicked();
			}
		});
		isIndividualRadioButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onIndividualSubmissionOptionClicked();
			}
		});
		isTeamRadioButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onTeamSubmissionOptionClicked();
			}
		});
	}
	
	@Override
	public void showRegisterTeamDialog(String challengeId) {
		registerTeamDialog.configure(challengeId, new Callback() {
			@Override
			public void invoke() {
				presenter.teamAdded();
			}
		});
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
	public void showModal2() {
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
	
	@Override
	public void setContributorsListVisible(boolean isVisible) {
		contributorsPanel.setVisible(isVisible);
	}

	@Override
	public void clearContributors() {
		contributorsPanel.clear();
	}
	
	@Override
	public void addEligibleContributor(String principalId) {
		contributorsPanel.add(getContributorRow(principalId, true));
	}
	
	@Override
	public void addInEligibleContributor(String principalId, String reason) {
		Div row = getContributorRow(principalId, false);
		//also add the reason
		Span span = new Span();
		span.addStyleName("greyText-imp");
		span.setText(reason);
		row.add(span);
		
		contributorsPanel.add(row);
	}
	
	@Override
	public void setTeamInEligibleErrorVisible(boolean isVisible, String error) {
		teamIneligibleHtml.setVisible(isVisible);
		teamIneligibleHtml.setText(error);
	}
	
	private Div getContributorRow(String principalId, boolean selectCheckbox) {
		Div row = new Div();
		InlineCheckBox cb = new InlineCheckBox();
		cb.setValue(selectCheckbox);
		cb.setEnabled(false);
		row.add(cb);
		
		UserBadge badge = ginInjector.getUserBadgeWidget();
		badge.configure(principalId);
		row.add(badge.asWidget());
		
		return row;
	}
	
	@Override
	public void setTeamComboBoxEnabled(boolean isEnabled) {
		teamComboBox.setEnabled(isEnabled);
	}
	
	@Override
	public void clearTeams() {
		teamComboBoxContainer.clear();
		teamComboBox = new Select();
	}
	
	@Override
	public void showEmptyTeams() {
		noTeamsFoundUI.setVisible(true);
	}
	
	@Override
	public void showTeams(List<Team> registeredTeams) {
		noTeamsFoundUI.setVisible(false);
		teamComboBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				presenter.onTeamSelected(teamComboBox.getValue());
			}
		});
		
		isIndividualRadioButton.setActive(true);
		
		for (Team teamHeader : registeredTeams) {
			Option teamOption = new Option();
			teamOption.setText(teamHeader.getName());
			teamComboBox.add(teamOption);
		}
		teamComboBoxContainer.add(teamComboBox);
	}
}
