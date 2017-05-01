package org.sagebionetworks.web.client.widget.accessrequirements;

import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.act.ACTRevokeUserAccessModal;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTRevokeUserAccessButton implements IsWidget {
	public static final String REVOKE_BUTTON_TEXT = "Revoke User Access";
	public Button button;
	public IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	public PortalGinInjector ginInjector;
	RestrictableObjectDescriptor subject;
	ACTAccessRequirement ar;
	
	@Inject
	public ACTRevokeUserAccessButton(Button button, 
			IsACTMemberAsyncHandler isACTMemberAsyncHandler,
			final PortalGinInjector ginInjector,
			final GlobalApplicationState globalAppState) {
		this.button = button;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		this.ginInjector = ginInjector;
		button.setVisible(false);
		button.addStyleName("margin-left-10");
		button.setText(REVOKE_BUTTON_TEXT);
		button.setType(ButtonType.DEFAULT);
		button.setIcon(IconType.USER_TIMES);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ACTRevokeUserAccessModal modal = ginInjector.getACTRevokeUserAccessModal();
				modal.configure(ar);
			}
		});
	}	
	
	public void configure(ACTAccessRequirement ar) {
		this.ar = ar;
		showIfACTMember();
	}
	
	private void showIfACTMember() {
		isACTMemberAsyncHandler.isACTMember(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isACTMember) {
				button.setVisible(isACTMember);
			}
		});
	}
	
	public void setPull(Pull pull) {
		button.setPull(pull);
	}
	
	public Widget asWidget() {
		return button.asWidget();
	}
	
}
