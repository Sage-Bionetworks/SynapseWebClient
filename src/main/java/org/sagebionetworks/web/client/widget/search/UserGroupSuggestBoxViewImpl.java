package org.sagebionetworks.web.client.widget.search;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.SuggestBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracle.UserGroupSuggestion;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserGroupSuggestBoxViewImpl extends FlowPanel implements UserGroupSuggestBoxView {
	
	private Presenter presenter;
	SuggestBox suggestBox;
	TextBox selectedItem;
	
	@Inject
	public UserGroupSuggestBoxViewImpl(UserGroupSuggestOracle oracle, SageImageBundle sageImageBundle) {
		suggestBox = new SuggestBox(oracle, new TextBox(), new SynapseSuggestionDisplay(sageImageBundle));
		suggestBox.getValueBox().addStyleName("form-control");
		suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				selectSuggestion((UserGroupSuggestion) event.getSelectedItem());
			}
			
		});
		
		selectedItem = new TextBox();
		selectedItem.setVisible(false);
		
		selectedItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				suggestBox.setVisible(true);
				selectedItem.setVisible(false);
				selectedItem.setText("");
				if (presenter.getSelectedSuggestion() != null) {
					suggestBox.setText(presenter.getSelectedSuggestion().getPrefix());
				}
				suggestBox.showSuggestionList();
			}
		});
		
		// Previous suggestions button.
		((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).getPrevButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.getPrevSuggestions();
			}
			
		});
		
		// Next suggestions button.
		((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).getNextButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.getNextSuggestions();
			}
			
		});
		this.add(suggestBox);
		this.add(selectedItem);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void updateFieldStateForSuggestions(UserGroupHeaderResponsePage responsePage, int offset) {
		Button prevBtn = ((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).getPrevButton();
		Button nextBtn = ((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).getNextButton();
		Label resultsLbl = ((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).getResultsLabel();
		
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
		suggestBox.getValueBox().setText(null);	// Empty text box
		suggestBox.setVisible(true);
		selectedItem.setVisible(false);
		selectedItem.setText("");
	}
	
	public void selectSuggestion(UserGroupSuggestion suggestion) {
		// Update the SuggestBox's selected suggestion.
		presenter.setSelectedSuggestion(suggestion);
		selectedItem.setText(suggestion.getReplacementString());
		selectedItem.setVisible(true);
		suggestBox.setVisible(false);
	}
	
	@Override
	public void showLoading() {
		((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).showLoading(this);
	}
	
	@Override
	public void hideLoading() {
		((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).hideLoading();
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
		selectedItem.getElement().setAttribute("placeholder", text);
		suggestBox.getValueBox().getElement().setAttribute("placeholder", text);
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
		return (UserGroupSuggestOracle) suggestBox.getSuggestOracle();
	}
	@Override
	public String getText() {
		return suggestBox.getText();
	}
}
