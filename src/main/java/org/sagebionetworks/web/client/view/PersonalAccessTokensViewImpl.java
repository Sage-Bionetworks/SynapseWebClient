package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.PersonalAccessTokensPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PersonalAccessTokensViewImpl extends Composite implements PersonalAccessTokensView {

	public interface PersonalAccessTokensViewImplUiBinder extends UiBinder<Widget, PersonalAccessTokensViewImpl> {
	}

	@UiField
	ReactComponentDiv container;
	@UiField
	Anchor backToSettingsAnchor;

	private PersonalAccessTokensPresenter presenter;
	private AuthenticationController authController;
	private Header headerWidget;
	private SynapseJSNIUtils jsniUtils;

	@Inject
	public PersonalAccessTokensViewImpl(PersonalAccessTokensViewImplUiBinder uiBinder, AuthenticationController authenticationController, Header headerWidget, SynapseJSNIUtils jsniUtils) {
		initWidget(uiBinder.createAndBindUi(this));

		this.jsniUtils = jsniUtils;
		this.authController = authenticationController;
		this.headerWidget = headerWidget;
		headerWidget.configure();

		backToSettingsAnchor.addClickHandler(event -> presenter.goTo(new Profile(authenticationController.getCurrentUserPrincipalId(), Synapse.ProfileArea.SETTINGS)));
	}


	@Override
	public void render() {
		Window.scrollTo(0, 0); // scroll user to top of page
		_showPersonalAccessTokensComponent(container.getElement(), authController.getCurrentUserSessionToken());
	}

	private static native void _showPersonalAccessTokensComponent(Element el, String sessionToken) /*-{
		try {
			var props = {
			  	title: "Personal Access Tokens",
				body: "Issue personal access tokens to access your Synapse resources in the command line clients. A personal access token will expire if it is unused for 180 consecutive days. You may create up to 100 personal access tokens.",
				token: sessionToken,
			};
			$wnd.ReactDOM.render($wnd.React.createElement(
					$wnd.SRC.SynapseComponents.AccessTokenPage, props, null),
					el);
		} catch (err) {
			console.error(err);
		}
	}-*/;




	@Override
	public void setPresenter(PersonalAccessTokensPresenter presenter) {
		this.presenter = presenter;
		headerWidget.refresh();
	}
}