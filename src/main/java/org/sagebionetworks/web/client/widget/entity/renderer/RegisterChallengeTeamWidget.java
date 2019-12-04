package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.RegisterTeamDialog;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterChallengeTeamWidget implements SingleButtonView.Presenter, WidgetRendererPresenter {

	private SingleButtonView view;
	private Map<String, String> descriptor;
	private PortalGinInjector ginInjector;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	public static final String DEFAULT_BUTTON_TEXT = "Register Team";
	String challengeId;
	Callback widgetRefreshRequired;

	@Inject
	public RegisterChallengeTeamWidget(SingleButtonView view, PortalGinInjector ginInjector, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.ginInjector = ginInjector;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
		view.setButtonSize(ButtonSize.LARGE);
		view.setButtonType(ButtonType.PRIMARY);
	}

	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		this.widgetRefreshRequired = widgetRefreshRequired;
		challengeId = descriptor.get(WidgetConstants.CHALLENGE_ID_KEY);
		String buttonText = descriptor.get(WidgetConstants.BUTTON_TEXT_KEY);
		if (buttonText == null)
			buttonText = DEFAULT_BUTTON_TEXT;

		view.setButtonText(buttonText);
		descriptor = widgetDescriptor;
	}

	@Override
	public void onClick() {
		// if logged in, then show register team dialog
		if (authenticationController.isLoggedIn()) {
			RegisterTeamDialog dialog = ginInjector.getRegisterTeamDialog();
			view.clearWidgets();
			view.addWidget(dialog.asWidget());
			dialog.configure(challengeId, widgetRefreshRequired);
		} else {
			view.showConfirmDialog(DisplayConstants.ANONYMOUS_JOIN, getConfirmCallback());
		}
	}

	public Callback getConfirmCallback() {
		return () -> {
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		};
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
