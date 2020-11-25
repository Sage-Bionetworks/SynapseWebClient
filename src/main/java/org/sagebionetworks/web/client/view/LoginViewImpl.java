package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.InfoAlert;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.LoginWidget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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

	// terms of service view
	@UiField
	Div termsOfUseView;
	@UiField
	Div termsOfUseContainer;	
	@UiField
	InfoAlert acceptedTermsOfUseView;
	@UiField
	LoadingSpinner loadingUi;
	@UiField
	Heading loadingUiText;
	@UiField
	Div synAlertContainer;
	@UiField
	Button takePledgeButton;
	
	private Presenter presenter;
	private LoginWidget loginWidget;
	private Header headerWidget;
	SynapseJSNIUtils jsniUtils;
	
	public interface LoginViewImplBinder extends UiBinder<Widget, LoginViewImpl> {
	}	

	@Inject
	public LoginViewImpl(LoginViewImplBinder uiBinder, Header headerWidget, LoginWidget loginWidget, SynapseJSNIUtils jsniUtils) {
		initWidget(uiBinder.createAndBindUi(this));
		this.loginWidget = loginWidget;
		this.headerWidget = headerWidget;
		this.jsniUtils = jsniUtils;
		headerWidget.configure();
		
		takePledgeButton.addClickHandler(event -> {
			presenter.onAcceptTermsOfUse();
		});
	}

	@Override
	public void setPresenter(Presenter loginPresenter) {
		this.presenter = loginPresenter;
		headerWidget.configure();
		headerWidget.refresh();
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
		loginWidget.asWidget().removeFromParent();
		loginWidgetPanel.setWidget(loginWidget.asWidget());
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}


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
	public void showTermsOfUse(boolean hasAccepted) {
		hideViews();
		
		if (!hasAccepted) {
			termsOfUseView.setVisible(true);
			takePledgeButton.setEnabled(false);
			jsniUtils.unmountComponentAtNode(termsOfUseContainer.getElement());
			_showTermsOfUse(termsOfUseContainer.getElement(), this);
		} else {
			acceptedTermsOfUseView.setVisible(true);
		}		
	}
	
	// will interact with Page Progress widget in the future
	public void onFormComplete() {
		takePledgeButton.setEnabled(true);
	}
	public void onFormIncomplete() {
		takePledgeButton.setEnabled(false);
	}
	
	private static native void _showTermsOfUse(Element el, LoginViewImpl v) /*-{
		try {
			function cb(completed) {
				if (completed) {
					v.@org.sagebionetworks.web.client.view.LoginViewImpl::onFormComplete()();
				} else {
					v.@org.sagebionetworks.web.client.view.LoginViewImpl::onFormIncomplete()();
				}				
			}
			var props = {
			  	onFormChange: cb,
			};
			$wnd.ReactDOM.render($wnd.React.createElement(
					$wnd.SRC.SynapseComponents.TermsAndConditions, props, null),
					el);
		} catch (err) {
			console.error(err);
		}
	}-*/;

	private void hideViews() {
		loadingUi.setVisible(false);
		loadingUiText.setVisible(false);
		loginView.setVisible(false);
		termsOfUseView.setVisible(false);
		acceptedTermsOfUseView.setVisible(false);		
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
}
