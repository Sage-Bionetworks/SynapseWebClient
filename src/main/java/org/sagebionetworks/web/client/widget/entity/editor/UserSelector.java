package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
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
		suggestBox.setTypeFilter(TypeFilter.USERS_ONLY);
		view.setSelectBox(suggestBox.asWidget());
		suggestBox.addItemSelectedHandler(new CallbackP<UserGroupSuggestion>() {
			public void invoke(UserGroupSuggestion suggestion) {
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
		suggestBox.clear();
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}

	public void onSynapseSuggestSelected(UserGroupSuggestion suggestion) {
		if (!Boolean.parseBoolean(suggestion.isIndividual())) {
			suggestBox.showErrorMessage(DisplayConstants.NO_USER_SELECTED);
			return;
		}
		
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
	
	public void addModalShownHandler(ModalShownHandler modalShownHandler) {
		view.addModalShownHandler(modalShownHandler);
	}
}
