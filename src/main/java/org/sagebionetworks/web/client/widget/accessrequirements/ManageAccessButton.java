package org.sagebionetworks.web.client.widget.accessrequirements;

import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateAccessRequirementWizard;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ManageAccessButton implements IsWidget {
	public static final String MANAGE_ACCESS_BUTTON_TEXT = "Manage Access";
	public Button button;
	public IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	RestrictableObjectDescriptor subject;
	AccessRequirement ar;
	
	@Inject
	public ManageAccessButton(Button button, 
			IsACTMemberAsyncHandler isACTMemberAsyncHandler,
			final GlobalApplicationState globalAppState) {
		this.button = button;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		button.setVisible(false);
		button.addStyleName("margin-left-10");
		button.setType(ButtonType.PRIMARY);
		button.setText(MANAGE_ACCESS_BUTTON_TEXT);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ACTDataAccessSubmissionsPlace place = new ACTDataAccessSubmissionsPlace(ACTDataAccessSubmissionsPlace.ACCESS_REQUIREMENT_ID_PARAM + "=" + ar.getId());
				globalAppState.getPlaceChanger().goTo(place);
			}
		});
	}	
	
	public void configure(AccessRequirement ar) {
		this.subject = null;
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
	
	public Widget asWidget() {
		return button.asWidget();
	}
	
}
