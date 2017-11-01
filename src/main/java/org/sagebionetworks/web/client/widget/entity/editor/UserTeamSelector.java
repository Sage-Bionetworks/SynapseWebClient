package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class UserTeamSelector implements UserSelectorView.Presenter {
	
	private UserSelectorView view;
	CallbackP<String> aliasCallback;
	SynapseSuggestBox suggestBox;
	GWTWrapper gwt;
	
	@Inject
	public UserTeamSelector(UserSelectorView view, 
			SynapseSuggestBox suggestBox, 
			UserGroupSuggestionProvider provider,
			GWTWrapper gwt
			) {
		this.view = view;
		this.suggestBox = suggestBox;
		this.gwt = gwt;
		view.setPresenter(this);
		suggestBox.setSuggestionProvider(provider);
		suggestBox.setTypeFilter(TypeFilter.ALL);
		view.setSelectBox(suggestBox.asWidget());
		suggestBox.addItemSelectedHandler(new CallbackP<UserGroupSuggestion>() {
			public void invoke(UserGroupSuggestion suggestion) {
				onSynapseSuggestSelected(suggestion);
			};
		});
	}
	
	/**
	 * Configure this widget.  Will call back when a username is selected.
	 * @param aliasCallback
	 */
	public void configure(CallbackP<String> aliasCallback) {
		this.aliasCallback = aliasCallback;
	}
	
	public void clear() {
		suggestBox.clear();
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}

	public void onSynapseSuggestSelected(UserGroupSuggestion suggestion) {
		String userName = suggestion.getHeader().getUserName();
		if (!suggestion.getHeader().getIsIndividual()) {
			// team name, convert to team alias
			userName = gwt.getUniqueAliasName(userName);
		}
		aliasCallback.invoke(userName);
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
