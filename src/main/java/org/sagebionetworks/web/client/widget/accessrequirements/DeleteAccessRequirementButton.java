package org.sagebionetworks.web.client.widget.accessrequirements;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DeleteAccessRequirementButton implements IsWidget {
	public static final String DELETED_ACCESS_REQUIREMENT_SUCCESS_MESSAGE = "Successfully deleted access requirement";
	public static final String DELETE_ACCESS_REQUIREMENT_MESSAGE = "Are you sure?";
	public static final String DELETE_ACCESS_REQUIREMENT_TITLE = "Delete Access Requirement";
	public static final String DELETE_ACCESS_REQUIREMENT_BUTTON_TEXT = DELETE_ACCESS_REQUIREMENT_TITLE;
	public Button button;
	public IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	RestrictableObjectDescriptor subject;
	AccessRequirement ar;
	SynapseClientAsync synapseClient;
	PopupUtilsView popupUtils;
	Callback confirmedDeleteCallback;
	CookieProvider cookies;
	Callback refreshCallback;

	@Inject
	public DeleteAccessRequirementButton(Button button, IsACTMemberAsyncHandler isACTMemberAsyncHandler, SynapseClientAsync synapseClient, PopupUtilsView popupUtils, CookieProvider cookies) {
		this.button = button;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.popupUtils = popupUtils;
		this.cookies = cookies;
		button.setVisible(false);
		button.addStyleName("margin-left-10");
		button.setType(ButtonType.DANGER);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteAccessRequirement();
			}
		});
		confirmedDeleteCallback = new Callback() {
			@Override
			public void invoke() {
				deleteAccessRequirementAfterConfirmation();
			}
		};
	}

	public void configure(AccessRequirement ar, Callback refreshCallback) {
		button.setText(DELETE_ACCESS_REQUIREMENT_BUTTON_TEXT);
		this.subject = null;
		this.ar = ar;
		this.refreshCallback = refreshCallback;
		showIfACTMember();
	}

	public void deleteAccessRequirement() {
		popupUtils.showConfirmDialog(DELETE_ACCESS_REQUIREMENT_TITLE, DELETE_ACCESS_REQUIREMENT_MESSAGE, confirmedDeleteCallback);
	}

	public void deleteAccessRequirementAfterConfirmation() {
		synapseClient.deleteAccessRequirement(ar.getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				popupUtils.showInfo(DELETED_ACCESS_REQUIREMENT_SUCCESS_MESSAGE);
				refreshCallback.invoke();
			}

			@Override
			public void onFailure(Throwable caught) {
				popupUtils.showErrorMessage(caught.getMessage());
			}
		});
	}

	private void showIfACTMember() {
		isACTMemberAsyncHandler.isACTActionAvailable(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isACTMember) {
				boolean isInAlpha = DisplayUtils.isInTestWebsite(cookies);
				button.setVisible(isACTMember && isInAlpha);
			}
		});
	}

	public Widget asWidget() {
		return button.asWidget();
	}

}
