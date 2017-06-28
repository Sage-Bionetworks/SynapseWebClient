package org.sagebionetworks.web.client.widget.accessrequirements.approval;

import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroup;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessorGroupWidget implements AccessorGroupView.Presenter, IsWidget {
	
	private AccessorGroupView view;
	SynapseAlert synAlert;
	AccessorGroup accessorGroup;
	PortalGinInjector ginInjector;
	PopupUtilsView popupUtils;
	AccessRequirementWidget accessRequirementWidget;
	DataAccessClientAsync dataAccessClient;
	SynapseClientAsync synapseClient;
	Callback onRevokeCallback;
	
	@Inject
	public AccessorGroupWidget(AccessorGroupView view, 
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			PopupUtilsView popupUtils,
			AccessRequirementWidget accessRequirementWidget,
			DataAccessClientAsync dataAccessClient,
			SynapseClientAsync synapseClient) {
		this.view = view;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.popupUtils = popupUtils;
		this.accessRequirementWidget = accessRequirementWidget;
		this.dataAccessClient = dataAccessClient;
		this.synapseClient = synapseClient;
		view.setPresenter(this);
		view.setSynAlert(synAlert);
		view.setAccessRequirementWidget(accessRequirementWidget);
	}
	
	public void configure(AccessorGroup accessorGroup) {
		synAlert.clear();
		this.accessorGroup = accessorGroup;
		addAccessorUserBadges(accessorGroup.getAccessorIds());
		UserBadge badge = ginInjector.getUserBadgeWidget();
		badge.configure(accessorGroup.getSubmitterId());
		view.setSubmittedBy(badge);
	}
	
	public void setOnRevokeCallback(Callback onRevokeCallback) {
		this.onRevokeCallback = onRevokeCallback;
	}
	
	public void addAccessorUserBadges(List<String> accessorIds) {
		view.clearAccessors();
		for (String userId : accessorIds) {
			UserBadge badge = ginInjector.getUserBadgeWidget();
			badge.configure(userId);
			view.addAccessor(badge);
		}
	}
	@Override
	public void onShowEmails() {
		//get the profiles, to get the usernames
		synapseClient.listUserProfiles(accessorGroup.getAccessorIds(), new AsyncCallback<List<UserProfile>>() {
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
				view.setEmailAddresses(sb.toString());
				view.showEmailAddressesDialog();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	public void addStyleNames(String styleNames) {
		view.addStyleNames(styleNames);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}
	
	@Override
	public void onShowAccessRequirement() {
		// TODO:  uncomment when access requirement id is available from the accessor group
//		accessRequirementWidget.configure(accessorGroup.getAccessRequirementId());
		view.showAccessRequirementDialog();
	}
	
	@Override
	public void onRevoke() {
		popupUtils.showConfirmDialog("Revoke access to group?", "Are you sure?", new Callback() {
			@Override
			public void invoke() {
				onRevokeAfterConfirm();
			}
		});
	}
	
	public void onRevokeAfterConfirm() {
		// TODO:  uncomment when access requirement id is available from the accessor group
//		dataAccessClient.revokeGroup(accessorGroup.getAccessRequirementId(), accessorGroup.getSubmitterId(), new AsyncCallback<Void>() {
//			@Override
//			public void onSuccess(Void result) {
//				popupUtils.showInfo("Successfully revoked access.", "");
//				if (onRevokeCallback != null) {
//					onRevokeCallback.invoke();
//				}
//			}
//			
//			@Override
//			public void onFailure(Throwable caught) {
//				synAlert.handleException(caught);
//			}
//		});
	}
}