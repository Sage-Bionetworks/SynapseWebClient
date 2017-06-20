package org.sagebionetworks.web.client.widget.accessrequirements;

import static org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandlerImpl.SESSION_KEY_PREFIX;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HideACTActionsButton implements IsWidget {
	Button button;
	IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	SessionStorage sessionStorage;
	AuthenticationController authController;
	GlobalApplicationState globalAppState;
	public static final String HIDE_ACT_UI = "Hide ACT UI";
	@Inject
	public HideACTActionsButton(Button button, 
			IsACTMemberAsyncHandler isACTMemberAsyncHandler,
			SessionStorage sessionStorage, 
			AuthenticationController authController,
			GlobalApplicationState globalAppState) {
		this.button = button;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		this.sessionStorage = sessionStorage;
		this.authController = authController;
		this.globalAppState = globalAppState;
		
		button.setVisible(false);
		button.setSize(ButtonSize.EXTRA_SMALL);
		button.setType(ButtonType.LINK);
		button.addStyleName("color-white margin-left-10 margin-right-10");
		button.setText(HIDE_ACT_UI);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hideACTActions();
			}
		});
		showIfACTMember();
	}
	
	public void refresh() {
		showIfACTMember();
	}
	
	public void hideACTActions() {
		sessionStorage.setItem(SESSION_KEY_PREFIX + authController.getCurrentUserPrincipalId(), Boolean.FALSE.toString());
		globalAppState.refreshPage();
	}
	
	private void showIfACTMember() {
		isACTMemberAsyncHandler.isACTMember(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isACTMember) {
				button.setVisible(isACTMember);
			}
		});
	}
	
	public Widget asWidget() {
		return button.asWidget();
	}
}
