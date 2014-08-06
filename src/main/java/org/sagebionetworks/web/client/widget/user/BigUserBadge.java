package org.sagebionetworks.web.client.widget.user;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BigUserBadge implements BigUserBadgeView.Presenter, SynapseWidgetPresenter {
	
	private BigUserBadgeView view;
	SynapseClientAsync synapseClient;
	AdapterFactory adapterFactory;
	ClientCache clientCache;
	
	@Inject
	public BigUserBadge(BigUserBadgeView view, SynapseClientAsync synapseClient, AdapterFactory adapterFactory, ClientCache clientCache) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.clientCache = clientCache;
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
		
		UserBadge.getUserProfile(principalId, adapterFactory, synapseClient, clientCache, new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(UserProfile profile) {
				String desc = description != null ? description : profile.getCompany();
				view.setProfile(profile, desc);
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
