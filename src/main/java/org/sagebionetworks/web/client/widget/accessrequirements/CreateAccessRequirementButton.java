package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateAccessRequirementWizard;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateAccessRequirementButton implements IsWidget {
	public static final String CREATE_NEW_ACCESS_REQUIREMENT_BUTTON_TEXT = "Create New Access Requirement";
	public static final String EDIT_ACCESS_REQUIREMENT_BUTTON_TEXT = "Edit Access Requirement";
	public Button button;
	public IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	public PortalGinInjector ginInjector;
	RestrictableObjectDescriptor subject;
	AccessRequirement ar;
	
	@Inject
	public CreateAccessRequirementButton(Button button, 
			IsACTMemberAsyncHandler isACTMemberAsyncHandler,
			final PortalGinInjector ginInjector) {
		this.button = button;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		this.ginInjector = ginInjector;
		button.setVisible(false);
		button.addStyleName("margin-left-10");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				CreateAccessRequirementWizard wizard = ginInjector.getCreateAccessRequirementWizard();
				if (subject != null) {
					wizard.configure(subject);	
				} else if (ar != null) {
					wizard.configure(ar);
				}
				wizard.showModal(null);
			}
		});
	}	
	
	public void configure(AccessRequirement ar) {
		button.setText(EDIT_ACCESS_REQUIREMENT_BUTTON_TEXT);
		this.subject = null;
		this.ar = ar;
		showIfACTMember();
	}
	
	public void configure(RestrictableObjectDescriptor subject) {
		button.setText(CREATE_NEW_ACCESS_REQUIREMENT_BUTTON_TEXT);
		this.subject = subject;
		this.ar = null;
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
	
	public Widget asWidget() {
		return button.asWidget();
	}
	
}
