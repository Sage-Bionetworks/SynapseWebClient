package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.SuggestBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxOracle.EntitySearchBoxSuggestion;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestionDisplay;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget is a Synapse entity Id search box
 *
 */
public class EntitySearchBoxViewImpl extends FlowPanel implements EntitySearchBoxView, IsWidget {

	private Presenter presenter;
	SuggestBox suggestBox;
	TextBox selectedItem;
	
	@Inject
	public EntitySearchBoxViewImpl(
			EntitySearchBoxOracle oracle) {
		suggestBox = new SuggestBox(oracle, new TextBox(), new SynapseSuggestionDisplay());
		suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				selectSuggestion((EntitySearchBoxSuggestion) event.getSelectedItem());
			}
			
		});
		
		selectedItem = new TextBox();
		selectedItem.setVisible(false);
		selectedItem.getElement().setAttribute("placeholder", "Enter search terms");
		
		selectedItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				suggestBox.setVisible(true);
				selectedItem.setVisible(false);
				selectedItem.setText("");
				if (presenter.getSelectedSuggestion() != null) {
					suggestBox.setText(presenter.getSelectedSuggestion().getPrefix());
				}
				suggestBox.setFocus(true);
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
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		getOracle().configure((EntitySearchBox) presenter);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void updateFieldStateForSuggestions(SearchResults responsePage, long offset) {
		Button prevBtn = ((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).getPrevButton();
		Button nextBtn = ((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).getNextButton();
		Label resultsLbl = ((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).getResultsLabel();
		
		prevBtn.setEnabled(offset != 0);
		boolean moreResults = offset + EntitySearchBox.PAGE_SIZE < responsePage.getFound();
		nextBtn.setEnabled(moreResults);
		
		String resultsLabel = "Displaying " + (offset + 1) + " - "
								+ (moreResults ? offset + EntitySearchBox.PAGE_SIZE : responsePage.getFound())
								+ " of " + responsePage.getFound();
		resultsLbl.setText(resultsLabel);
	}
	
	public void clear() {
		presenter.setSelectedSuggestion(null);
		suggestBox.getValueBox().setText(null); // Empty text box
		suggestBox.setVisible(true);
		selectedItem.setVisible(false);
		selectedItem.setText("");
	}
	
	public void selectSuggestion(EntitySearchBoxSuggestion suggestion) {
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void setDisplayWidth(int width) {
		setWidth(Integer.toString(width));
	}
	
	@Override
	public int getWidth() {
		return getOffsetWidth();
	}
	
	@Override
	public EntitySearchBoxOracle getOracle() {
		return (EntitySearchBoxOracle) suggestBox.getSuggestOracle();
	}

	@Override
	public String getText() {
		return suggestBox.getText();
	}
	
}
