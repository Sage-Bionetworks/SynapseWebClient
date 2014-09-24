package org.sagebionetworks.web.client.widget.search;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.gwt.HTMLPanel;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox.UserGroupSuggestOracle;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox.UserGroupSuggestOracle.UserGroupSuggestion;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlowPanel;
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
		super(oracle, new TextBox(), new UserGroupSuggestionDisplay(sageImageBundle));
		getElement().setAttribute("placeHolder", "Enter Name...");
		addStyleName("userGroupSuggestBox");
		
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
		((UserGroupSuggestionDisplay) getSuggestionDisplay()).getPrevButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.getPrevSuggestions();
			}
			
		});
		
		// Next suggestions button.
		((UserGroupSuggestionDisplay) getSuggestionDisplay()).getNextButton().addClickHandler(new ClickHandler() {

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
		Button prevBtn = ((UserGroupSuggestionDisplay) getSuggestionDisplay()).getPrevButton();
		Button nextBtn = ((UserGroupSuggestionDisplay) getSuggestionDisplay()).getNextButton();
		Label resultsLbl = ((UserGroupSuggestionDisplay) getSuggestionDisplay()).getResultsLabel();
		
		prevBtn.setEnabled(offset != 0);
		boolean moreResults = offset + UserGroupSuggestBox.PAGE_SIZE < responsePage.getTotalNumberOfResults();
		nextBtn.setEnabled(moreResults);
		
		String resultsLabel = "Displaying " + (offset + 1) + " - " + (moreResults ? offset + UserGroupSuggestBox.PAGE_SIZE : responsePage.getTotalNumberOfResults())
								+ " of " + responsePage.getTotalNumberOfResults();
		resultsLbl.setText(resultsLabel);
	}
	
	public void clear() {
		presenter.setSelectedSuggestion(null);
		getValueBox().setText(null);
	}
	
	public void selectSuggestion(UserGroupSuggestion suggestion) {
		getValueBox().setFocus(false);
		
		// Update the SuggestBox's selected suggestion.
		presenter.setSelectedSuggestion(suggestion);
		setText(suggestion.getReplacementString());
	}
	
	@Override
	public void showLoading() {
		((UserGroupSuggestionDisplay) getSuggestionDisplay()).showLoading(this);
	}
	
	@Override
	public void hideLoading() {
		((UserGroupSuggestionDisplay) getSuggestionDisplay()).hideLoading();
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
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		getUserGroupSuggestOracle().configure(this, (UserGroupSuggestBox) presenter);
	}
	
	@Override
	public UserGroupSuggestOracle getUserGroupSuggestOracle() {
		return (UserGroupSuggestOracle) getSuggestOracle();
	}
	
	
	/*
	 * SuggestionDisplay
	 */
	public static class UserGroupSuggestionDisplay extends SuggestBox.DefaultSuggestionDisplay {
		private SageImageBundle sageImageBundle;
		
		private Label resultsLabel;
		private ButtonGroup buttonGroup;
		private Button prevButton;
		private Button nextButton;
		
		private Widget popupContents; // to save when loading.
		
		private HTMLPanel loadingPanel;
		
		public UserGroupSuggestionDisplay(SageImageBundle sageImageBundle) {
			super();
			this.sageImageBundle = sageImageBundle;
		}
		
		@Override
		protected Widget decorateSuggestionList(Widget suggestionList) {
			setUpFields();
			FlowPanel suggestList = new FlowPanel();
			suggestList.add(suggestionList);
			
			FlowPanel pagingArea = new FlowPanel();
			pagingArea.addStyleName("userGroupSuggestionPagingArea");
			pagingArea.add(buttonGroup);
			pagingArea.add(resultsLabel);
			suggestList.add(pagingArea);
			return suggestList;
		}
		
		public Label getResultsLabel()	{	return resultsLabel;	}
		public Button getPrevButton()	{	return prevButton;		}
		public Button getNextButton()	{	return nextButton;		}
		public Widget getPopupContents(){	return popupContents;	}
		
		
		public void showLoading(UserGroupSuggestBoxViewImpl suggestBox) {
			popupContents = getPopupPanel().getWidget();
			if (loadingPanel == null) {
				loadingPanel = new HTMLPanel(DisplayUtils.getLoadingHtml(sageImageBundle));
				loadingPanel.setWidth(suggestBox.getOffsetWidth() + "px");
			}
			getPopupPanel().setWidget(loadingPanel);
			getPopupPanel().showRelativeTo(suggestBox);
		}
		
		public void hideLoading() {
			getPopupPanel().setWidget(popupContents);
		}
		
		
		private void setUpFields() {
			resultsLabel = new Label();
			resultsLabel.addStyleName("userGroupSuggesionResultsLabel");
			
			prevButton = new Button("Prev");
			prevButton.setEnabled(false);
			nextButton = new Button("Next");
			
			buttonGroup = new ButtonGroup();
			buttonGroup.addStyleName("btn-group btn-group-xs userGroupSuggestionPager");
			buttonGroup.add(prevButton);
			buttonGroup.add(nextButton);
		}
	} // end of inner class UserGroupSuggestionDisplay
}
