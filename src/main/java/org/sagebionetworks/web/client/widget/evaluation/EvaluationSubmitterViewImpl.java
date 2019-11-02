package org.sagebionetworks.web.client.widget.evaluation;

import java.util.List;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.InlineCheckBox;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.entity.RegisterTeamDialog;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.FormParams;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationSubmitterViewImpl implements EvaluationSubmitterView {
	public interface Binder extends UiBinder<Widget, EvaluationSubmitterViewImpl> {
	}

	private Presenter presenter;
	private EvaluationList evaluationList;
	private EntityFinder entityFinder;
	private boolean showEntityFinder;
	private Reference selectedReference;
	AuthenticationController authController;
	boolean isForm;
	JavaScriptObject formRef;

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
	ListBox teamComboBox;
	@UiField
	Radio isIndividualRadioButton;
	@UiField
	Radio isTeamRadioButton;
	@UiField
	Panel contributorsPanel;
	@UiField
	SimplePanel registerTeamDialogContainer;
	@UiField
	Anchor registerMyTeamLink;
	@UiField
	Anchor registerMyTeamLink2;
	@UiField
	Paragraph teamIneligibleHtml;
	@UiField
	LoadingSpinner contributorsLoadingUI;
	@UiField
	Div teamsUI;
	@UiField
	Div availableTeamsUI;
	@UiField
	Div emptyTeamsUI;
	@UiField
	SimplePanel challengeListSynAlertPanel;
	@UiField
	SimplePanel teamSelectSynAlertPanel;
	@UiField
	SimplePanel contributorSynAlertPanel;
	@UiField
	SimplePanel dockerCommitListSynAlertPanel;
	@UiField
	FormGroup evaluationListUI;
	@UiField
	Modal dockerCommitModal;
	@UiField
	SimplePanel dockerCommitListContainer;
	@UiField
	Button dockerCommitNextButton;
	@UiField
	FormLabel multipleEvaluationsFormLabel;
	@UiField
	FormLabel singleEvaluationFormLabel;
	@UiField
	FormGroup submissionNameUi;
	@UiField
	Div formDiv;

	private PortalGinInjector ginInjector;

	@Inject
	public EvaluationSubmitterViewImpl(Binder binder, EntityFinder entityFinder, EvaluationList evaluationList, PortalGinInjector ginInjector, AuthenticationController authController) {
		widget = binder.createAndBindUi(this);
		this.entityFinder = entityFinder;
		this.evaluationList = evaluationList;
		this.ginInjector = ginInjector;
		this.authController = authController;
		evaluationListContainer.setWidget(evaluationList.asWidget());
		initClickHandlers();
		widget.addAttachHandler(event -> {
			if (!event.isAttached()) {
				// detach event, clean up react component
				ginInjector.getSynapseJSNIUtils().unmountComponentAtNode(formDiv.getElement());
			}
		});
	}

	public void initClickHandlers() {
		submissionNameField.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					nextButton.click();
				}
			}
		});
		dockerCommitNextButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.onDockerCommitNextButton();
			}
		});
		nextButton.addClickHandler(event -> {
			Evaluation evaluation = evaluationList.getSelectedEvaluation();
			if (evaluation != null) {
				if (showEntityFinder) {
					if (selectedReference == null || selectedReference.getTargetId() == null) {
						// invalid, return.
						showErrorMessage(DisplayConstants.NO_ENTITY_SELECTED);
						return;
					}
				}
				if (isForm) {
					// ask the form to submit, and wait for the callback (to go to the next page)
					_submitForm(formRef);
				} else {
					presenter.onNextClicked(selectedReference, submissionNameField.getValue(), evaluation);
				}

			} else {
				showErrorMessage(DisplayConstants.NO_EVALUATION_SELECTED);
			}
		});

		okButton.addClickHandler(event -> {
			presenter.onDoneClicked();
		});

		entityFinderButton.addClickHandler(event -> {
			entityFinder.configure(true, new SelectedHandler<Reference>() {
				@Override
				public void onSelected(Reference selected) {
					if (selected.getTargetId() != null) {
						selectedReference = selected;
						selectedText.setText(DisplayUtils.createEntityVersionString(selected));
						entityFinder.hide();
					} else {
						showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
					}
				}
			});
			entityFinder.show();
		});

		ClickHandler registerTeamLink = event -> {
			presenter.onRegisterTeamClicked();
		};
		registerMyTeamLink.addClickHandler(registerTeamLink);
		registerMyTeamLink2.addClickHandler(registerTeamLink);

		isIndividualRadioButton.addClickHandler(event -> {
			presenter.onIndividualSubmissionOptionClicked();
		});

		isTeamRadioButton.addClickHandler(event -> {
			presenter.onTeamSubmissionOptionClicked();
		});

		teamComboBox.addChangeHandler(event -> {
			presenter.onTeamSelected(teamComboBox.getSelectedIndex());
		});
	}

	@Override
	public void setNextButtonLoading() {
		nextButton.state().loading();
	}

	@Override
	public void resetNextButton() {
		nextButton.state().reset();
	}

	@Override
	public void resetSubmitButton() {
		okButton.state().reset();
	}

	@Override
	public void setSubmitButtonLoading() {
		okButton.state().loading();
	}

	@Override
	public void showRegisterTeamDialog(String challengeId) {
		RegisterTeamDialog dialog = ginInjector.getRegisterTeamDialog();
		registerTeamDialogContainer.setWidget(dialog.asWidget());

		dialog.configure(challengeId, new Callback() {
			@Override
			public void invoke() {
				presenter.refreshRegisteredTeams();
			}
		});
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public void showLoading() {}

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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
		resetNextButton();
		resetSubmitButton();
	}

	@Override
	public void setDockerCommitList(Widget widget) {
		dockerCommitListContainer.setWidget(widget);
	}

	@Override
	public void showDockerCommitModal() {
		dockerCommitModal.show();
	}

	@Override
	public void hideDockerCommitModal() {
		dockerCommitModal.hide();
	}

	@Override
	public void showModal1(boolean isEntitySet, FormParams formParams, List<Evaluation> evaluations) {
		clear();
		isForm = formParams != null;
		this.showEntityFinder = !isEntitySet && !isForm;
		submissionNameUi.setVisible(!isForm);
		entityFinderUI.setVisible(showEntityFinder);
		evaluationList.configure(evaluations);
		boolean isMoreThanOneEvaluation = evaluations.size() > 1;
		multipleEvaluationsFormLabel.setVisible(isMoreThanOneEvaluation);
		singleEvaluationFormLabel.setVisible(!isMoreThanOneEvaluation);
		if (isForm) {
			// here we go! add the SRC entityform component to the formDiv (and listen for detach for cleanup)
			// going to need some space
			modal1.addStyleName("modal-fullscreen");
			String sessionToken = authController.getCurrentUserSessionToken();
			_showForm(formDiv.getElement(), sessionToken, formParams.getContainerSynId(), formParams.getJsonSchemaSynId(), formParams.getUiSchemaSynId(), this);
		} else {
			modal1.removeStyleName("modal-fullscreen");
		}
		modal1.show();
	}

	public void setFormSynId(String synId) {
		Evaluation evaluation = evaluationList.getSelectedEvaluation();
		selectedReference = new Reference();
		selectedReference.setTargetId(synId);
		presenter.onNextClicked(selectedReference, submissionNameField.getValue(), evaluation);
	}

	private static native void _submitForm(JavaScriptObject formRef) /*-{
		formRef.submitForm();
	}-*/;

	private static native void _showForm(Element el, String sessionToken, String parentContainerSynId, String jsonSchemaSynId, String uiSchemaSynId, EvaluationSubmitterViewImpl view) /*-{
		try {
			view.@org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitterViewImpl::formRef = $wnd.React
					.createRef();
			function setRefFunction(form) {
				view.@org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitterViewImpl::formRef = form;
			}
			;
			function synIdCallbackFunction(synId) {
				view.@org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitterViewImpl::setFormSynId(Ljava/lang/String;)(synId);
			}
			;
			var initializeFormData = false;
			var props = {
				parentContainerId : parentContainerSynId,
				token : sessionToken,
				formSchemaEntityId : jsonSchemaSynId,
				formUiSchemaEntityId : uiSchemaSynId,
				initFormData : initializeFormData,
				ref : setRefFunction,
				synIdCallback : synIdCallbackFunction
			};
			$wnd.ReactDOM.render($wnd.React.createElement(
					$wnd.SRC.SynapseComponents.EntityForm, props, null), el);
		} catch (err) {
			console.error(err);
		}
	}-*/;

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
		// also add the reason
		Span span = new Span();
		span.addStyleName("greyText-imp margin-left-5");
		span.setText(reason);
		row.add(span);

		contributorsPanel.add(row);
	}

	@Override
	public void setTeamInEligibleError(String error) {
		teamIneligibleHtml.setText(error);
	}

	private Div getContributorRow(String principalId, boolean selectCheckbox) {
		Div row = new Div();
		InlineCheckBox cb = new InlineCheckBox();
		cb.addStyleName("moveup-10");
		cb.setValue(selectCheckbox);
		cb.setEnabled(false);
		row.add(cb);

		UserBadge badge = ginInjector.getUserBadgeWidget();
		badge.configure(principalId);
		row.add(badge.asWidget());

		return row;
	}

	@Override
	public void clearTeams() {
		teamComboBox.clear();
	}

	@Override
	public void showEmptyTeams() {
		teamsUI.setVisible(true);
		emptyTeamsUI.setVisible(true);
		availableTeamsUI.setVisible(false);
		registerMyTeamLink2.setVisible(true);
	}

	@Override
	public void showTeamsUI(List<Team> registeredTeams) {
		teamsUI.setVisible(true);
		emptyTeamsUI.setVisible(false);
		availableTeamsUI.setVisible(true);
		registerMyTeamLink2.setVisible(false);
		teamComboBox.clear();
		for (Team teamHeader : registeredTeams) {
			teamComboBox.addItem(teamHeader.getName());
		}
	}

	@Override
	public void setIndividualSubmissionActive() {
		isIndividualRadioButton.setValue(true, true);
		isTeamRadioButton.setValue(false, true);
	}

	@Override
	public void setTeamSubmissionActive() {
		isIndividualRadioButton.setValue(false, true);
		isTeamRadioButton.setValue(true, true);
	}

	@Override
	public void hideTeamsUI() {
		teamsUI.setVisible(false);
	}

	@Override
	public void setContributorsLoading(boolean isVisible) {
		contributorsLoadingUI.setVisible(isVisible);
	}

	@Override
	public void setChallengesSynAlertWidget(Widget synAlert) {
		this.challengeListSynAlertPanel.setWidget(synAlert);
	}

	@Override
	public void setTeamSelectSynAlertWidget(Widget synAlert) {
		this.teamSelectSynAlertPanel.setWidget(synAlert);
	}

	@Override
	public void setContributorsSynAlertWidget(Widget synAlert) {
		this.contributorSynAlertPanel.setWidget(synAlert);
	}

	@Override
	public void setEvaluationListVisible(boolean visible) {
		evaluationListUI.setVisible(visible);
	}

	@Override
	public void setDockerCommitSynAlert(Widget widget) {
		this.dockerCommitListSynAlertPanel.setWidget(widget);
	}
}
