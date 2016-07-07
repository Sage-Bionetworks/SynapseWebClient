package org.sagebionetworks.web.client.presenter.users;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.Presenter;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class RegisterAccountPresenter extends AbstractActivity implements RegisterAccountView.Presenter, Presenter<RegisterAccount> {
	private RegisterAccount place;
	private RegisterAccountView view;
	
	private GlobalApplicationState globalApplicationState;
	private RegisterWidget registerWidget;
	private Header headerWidget;
	private Footer footerWidget;
	
	@Inject
	public RegisterAccountPresenter(RegisterAccountView view,
			GlobalApplicationState globalApplicationState,
			RegisterWidget registerWidget,
			Header headerWidget, Footer footerWidget) {
		this.view = view;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		// Set the presenter on the view
		this.globalApplicationState = globalApplicationState;
		this.registerWidget = registerWidget;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}
	
	public void init() {
		boolean isInline = false;
		registerWidget.configure(isInline);
		view.setRegisterWidget(registerWidget.asWidget());
		headerWidget.configure(false);
		headerWidget.refresh();
		view.setFooterWidget(footerWidget.asWidget());
		view.setHeaderWidget(headerWidget.asWidget());
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}

	@Override
	public void setPlace(RegisterAccount place) {
		this.place = place;
		String token = place.toToken();
		String email = "";
		if(token != null && !ClientProperties.DEFAULT_PLACE_TOKEN.equals(token)){
			email = token.trim();
		}
		registerWidget.setEmail(email);
		init();
	}
}
