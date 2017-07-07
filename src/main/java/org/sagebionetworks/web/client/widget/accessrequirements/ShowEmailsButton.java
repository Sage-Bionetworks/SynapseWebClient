package org.sagebionetworks.web.client.widget.accessrequirements;

import java.util.Iterator;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.Button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ShowEmailsButton implements IsWidget {
	public static final String EMAILS_BUTTON_TEXT = "Email";
	Button button;
	SynapseClientAsync synapseClient;
	List<String> userIds;
	PopupUtilsView popupUtils;
	
	@Inject
	public ShowEmailsButton(
			Button button,
			SynapseClientAsync synapseClient,
			PopupUtilsView popupUtils
		) {
		this.button = button;
		this.synapseClient = synapseClient;
		this.popupUtils = popupUtils;
		button.addStyleName("margin-left-10");
		button.setType(ButtonType.PRIMARY);
		button.setSize(ButtonSize.EXTRA_SMALL);
		button.setText(EMAILS_BUTTON_TEXT);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onShowEmails();
			}
		});
	}	
	
	public void configure(List<String> userIds) {
		this.userIds = userIds;
	}
	
	public void onShowEmails() {
		//get the profiles, to get the usernames
		synapseClient.listUserProfiles(userIds, new AsyncCallback<List<UserProfile>>() {
			@Override
			public void onSuccess(List<UserProfile> userProfiles) {
				StringBuilder sb = new StringBuilder();
				for (Iterator it = userProfiles.iterator(); it.hasNext();) {
					UserProfile profile = (UserProfile) it.next();
					sb.append(profile.getUserName() + "@synapse.org");
					if (it.hasNext()) {
						sb.append(", ");
					}
				}
				popupUtils.showInfoDialog("Email ", sb.toString(), null);
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
