package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;

public interface SynapseSuggestBoxView extends IsWidget, SynapseView, Focusable, HasKeyDownHandlers {

	/**
	 * Gets the string of text in the suggest box.
	 * 
	 * @return The text of the currently contained in the suggest box.
	 */
	String getText();

	SynapseSuggestOracle getUserGroupSuggestOracle();

	void hideLoading();

	void clear();

	void updateFieldStateForSuggestions(int numResults, int offset);

	void setPlaceholderText(String text);

	int getWidth();

	void setFocus(boolean focused);

	void selectAll();

	/**
	 * Sets the displays width. This width does not include decorations such as margin, border, or
	 * padding.
	 * 
	 * @param width The CSS unit of width (e.g. "10px", "1em")
	 */
	void setDisplayWidth(String width);


	/**
	 * Set the presenter.
	 * 
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

		void showLoading();

		void hideLoading();

		void showErrorMessage(String message);

		void updateFieldStateForSuggestions(int numResults, int offset);

		void handleOracleException(Throwable caught);
	}

	void configure(SynapseSuggestOracle oracle);

	void setText(String text);

	void setSelectedText(String displayString);
}
