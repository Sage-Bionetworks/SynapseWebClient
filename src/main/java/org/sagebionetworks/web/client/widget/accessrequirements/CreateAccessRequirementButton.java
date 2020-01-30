package org.sagebionetworks.web.client.widget.accessrequirements;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
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

public class CreateAccessRequirementButton implements IsWidget {
	public static final String CREATE_NEW_ACCESS_REQUIREMENT_BUTTON_TEXT = "Create New Access Requirement";
	public static final String EDIT_ACCESS_REQUIREMENT_BUTTON_TEXT = "Edit Access Requirement";
	public Button button;
	public IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	public PortalGinInjector ginInjector;
	RestrictableObjectDescriptor subject;
	AccessRequirement ar;
	Callback refreshCallback;

	@Inject
	public CreateAccessRequirementButton(Button button, IsACTMemberAsyncHandler isACTMemberAsyncHandler, final PortalGinInjector ginInjector) {
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
				wizard.showModal(new WizardCallback() {
					@Override
					public void onFinished() {
						refreshCallback.invoke();
					}

					@Override
					public void onCanceled() {
						refreshCallback.invoke();
					}
				});
			}
		});
	}

	public void configure(AccessRequirement ar, Callback refreshCallback) {
		button.setText(EDIT_ACCESS_REQUIREMENT_BUTTON_TEXT);
		button.setSize(ButtonSize.DEFAULT);
		button.setType(ButtonType.DEFAULT);
		button.setIcon(IconType.EDIT);
		this.refreshCallback = refreshCallback;
		this.subject = null;
		this.ar = ar;
		showIfACTMember();
	}

	public void configure(RestrictableObjectDescriptor subject, Callback refreshCallback) {
		button.setText(CREATE_NEW_ACCESS_REQUIREMENT_BUTTON_TEXT);
		button.setSize(ButtonSize.LARGE);
		button.setType(ButtonType.PRIMARY);
		button.setIcon(IconType.PLUS);
		this.refreshCallback = refreshCallback;
		this.subject = subject;
		this.ar = null;
		showIfACTMember();
	}

	private void showIfACTMember() {
		isACTMemberAsyncHandler.isACTActionAvailable(new CallbackP<Boolean>() {
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
