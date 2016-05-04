package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class UserSelector implements UserSelectorView.Presenter {
	
	private UserSelectorView view;
	CallbackP<String> usernameCallback;
	SynapseSuggestBox suggestBox;
	
	@Inject
	public UserSelector(UserSelectorView view, 
			SynapseSuggestBox suggestBox, 
			UserGroupSuggestionProvider provider
			) {
		this.view = view;
		this.suggestBox = suggestBox;
		view.setPresenter(this);
		suggestBox.setSuggestionProvider(provider);
		view.setSelectBox(suggestBox.asWidget());
		suggestBox.addItemSelectedHandler(new CallbackP<SynapseSuggestion>() {
			public void invoke(SynapseSuggestion suggestion) {
				onSynapseSuggestSelected((UserGroupSuggestion)suggestion);
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
		suggestBox.clear();
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}

	public void onSynapseSuggestSelected(UserGroupSuggestion suggestion) {
		usernameCallback.invoke(suggestion.getHeader().getUserName());
		view.hide();
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
