package org.sagebionetworks.web.client.widget.entity.act;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTRevokeUserAccessModal implements RevokeUserAccessModalView.Presenter, IsWidget {
	
	public static final String REVOKED_USER = "Successfully revoked user access";
	public static final String NO_APPROVAL_FOUND = "There was no approval found for the specified user and requirement";
	public static final String NO_USER_SELECTED = "You must select a user";
	private String userId;
	
	private RevokeUserAccessModalView view;
	private SynapseAlert synAlert;
	private SynapseSuggestBox peopleSuggestWidget;
	private SynapseClientAsync synapseClient;
	private ACTAccessRequirement ar;
	@Inject
	public ACTRevokeUserAccessModal(RevokeUserAccessModalView view,
			SynapseAlert synAlert,
			SynapseSuggestBox peopleSuggestBox,
			UserGroupSuggestionProvider provider, 
			SynapseClientAsync synapseClient
			) {
		this.view = view;
		this.synAlert = synAlert;
		this.peopleSuggestWidget = peopleSuggestBox;
		this.synapseClient = synapseClient;
		peopleSuggestWidget.setSuggestionProvider(provider);
		this.view.setPresenter(this);
		this.view.setUserPickerWidget(peopleSuggestWidget.asWidget());
		view.setSynAlert(synAlert.asWidget());
		peopleSuggestBox.addItemSelectedHandler(new CallbackP<SynapseSuggestion>() {
			@Override
			public void invoke(SynapseSuggestion suggestion) {
				onUserSelected(suggestion);
			}
		});
	}

	public void configure(ACTAccessRequirement ar) {
		this.ar = ar;
		synAlert.clear();
		view.show();
	}
	
	@Override
	public void onRevoke() {
		if (userId == null) {
			synAlert.showError(NO_USER_SELECTED);
			return;
		}
		view.setRevokeProcessing(true);
		synapseClient.deleteAccessApprovals(ar.getId().toString(), userId, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				view.setRevokeProcessing(false);
			}
			
			@Override
			public void onSuccess(Void result) {
				view.setRevokeProcessing(false);
				view.hide();
				view.showInfo(REVOKED_USER, "");
			}
		});
	}
	
	public void onUserSelected(SynapseSuggestion suggestion) {
		this.userId = suggestion.getId();
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
}
