package org.sagebionetworks.web.client.widget.user;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BigUserBadge implements BigUserBadgeView.Presenter, SynapseWidgetPresenter {
	
	private BigUserBadgeView view;
	SynapseClientAsync synapseClient;
	NodeModelCreator nodeModelCreator;
	
	@Inject
	public BigUserBadge(BigUserBadgeView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}
	
	public void configure(UserProfile profile, String description) {
		view.setProfile(profile, description);
	}
	
	public void configure(final String principalId) {
		configure(principalId, null);
	}
	
	public void configure(final String principalId, final String description) {
		view.showLoading();
		synapseClient.getUserProfile(principalId, new AsyncCallback<String>() {			
			@Override
			public void onSuccess(String result) {
				try {
					UserProfile profile = nodeModelCreator.createJSONEntity(result, UserProfile.class);
					String desc = description != null ? description : profile.getCompany();
					view.setProfile(profile, desc);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showLoadError(principalId);
			}
		});

	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
