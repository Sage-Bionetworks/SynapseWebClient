package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginViewImpl extends Composite implements LoginView {
	@UiField
	SimplePanel loginWidgetPanel;
	@UiField
	HTMLPanel loginView;
	
	//terms of service view
	@UiField
	HTMLPanel termsOfServiceView;
	@UiField
	DivElement termsOfServiceHighlightBox;
	@UiField
	CheckBox actEthicallyCb;
	@UiField
	CheckBox protectPrivacyCb;
	@UiField
	CheckBox noHackCb;
	@UiField
	CheckBox shareCb;
	@UiField
	CheckBox responsibilityCb;
	@UiField
	CheckBox lawsCb;
	@UiField
	Button takePledgeButton;
	@UiField
	LoadingSpinner loadingUi;
	@UiField
	Heading loadingUiText;
	@UiField
	Div synAlertContainer;
	
	private Presenter presenter;
	private LoginWidget loginWidget;
	private Header headerWidget;
	public interface Binder extends UiBinder<Widget, LoginViewImpl> {}
	boolean toUInitialized;
	
	
	@Inject
	public LoginViewImpl(Binder uiBinder,
			Header headerWidget,
			LoginWidget loginWidget) {
		initWidget(uiBinder.createAndBindUi(this));
		this.loginWidget = loginWidget;
		this.headerWidget = headerWidget;
		headerWidget.configure();
		toUInitialized = false;
		termsOfServiceHighlightBox.setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Awareness and Ethics Pledge");
	}

	@Override
	public void setPresenter(Presenter loginPresenter) {
		this.presenter = loginPresenter;
		headerWidget.configure();
		com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void showLoggingInLoader() {
		hideViews();
		loadingUi.setVisible(true);
		loadingUiText.setVisible(true);
	}

	@Override
	public void hideLoggingInLoader() {
		loadingUi.setVisible(false);
		loadingUiText.setVisible(false);
	}

	@Override
	public void showLogin() {
		clear();
		hideViews();
		loginView.setVisible(true);
		headerWidget.refresh();
	  	
		// Add the widget to the panel
		loginWidgetPanel.clear();
		loginWidget.asWidget().removeFromParent();
		loginWidgetPanel.add(loginWidget.asWidget());
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
		loginWidget.clear();
		loginWidgetPanel.clear();
	}
	
	@Override
	public void showTermsOfUse(final Callback callback) {
		hideViews();
		//initialize checkboxes
		actEthicallyCb.setValue(false);
		protectPrivacyCb.setValue(false);;
		noHackCb.setValue(false);
		shareCb.setValue(false);
		responsibilityCb.setValue(false);
		lawsCb.setValue(false);

		termsOfServiceView.setVisible(true);
		//initialize if necessary
		if (!toUInitialized) {
			toUInitialized = true;
			takePledgeButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(validatePledge()) {
						callback.invoke();
					} else {
						showErrorMessage("To take the pledge, you must first agree to all of the statements.");
					}
				}
			});
		}
     }
	
	private boolean validatePledge() {
		return actEthicallyCb.getValue() && protectPrivacyCb.getValue() && noHackCb.getValue() && shareCb.getValue() && responsibilityCb.getValue() && lawsCb.getValue();
	}
	private void hideViews() {
		loadingUi.setVisible(false);
		loadingUiText.setVisible(false);
		loginView.setVisible(false);
		termsOfServiceView.setVisible(false);
	}
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
}
