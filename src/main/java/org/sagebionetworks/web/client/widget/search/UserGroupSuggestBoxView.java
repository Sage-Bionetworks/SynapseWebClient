package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox.UserGroupSuggestOracle;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox.UserGroupSuggestOracle.UserGroupSuggestion;

import com.google.gwt.user.client.ui.IsWidget;

public interface UserGroupSuggestBoxView extends IsWidget, SynapseView {
	
	/**
	 * Gets the string of text in the suggest box.
	 * @return The text of the currently contained in the suggest box.
	 */
	String getText();
	UserGroupSuggestOracle getUserGroupSuggestOracle();
	
	void hideLoading();
	void clear();
	void updateFieldStateForSuggestions(UserGroupHeaderResponsePage responsePage, int offset);
	
	
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		UserGroupSuggestion getSelectedSuggestion();
		void setSelectedSuggestion(UserGroupSuggestion selectedSuggestion);
		
		void getPrevSuggestions();
		void getNextSuggestions();
	}
}
