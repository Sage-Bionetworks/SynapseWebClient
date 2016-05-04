package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class UserSelector implements UserSelectorView.Presenter {
	
	private UserSelectorView view;
	CallbackP<String> usernameCallback;
	SynapseSuggestBox suggestBox;
	SynapseAlert synAlert;
	SynapseClientAsync synapseClient;
	@Inject
	public UserSelector(UserSelectorView view, 
			SynapseSuggestBox suggestBox, 
			UserGroupSuggestionProvider provider,
			SynapseAlert synAlert,
			SynapseClientAsync synapseClient
			) {
		this.view = view;
		this.suggestBox = suggestBox;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
		suggestBox.setSuggestionProvider(provider);
		view.setSelectBox(suggestBox.asWidget());
		suggestBox.addItemSelectedHandler(new CallbackP<SynapseSuggestion>() {
			public void invoke(SynapseSuggestion suggestion) {
				onSynapseSuggestSelected(suggestion);
			};
		});
	}
	
	/**
	 * Configure this widget.  Will call back when a username is selected.
	 * @param usernameCallback
	 */
	public void configure(CallbackP<String> usernameCallback) {
		this.usernameCallback = usernameCallback;
	}
	
	public void clear() {
		synAlert.clear();
		suggestBox.clear();
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}

	public void onSynapseSuggestSelected(SynapseSuggestion suggestion) {
		synAlert.clear();
		synapseClient.getUserProfile(suggestion.getId(), new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(UserProfile profile) {
				usernameCallback.invoke(profile.getUserName());
				view.hide();
			}
			
			@Override
			public void onFailure(Throwable t) {
				synAlert.handleException(t);
			}
		});
	}
	
	@Override
	public void onModalShown() {
		suggestBox.setFocus(true);		
	}
	
	public void show() {
		clear();
		view.show();
	}
}
