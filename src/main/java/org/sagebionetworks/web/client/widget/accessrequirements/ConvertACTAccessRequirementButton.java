package org.sagebionetworks.web.client.widget.accessrequirements;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementConversionRequest;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PopupUtilsView;
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

public class ConvertACTAccessRequirementButton implements IsWidget, ClickHandler {
	public static final String SUCCESS_MESSAGE = "Successfully converted access requirement";
	public static final String CONVERT_AR_CONFIRM_TITLE = "Are you sure you want to migrate this Access Requirement?";
	public static final String CONVERT_AR_CONFIRM_MESSAGE = "Clicking OK will change this access requirement so that new data access requests will be handled within Synapse (no longer through Jira/email/spreadsheets). Please test the migration on staging (staging.synapse.org) before running on production (www.synapse.org). THIS ACTION CANNOT BE UNDONE.";
	public static final String DELETE_OLD_INSTRUCTIONS_MESSAGE = "Sorry, you have to delete the old (html) instructions before it can be converted.  Edit the Access Requirement, add a wiki, then delete the old instructions.";
	public static final String CONVERT_TO_MANAGED = "Migrate";
	public Button button;
	public IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	ACTAccessRequirement ar;
	PopupUtilsView popupUtils;
	DataAccessClientAsync dataAccessClient;
	Callback refreshCallback;

	@Inject
	public ConvertACTAccessRequirementButton(Button button, IsACTMemberAsyncHandler isACTMemberAsyncHandler, PopupUtilsView popupUtilsView, DataAccessClientAsync dataAccessClient) {
		this.button = button;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		this.popupUtils = popupUtilsView;
		this.dataAccessClient = dataAccessClient;
		fixServiceEntryPoint(dataAccessClient);
		button.setVisible(false);
		button.addStyleName("margin-left-10");
		button.setType(ButtonType.WARNING);
		button.setIcon(IconType.HAND_SPOCK_O);
		button.setText(CONVERT_TO_MANAGED);
		button.addClickHandler(this);
	}

	public void configure(ACTAccessRequirement ar, Callback refreshCallback) {
		this.ar = ar;
		this.refreshCallback = refreshCallback;
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

	@Override
	public void onClick(ClickEvent event) {
		if (ar.getActContactInfo() != null && ar.getActContactInfo().trim().length() > 0) {
			popupUtils.showErrorMessage(DELETE_OLD_INSTRUCTIONS_MESSAGE);
		} else {
			// confirm
			popupUtils.showConfirmDialog(CONVERT_AR_CONFIRM_TITLE, CONVERT_AR_CONFIRM_MESSAGE, new Callback() {
				@Override
				public void invoke() {
					// confirmed
					convertAccessRequirement();
				}
			});
		}
	}

	public void convertAccessRequirement() {
		AccessRequirementConversionRequest request = new AccessRequirementConversionRequest();
		request.setAccessRequirementId(ar.getId().toString());
		request.setCurrentVersion(ar.getVersionNumber());
		request.setEtag(ar.getEtag());
		dataAccessClient.convertAccessRequirement(request, new AsyncCallback<AccessRequirement>() {

			@Override
			public void onSuccess(AccessRequirement result) {
				popupUtils.showInfo(SUCCESS_MESSAGE);
				refreshCallback.invoke();
			}

			@Override
			public void onFailure(Throwable caught) {
				popupUtils.showErrorMessage(caught.getMessage());
			}
		});
	}

	public Widget asWidget() {
		return button.asWidget();
	}

}
