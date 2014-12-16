package org.sagebionetworks.web.client.widget.entity;

import java.util.HashSet;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
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
	Modal modal;
	
	@UiField
	Button okButton;
	@UiField
	Button entityFinderButton;
	@UiField
	SimplePanel evaluationListContainer;
	@UiField
	TextBox submissionNameField;
	@UiField
	TextBox teamNameField;
	@UiField
	Heading selectedText;
	@UiField
	FormGroup entityFinderUI;
	
	@Inject
	public EvaluationSubmitterViewImpl(EvaluationSubmitterViewImplUiBinder binder, EntityFinder entityFinder, EvaluationList evaluationList) {
		modal = (Modal)binder.createAndBindUi(this);
		this.entityFinder = entityFinder;
		this.evaluationList = evaluationList;
		
		evaluationListContainer.setWidget(evaluationList.asWidget());
		initClickHandlers();
	}
	
	public void initClickHandlers() {
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				List<Evaluation> evaluations = evaluationList.getSelectedEvaluations();
				if (evaluations.size() > 0) {
					if (showEntityFinder) {
						if (selectedReference == null || selectedReference.getTargetId() == null) {
							//invalid, return.
							showErrorMessage(DisplayConstants.NO_ENTITY_SELECTED);
							return;
						}
					}
					presenter.submitToEvaluations(selectedReference, submissionNameField.getValue(), teamNameField.getValue(), evaluations);
				} else {
					showErrorMessage(DisplayConstants.NO_EVALUATION_SELECTED);
				}
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
	public void showSubmissionAcceptedDialogs(HashSet<String> receiptMessages) {
		for (String message : receiptMessages) {
			DisplayUtils.showInfoDialog(DisplayConstants.THANK_YOU_FOR_SUBMISSION, SafeHtmlUtils.htmlEscape(message), null);
		}
	}
	
	@Override
	public void showAccessRequirement(String arText, final Callback touAcceptanceCallback) {
		final Dialog dialog = new Dialog();
       	dialog.setMaximizable(false);
        dialog.setSize(640, 480);
        dialog.setPlain(true); 
        dialog.setModal(true); 
        dialog.setAutoHeight(true);
        dialog.setResizable(false);
        ScrollPanel panel = new ScrollPanel(new HTML(arText));
        panel.addStyleName("margin-top-left-10");
        panel.setSize("605px", "450px");
        dialog.add(panel);
 		dialog.setHeading("Terms of Use");
		// agree to TOU, cancel
        dialog.okText = DisplayConstants.BUTTON_TEXT_ACCEPT_TERMS_OF_USE;
        dialog.setButtons(Dialog.OKCANCEL);
        com.extjs.gxt.ui.client.widget.button.Button touButton = dialog.getButtonById(Dialog.OK);
        touButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				touAcceptanceCallback.invoke();
			}
        });
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
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
	public void popupSelector(boolean showEntityFinder, List<Evaluation> evaluations) {
		clear();
		entityFinderUI.setVisible(showEntityFinder);
		evaluationList.configure(evaluations);
        this.showEntityFinder = showEntityFinder;
        modal.show();
	}
	
	
	@Override
	public void hideWindow() {
		modal.hide();	
	}
}
