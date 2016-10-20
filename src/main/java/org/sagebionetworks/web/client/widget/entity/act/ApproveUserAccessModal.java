package org.sagebionetworks.web.client.widget.entity.act;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.ACTAccessApproval;
import org.sagebionetworks.repo.model.ACTApprovalStatus;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.web.client.SynapseClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ApproveUserAccessModal implements ApproveUserAccessModalView.Presenter, IsWidget {
	
	private String accessRequirement;
	private String userId;
	
	private ApproveUserAccessModalView view;
	private SynapseAlert synAlert;
	private SynapseSuggestBox peopleSuggestWidget;
	private Map<String, AccessRequirement> arMap;
	//private SynapseClient synapseClient;
	
	@Inject
	public ApproveUserAccessModal(ApproveUserAccessModalView view,
			SynapseAlert synAlert,
			SynapseSuggestBox peopleSuggestBox,
			UserGroupSuggestionProvider provider) {
		this.view = view;
		this.synAlert = synAlert;
		this.peopleSuggestWidget = peopleSuggestBox;
		//this.synapseClient = synapseClient;
		peopleSuggestWidget.setSuggestionProvider(provider);
		this.view.setPresenter(this);
		this.view.setUserPickerWidget(peopleSuggestWidget.asWidget());
		peopleSuggestBox.addItemSelectedHandler(new CallbackP<SynapseSuggestion>() {
			@Override
			public void invoke(SynapseSuggestion suggestion) {
				onUserSelected(suggestion);
			}
		});
	}
	
	public void configure(List<AccessRequirement> accessRequirements) {
		this.arMap = new HashMap<String, AccessRequirement>();
		List<String> list = new ArrayList<String>();
		for (AccessRequirement ar : accessRequirements) {
			arMap.put(Long.toString(ar.getId()), ar);
			list.add(Long.toString(ar.getId()));
		}
		view.setSynAlert(synAlert.asWidget());
		view.setStates(list);
		if (list.size() > 0) {
			view.setAccessRequirement(list.get(0), GovernanceServiceHelper.getAccessRequirementText(arMap.get(list.get(0))));			
		}
	}
	
	public void show() {
		synAlert.clear();
		view.show();
	}

	@Override
	public void onSubmit() {
		if (userId == null) {
			synAlert.showError("You must select a user to approve");
			return;
		}
//		ACTAccessApproval aa  = new ACTAccessApproval();
//		aa.setAccessorId(userId);  //user id
//		aa.setApprovalStatus(ACTApprovalStatus.APPROVED);
//		aa.setRequirementId(Long.parseLong(accessRequirement)); //requirement id
		//synapseClient.createAccessApproval(aa);
		view.hide();
	}
	
	public void onUserSelected(SynapseSuggestion suggestion) {
		this.userId = suggestion.getId();
	}
	
	public Widget asWidget() {
		view.setPresenter(this);			
		return view.asWidget();
	}

	@Override
	public void onStateSelected(String state) {
		accessRequirement = state;
		view.setAccessRequirement(state, GovernanceServiceHelper.getAccessRequirementText(arMap.get(state)));
	}
		
}
