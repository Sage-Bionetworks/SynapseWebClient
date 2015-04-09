package org.sagebionetworks.web.client.widget.search;

import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracle;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracle.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestionDisplay;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.inject.Inject;

public class UserGroupSuggestBoxViewImpl extends SuggestBox implements UserGroupSuggestBoxView {
	
	private Presenter presenter;
	
	@Inject
	public UserGroupSuggestBoxViewImpl(UserGroupSuggestOracle oracle, SageImageBundle sageImageBundle) {
		// Textbox? Or discuss functionality.
		super(oracle, new TextBox(), new SynapseSuggestionDisplay(sageImageBundle));
		addStyleName("userGroupSuggestBox");
		getValueBox().addStyleName("form-control");
		addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				selectSuggestion((UserGroupSuggestion) event.getSelectedItem());
			}
			
		});
		
		getValueBox().addFocusHandler(new FocusHandler() {

			@Override
			public void onFocus(FocusEvent event) {
				if (presenter.getSelectedSuggestion() != null) {
					
					// If a user/group is selected, the text in the input box should not
					// be editable. If the user tries to edit it (focus event on value box),
					// the text will revert to what it was before they selected the element.
					setText(presenter.getSelectedSuggestion().getPrefix());
					showSuggestionList();
					presenter.setSelectedSuggestion(null);
				}
			}
			
		});
		
		// Previous suggestions button.
		((SynapseSuggestionDisplay) getSuggestionDisplay()).getPrevButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.getPrevSuggestions();
			}
			
		});
		
		// Next suggestions button.
		((SynapseSuggestionDisplay) getSuggestionDisplay()).getNextButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.getNextSuggestions();
			}
			
		});
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void updateFieldStateForSuggestions(UserGroupHeaderResponsePage responsePage, int offset) {
		Button prevBtn = ((SynapseSuggestionDisplay) getSuggestionDisplay()).getPrevButton();
		Button nextBtn = ((SynapseSuggestionDisplay) getSuggestionDisplay()).getNextButton();
		Label resultsLbl = ((SynapseSuggestionDisplay) getSuggestionDisplay()).getResultsLabel();
		
		prevBtn.setEnabled(offset != 0);
		boolean moreResults = offset + UserGroupSuggestBox.PAGE_SIZE < responsePage.getTotalNumberOfResults();
		nextBtn.setEnabled(moreResults);
		
		String resultsLabel = "Displaying " + (offset + 1) + " - "
								+ (moreResults ? offset + UserGroupSuggestBox.PAGE_SIZE : responsePage.getTotalNumberOfResults())
								+ " of " + responsePage.getTotalNumberOfResults();
		resultsLbl.setText(resultsLabel);
	}
	
	public void clear() {
		presenter.setSelectedSuggestion(null);
		getValueBox().setText(null);	// Empty text box
	}
	
	public void selectSuggestion(UserGroupSuggestion suggestion) {
		getValueBox().setFocus(false);
		
		// Update the SuggestBox's selected suggestion.
		presenter.setSelectedSuggestion(suggestion);
		setText(suggestion.getReplacementString());
	}
	
	@Override
	public void showLoading() {
		((SynapseSuggestionDisplay) getSuggestionDisplay()).showLoading(this);
	}
	
	@Override
	public void hideLoading() {
		((SynapseSuggestionDisplay) getSuggestionDisplay()).hideLoading();
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void setPlaceholderText(String text) {
		getValueBox().getElement().setAttribute("placeholder", text);
	}
	
	@Override
	public void setDisplayWidth(String width) {
		setWidth(width);
	}
	
	@Override
	public int getWidth() {
		return getOffsetWidth();
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		getUserGroupSuggestOracle().configure((UserGroupSuggestBox) presenter);
	}
	
	@Override
	public UserGroupSuggestOracle getUserGroupSuggestOracle() {
		return (UserGroupSuggestOracle) getSuggestOracle();
	}
}
