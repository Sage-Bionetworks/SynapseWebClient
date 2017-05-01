package org.sagebionetworks.web.client.widget.accessrequirements;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RequestRevokeUserAccessButton implements IsWidget {
	public static final String REVOKE_BUTTON_TEXT = "Revoke User Access";
	public Button button;
	public PortalGinInjector ginInjector;
	RestrictableObjectDescriptor subject;
	ACTAccessRequirement ar;
	JiraURLHelper jiraUrlHelper;
	PopupUtilsView popupUtilsView;
	AuthenticationController authController;
	IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	@Inject
	public RequestRevokeUserAccessButton(AuthenticationController authController,
			IsACTMemberAsyncHandler isACTMemberAsyncHandler,
			Button button, 
			JiraURLHelper jiraUrlHelper,
			final GlobalApplicationState globalAppState,
			PopupUtilsView popupUtilsView) {
		this.button = button;
		this.jiraUrlHelper = jiraUrlHelper;
		this.popupUtilsView = popupUtilsView;
		this.authController = authController;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		button.setVisible(false);
		button.addStyleName("margin-left-10");
		button.setText(REVOKE_BUTTON_TEXT);
		button.setType(ButtonType.DEFAULT);
		button.setSize(ButtonSize.EXTRA_SMALL);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onRevokeAccess();
			}
		});
	}	
	
	public void configure(ACTAccessRequirement ar) {
		this.ar = ar;
		showIfNotACTMember();
	}
	
	private void showIfNotACTMember() {
		isACTMemberAsyncHandler.isACTMember(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isACTMember) {
				button.setVisible(!isACTMember);
			}
		});
	}

	public void setSize(ButtonSize size) {
		button.setSize(size);
	}
	
	public void setPull(Pull pull) {
		button.setPull(pull);
	}
	
	public Widget asWidget() {
		return button.asWidget();
	}
	
	public void onRevokeAccess() {
		UserProfile profile = authController.getCurrentUserSessionData().getProfile();
		String jiraUrl = jiraUrlHelper.createRevokeAccessIssue(
				authController.getCurrentUserPrincipalId(), 
				DisplayUtils.getDisplayName(profile), 
				DisplayUtils.getPrimaryEmail(profile), 
				ar.getId().toString());
		popupUtilsView.openInNewWindow(jiraUrl);
	}
}
