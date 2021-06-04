package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsni.SynapseContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.FullWidthAlert;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.pageprogress.PageProgressWidget;
import org.sagebionetworks.web.shared.WebConstants;

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
	ReactComponentDiv termsOfUseContainer;	
	@UiField
	FullWidthAlert acceptedTermsOfUseView;
	@UiField
	LoadingSpinner loadingUi;
	@UiField
	Heading loadingUiText;
	@UiField
	Div synAlertContainer;
	@UiField
	Div pageProgressContainer;
	
	private Presenter presenter;
	private LoginWidget loginWidget;
	private Header headerWidget;
	SynapseJSNIUtils jsniUtils;
	SynapseContextPropsProvider propsProvider;
	PageProgressWidget pageProgressWidget;
	Callback backBtnCallback, forwardBtnCallback;
	public interface LoginViewImplBinder extends UiBinder<Widget, LoginViewImpl> {
	}	

	@Inject
	public LoginViewImpl(LoginViewImplBinder uiBinder, Header headerWidget, LoginWidget loginWidget, SynapseJSNIUtils jsniUtils, PageProgressWidget pageProgressWidget, SynapseContextPropsProvider propsProvider) {
		initWidget(uiBinder.createAndBindUi(this));
		this.loginWidget = loginWidget;
		this.headerWidget = headerWidget;
		this.pageProgressWidget = pageProgressWidget;
		this.jsniUtils = jsniUtils;
		this.propsProvider = propsProvider;
		headerWidget.configure();
		pageProgressContainer.add(pageProgressWidget);
		backBtnCallback = () -> {
			presenter.onCancelAcceptTermsOfUse();			
		};
		forwardBtnCallback = () -> {
			presenter.onAcceptTermsOfUse();
		};
	}

	private void reconfigurePageProgress(boolean enableForward) {
		pageProgressWidget.configure(WebConstants.SYNAPSE_GREEN, 75, "Cancel", backBtnCallback, "Next", forwardBtnCallback, enableForward);
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
			reconfigurePageProgress(false);
			_showTermsOfUse(termsOfUseContainer.getElement(), this, propsProvider.getJsniContextProps());
		} else {
			acceptedTermsOfUseView.setVisible(true);
		}		
	}
	
	public void onFormChange(boolean completed) {
		reconfigurePageProgress(completed);		
	}
	
	private static native void _showTermsOfUse(Element el, LoginViewImpl v, SynapseContextProviderPropsJSNIObject wrapperProps) /*-{
		try {
			function cb(completed) {
				v.@org.sagebionetworks.web.client.view.LoginViewImpl::onFormChange(Z)(completed);
			}
			var props = {
			  	onFormChange: cb,
			};

			var component = $wnd.React.createElement($wnd.SRC.SynapseComponents.TermsAndConditions, props, null)
			var wrapper = $wnd.React.createElement($wnd.SRC.SynapseContext.SynapseContextProvider, wrapperProps, component)

			$wnd.ReactDOM.render(wrapper, el);
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
