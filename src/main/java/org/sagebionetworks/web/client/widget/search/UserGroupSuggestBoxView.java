package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracle;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracle.UserGroupSuggestion;

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
	void setPlaceholderText(String text);
	int getWidth();
	
	/**
	 * Sets the displays width. This width does not include decorations
	 * such as margin, border, or padding.
	 * @param width The CSS unit of width (e.g. "10px", "1em")
	 */
	void setDisplayWidth(String width);
	
	
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
		
		void addItemSelectedHandler(CallbackP<UserGroupSuggestion> callback);
	}
}
