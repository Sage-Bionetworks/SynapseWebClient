package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.BlockQuote;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessRequirementDialogViewImpl implements AccessRequirementDialogView {
	
	public interface Binder extends UiBinder<Widget, AccessRequirementDialogViewImpl> {}
	
	Presenter presenter;
	@UiField
	Div controlledUseUI;
	@UiField
	Div noneUI;
	@UiField
	Div openUI;
	
	@UiField
	Div approvedHeading;
	@UiField
	Div touHeading;
	@UiField
	Div actHeading;
	@UiField
	SimplePanel wikiTermsUI;
	@UiField
	BlockQuote termsUI;
	@UiField
	HTML terms;
	@UiField
	Div anonymousAccessNote;
	@UiField
	Div imposeRestrictionsAllowedNote;
	@UiField
	Div imposeRestrictionsNotAllowedNote;
	@UiField
	Div anonymousFlagNote;
	@UiField
	Div imposeRestrictionsNotAllowedFlagNote;
	@UiField
	Anchor jiraFlagLink;

	//buttons
	@UiField
	Button cancelButton;
	@UiField
	Button closeButton;
	@UiField
	Button loginButton;
	@UiField
	Button imposeRestrictionsButton;
	@UiField
	Button signTermsButton;
	@UiField
	Button requestAccessFromACTButton;
	
	
	//this UI widget
	Modal widget;
	
	@Inject
	public AccessRequirementDialogViewImpl(Binder binder) {
		this.widget = (Modal)binder.createAndBindUi(this);
		
		imposeRestrictionsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.imposeRestrictionClicked();
			}
		});
		
		signTermsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.signTermsOfUseClicked();
			}
		});
		
		requestAccessFromACTButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.requestACTClicked();
			}
		});
		
		jiraFlagLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.flagClicked();
			}
		});
		
		loginButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.loginClicked();
			}
		});
		
		ClickHandler onCancel = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.cancelClicked();
			}
		};
		cancelButton.addClickHandler(onCancel);
		closeButton.addClickHandler(onCancel);
	}
	
	

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter=presenter;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void open(String url) {
		Window.open(url, "_blank", "");	
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
	public void showLoading() {
	}
	
	
	@Override
	public void showModal() {
		widget.show();
	}
	@Override
	public void hideModal() {
		widget.hide();
	}
	
	@Override
	public void clear() {
		//hide everything
		controlledUseUI.setVisible(false);
		noneUI.setVisible(false);
		openUI.setVisible(false);
		approvedHeading.setVisible(false);
		touHeading.setVisible(false);
		actHeading.setVisible(false);
		anonymousAccessNote.setVisible(false);
		termsUI.setVisible(false);
		wikiTermsUI.setVisible(false);
		imposeRestrictionsAllowedNote.setVisible(false);
		imposeRestrictionsNotAllowedNote.setVisible(false);
		anonymousFlagNote.setVisible(false);
		imposeRestrictionsNotAllowedFlagNote.setVisible(false);
		cancelButton.setVisible(false);
		closeButton.setVisible(false);
		loginButton.setVisible(false);
		imposeRestrictionsButton.setVisible(false);
		signTermsButton.setVisible(false);
		requestAccessFromACTButton.setVisible(false);
	}
	
	@Override
	public void showActHeading() {
		actHeading.setVisible(true);
	}
	
	@Override
	public void showAnonymousAccessNote() {
		anonymousAccessNote.setVisible(true);
	}
	
	@Override
	public void showAnonymousFlagNote() {
		anonymousFlagNote.setVisible(true);
	}
	
	@Override
	public void showApprovedHeading() {
		approvedHeading.setVisible(true);
	}
	
	@Override
	public void showCancelButton() {
		cancelButton.setVisible(true);
	}
	
	@Override
	public void showCloseButton() {
		closeButton.setVisible(true);
	}
	
	@Override
	public void showControlledUseUI() {
		controlledUseUI.setVisible(true);
	}
	
	@Override
	public void showImposeRestrictionsAllowedNote() {
		imposeRestrictionsAllowedNote.setVisible(true);
	}
	
	@Override
	public void showImposeRestrictionsButton() {
		imposeRestrictionsButton.setVisible(true);
	}
	
	@Override
	public void showImposeRestrictionsNotAllowedFlagNote() {
		imposeRestrictionsNotAllowedFlagNote.setVisible(true);
	}
	
	@Override
	public void showImposeRestrictionsNotAllowedNote() {
		imposeRestrictionsNotAllowedNote.setVisible(true);
	}

	@Override
	public void showLoginButton() {
		loginButton.setVisible(true);
	}
	
	@Override
	public void showNoRestrictionsUI() {
		noneUI.setVisible(true);
	}
	
	@Override
	public void showOpenUI() {
		openUI.setVisible(true);
	}
	
	@Override
	public void showRequestAccessFromACTButton() {
		requestAccessFromACTButton.setVisible(true);
	}
	
	@Override
	public void showSignTermsButton() {
		signTermsButton.setVisible(true);
	}
	
	@Override
	public void showTermsUI() {
		termsUI.setVisible(true);
	}
	
	@Override
	public void showTouHeading() {
		touHeading.setVisible(true);
	}
	
	@Override
	public void setTerms(String arText) {
		terms.setHTML(arText);
	}
	
	@Override
	public void showWikiTermsUI() {
		wikiTermsUI.setVisible(true);
	}
	
	@Override
	public void setWikiTermsWidget(Widget wikiWidget) {
		wikiTermsUI.setWidget(wikiWidget);
	}
	/*
	 * Private Methods
	 */

}
