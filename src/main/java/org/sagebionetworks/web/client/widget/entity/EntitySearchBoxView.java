package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxOracle.EntitySearchBoxSuggestion;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * 
 * @author dburdick
 *
 */
public interface EntitySearchBoxView extends IsWidget, SynapseView {

	public interface Presenter {
		void setSelectedSuggestion(EntitySearchBoxSuggestion suggestion);

		EntitySearchBoxSuggestion getSelectedSuggestion();

		void getPrevSuggestions();

		void getNextSuggestions();
	}

	void setPresenter(Presenter presenter);

	void updateFieldStateForSuggestions(SearchResults responsePage, long offset);

	void setDisplayWidth(int width);

	int getWidth();

	EntitySearchBoxOracle getOracle();

	void hideLoading();

	/**
	 * Gets the string of text in the suggest box.
	 * 
	 * @return The text of the currently contained in the suggest box.
	 */
	String getText();
}
