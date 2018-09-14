package org.sagebionetworks.web.client.widget.search;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.SuggestBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseSuggestBoxViewImpl extends FlowPanel implements SynapseSuggestBoxView {
	
	private Presenter presenter;
	SuggestBox suggestBox;
	TextBox selectedItem;
	Text selectedItemText;
	SynapseAlert synAlert;
	@Inject
	public SynapseSuggestBoxViewImpl(SynapseAlert synAlert) {
		this.synAlert = synAlert;
	}

	@Override
	public void configure(SynapseSuggestOracle oracle) {
		final TextBox suggestTextBox = new TextBox();
		suggestTextBox.getElement().setAttribute("name", "address");
		suggestBox = new SuggestBox(oracle, suggestTextBox, new SynapseSuggestionDisplay());
		suggestBox.getValueBox().addStyleName("form-control");
		suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				selectSuggestion((UserGroupSuggestion)event.getSelectedItem());
			}
		});
		selectedItem = new TextBox();
		selectedItem.setVisible(false);
		selectedItem.getElement().setAttribute("name", "code");
		selectedItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				suggestBox.setVisible(true);
				selectedItem.setVisible(false);
				selectedItem.setText("");
				if (presenter.getSelectedSuggestion() != null) {
					// is this the same text that weas being filled in before?
//					suggestBox.setText(selectedItem.getText());
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
		selectedItemText = new Text();
		this.add(suggestBox);
		this.add(selectedItem);
		this.add(selectedItemText);
		this.add(synAlert.asWidget());
	}
	
	@Override
	public void setFocus(boolean focused) {
		suggestBox.getValueBox().setFocus(true);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void updateFieldStateForSuggestions(int numResults, int offset) {
		Button prevBtn = ((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).getPrevButton();
		Button nextBtn = ((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).getNextButton();
		Label resultsLbl = ((SynapseSuggestionDisplay) suggestBox.getSuggestionDisplay()).getResultsLabel();
		
		prevBtn.setEnabled(offset != 0);
		boolean moreResults = offset + SynapseSuggestBox.PAGE_SIZE < numResults;
		nextBtn.setEnabled(moreResults);
		
		String resultsLabel = "Displaying " + (offset + 1) + " - "
								+ (moreResults ? offset + SynapseSuggestBox.PAGE_SIZE : numResults);
		resultsLbl.setText(resultsLabel);
	}
	
	public void clear() {
		presenter.setSelectedSuggestion(null);
		suggestBox.getValueBox().setText(null);	// Empty text box
		suggestBox.setVisible(true);
		selectedItem.setVisible(false);
		selectedItem.setText("");
		selectedItemText.setText("");
	}
	
	public void selectSuggestion(UserGroupSuggestion suggestion) {
		// Update the SuggestBox's selected suggestion.
		synAlert.clear();
		selectedItem.setText(suggestion.getReplacementString());
		selectedItem.setVisible(true);
		suggestBox.setVisible(false);
		presenter.setSelectedSuggestion(suggestion);
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
		synAlert.showError(message);
	}
	
	@Override
	public void setPlaceholderText(String text) {
		selectedItem.getElement().setAttribute("placeholder", text);
		suggestBox.getValueBox().getElement().setAttribute("placeholder", text);
	}
	
	@Override
	public void setText(String text) {
		suggestBox.setText(text);;
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
	}
	
	@Override
	public SynapseSuggestOracle getUserGroupSuggestOracle() {
		return (SynapseSuggestOracle) suggestBox.getSuggestOracle();
	}
	
	@Override
	public String getText() {
		return suggestBox.getText();
	}

	@Override
	public void setSelectedText(String displayString) {
		selectedItemText.setText(displayString);
	}
	
	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return suggestBox.getValueBox().addKeyDownHandler(handler);
	}

	@Override
	public int getTabIndex() {
		return suggestBox.getValueBox().getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		suggestBox.getValueBox().setAccessKey(key);
	}

	@Override
	public void setTabIndex(int index) {
		suggestBox.getValueBox().setTabIndex(index);
	}
	
}
