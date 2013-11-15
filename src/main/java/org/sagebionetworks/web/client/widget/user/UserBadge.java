package org.sagebionetworks.web.client.widget.user;

import java.util.Map;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserBadge implements UserBadgeView.Presenter, SynapseWidgetPresenter, WidgetRendererPresenter {
	
	private UserBadgeView view;
	SynapseClientAsync synapseClient;
	NodeModelCreator nodeModelCreator;
	private Integer maxNameLength;
	
	@Inject
	public UserBadge(UserBadgeView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}
	
	public void setMaxNameLength(Integer maxLength) {
		this.maxNameLength = maxLength;
	}
	
	public void configure(UserProfile profile) {
		view.setProfile(profile, maxNameLength);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired) {
		//get the user id from the descriptor, and pass to the other configure
		configure(widgetDescriptor.get(WidgetConstants.USERBADGE_WIDGET_ID_KEY));
	}
	
	public void configure(final String principalId) {
		view.showLoading();
		synapseClient.getUserProfile(principalId, new AsyncCallback<String>() {			
			@Override
			public void onSuccess(String result) {
				try {
					UserProfile profile = nodeModelCreator.createJSONEntity(result, UserProfile.class);
					view.setProfile(profile, maxNameLength);
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
