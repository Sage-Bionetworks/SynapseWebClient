package org.sagebionetworks.web.client.widget.entity;

import java.util.LinkedList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.gwt.HTMLPanel;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class UserGroupSuggestBox extends SuggestBox {
	private UserGroupSuggestOracle.UserGroupSuggestion selectedSuggestion;
	
	public UserGroupSuggestBox(UserGroupSuggestOracle oracle, UserGroupSuggestionDisplay display) {
		super(oracle, new TextBox(), display);
		oracle.setDisplay(display);
		display.setOracle(oracle);
		getElement().setAttribute("placeHolder", "Enter Name...");
	}
	
	public UserGroupSuggestOracle.UserGroupSuggestion getSelectedUserGroupSuggestion() {
		return selectedSuggestion;
	}
	
	public void clear() {
		setSelectedUserGroupSuggestion(null);
		getValueBox().setText(null);
	}
	
	private void setSelectedUserGroupSuggestion(UserGroupSuggestOracle.UserGroupSuggestion selectedSuggestion) {
		this.selectedSuggestion = selectedSuggestion;
	}
	
	
	/*
	 * SuggestionDisplay
	 */
	public static class UserGroupSuggestionDisplay extends SuggestBox.DefaultSuggestionDisplay {
		private UserGroupSuggestOracle oracle;
		private Label resultsLabel;
		private Button prevButton;
		private Button nextButton;
		private Widget popupContents; // to save when loading.
		private SageImageBundle sageImageBundle;
		
		public UserGroupSuggestionDisplay(SageImageBundle sageImageBundle) {
			super();
			this.sageImageBundle = sageImageBundle;
		}
		
		@Override
		protected Widget decorateSuggestionList(Widget suggestionList) {
			FlowPanel suggestList = new FlowPanel();
			suggestList.add(suggestionList);
			
			FlowPanel pagingArea = new FlowPanel();
			pagingArea.addStyleName("userGroupSuggestionPagingArea");
			pagingArea.add(createPager());
			pagingArea.add(resultsLabel);
			suggestList.add(pagingArea);
			return suggestList;
		}
		
		public Label getResultsLabel()	{	return resultsLabel;	}
		public Button getPrevButton()	{	return prevButton;		}
		public Button getNextButton()	{	return nextButton;		}
		
		public void setOracle(UserGroupSuggestOracle oracle) {
			this.oracle = oracle;
		}
		
		public void showLoading() {
			popupContents = getPopupPanel().getWidget();
			HTMLPanel loading = new HTMLPanel(DisplayUtils.getLoadingHtml(sageImageBundle));
			loading.setWidth(oracle.getSuggestBox().getOffsetWidth() + "px");
			getPopupPanel().setWidget(loading);
			getPopupPanel().showRelativeTo(oracle.getSuggestBox());
		}
		
		public void hideLoading() {
			getPopupPanel().setWidget(popupContents);
		}
		
		private ButtonGroup createPager() {
			setUpFields();
			ButtonGroup group = new ButtonGroup();
			group.addStyleName("btn-group btn-group-xs userGroupSuggestionPager");
			group.add(prevButton);
			group.add(nextButton);
			
			return group;
		}
		
		private void setUpFields() {
			resultsLabel = new Label();
			resultsLabel.addStyleName("userGroupSuggesionResultsLabel");
			prevButton = new Button("Prev");
			prevButton.setEnabled(false);
			nextButton = new Button("Next");
			
			prevButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					oracle.getPrevSuggestions();
				}
				
			});
			
			nextButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					oracle.getNextSuggestions();
				}
				
			});
		}
	} // end of inner class UserGroupSuggestionDisplay
	
	
	/*
	 * SuggestOracle
	 */
	public static class UserGroupSuggestOracle extends SuggestOracle {
		public static final int DELAY = 750;
		public static final int PAGE_SIZE = 10;
		
		private int offset;
		
		private SuggestOracle.Request request;
		private SuggestOracle.Callback callback;
		
		private UserGroupSuggestBox suggestBox;
		private SynapseClientAsync synapseClient;
		private String baseFileHandleUrl;
		private String baseProfileAttachmentUrl;
		private UserGroupSuggestionDisplay display;
		
		private Timer timer = new Timer() {

			@Override
			public void run() {
				
				// If you backspace quickly the contents of the field are emptied but a
				// query for a single character is still executed. Workaround for this
				// is to check for an empty string field here.
				if (suggestBox != null && !suggestBox.getText().trim().isEmpty()) {
					offset = 0;
					getSuggestions();
				}
			}
			
		};
		
		public void setDisplay(UserGroupSuggestionDisplay display) {
			this.display = display;
		}
		
		public void configure(final UserGroupSuggestBox suggestBox, SynapseClientAsync synapseClient, String baseFileHandleUrl, String baseProfileAttachmentUrl) {
			this.suggestBox = suggestBox;
			this.synapseClient = synapseClient;
			this.baseFileHandleUrl = baseFileHandleUrl;
			this.baseProfileAttachmentUrl = baseProfileAttachmentUrl;
			
			suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

				@Override
				public void onSelection(SelectionEvent<Suggestion> event) {
					UserGroupSuggestion suggestion = (UserGroupSuggestion) event.getSelectedItem();
					suggestBox.getValueBox().setFocus(false);
					
					// Update the SuggestBox's selected suggestion.
					suggestBox.setSelectedUserGroupSuggestion(suggestion);
					suggestBox.setText(suggestion.getReplacementString());
				}
				
			});
			
			suggestBox.getValueBox().addFocusHandler(new FocusHandler() {

				@Override
				public void onFocus(FocusEvent event) {
					if (suggestBox.getSelectedUserGroupSuggestion() != null) {
						
						// If a user/group is selected, the text in the input box should not
						// be editable. If the user tries to edit it (focus event on value box),
						// the text will revert to what it was before they selected the element.
						suggestBox.setText(suggestBox.getSelectedUserGroupSuggestion().getPrefix());
						suggestBox.showSuggestionList();
						suggestBox.setSelectedUserGroupSuggestion(null);
					}
				}
				
			});
		}
		
		public UserGroupSuggestBox getSuggestBox() {
			return suggestBox;
		}
		
		public void getNextSuggestions() {
			offset += PAGE_SIZE;
			getSuggestions();
		}
		
		public void getPrevSuggestions() {
			offset -= PAGE_SIZE;
			getSuggestions();
		}
		
		public boolean isDisplayStringHTML() {
			return true;
		}
		
		public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
			this.request = request;
			this.callback = callback;
			
			timer.cancel();
			timer.schedule(DELAY);
		}
		
		public void getSuggestions() {
			display.showLoading();
			
			String prefix = request.getQuery();
			final List<Suggestion> suggestions = new LinkedList<Suggestion>();
			
			synapseClient.getUserGroupHeadersByPrefix(prefix, PAGE_SIZE, offset, new AsyncCallback<UserGroupHeaderResponsePage>() {
				@Override
				public void onSuccess(UserGroupHeaderResponsePage result) {
					// Update display fields.
					display.getPrevButton().setEnabled(offset != 0);
					boolean moreResults = offset + PAGE_SIZE < result.getTotalNumberOfResults();
					display.getNextButton().setEnabled(moreResults);
					String resultsLabel = "Displaying " + (offset + 1) + " - " + (moreResults ? offset + PAGE_SIZE : result.getTotalNumberOfResults())
											+ " of " + result.getTotalNumberOfResults();
					display.getResultsLabel().setText(resultsLabel);
					
					// Load suggestions.
					for (UserGroupHeader header : result.getChildren()) {
						suggestions.add(new UserGroupSuggestion(header, baseFileHandleUrl, baseProfileAttachmentUrl));
					}

					// Set up response
					SuggestOracle.Response response = new SuggestOracle.Response(suggestions);
					callback.onSuggestionsReady(request, response);
					
					display.hideLoading();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if (caught != null) {
						DisplayUtils.showErrorMessage(caught.getMessage());
					}
				}

			});
			
		}
		
		/*
		 * Suggestion
		 */
		public class UserGroupSuggestion implements IsSerializable, Suggestion {
			private UserGroupHeader header;
			private String prefix;
			private String baseFileHandleUrl;
			private String baseProfileAttachmentUrl;
			
			public UserGroupSuggestion(UserGroupHeader header, String baseFileHandleUrl, String baseProfileAttachmentUrl) {
				this.header = header;
				this.baseFileHandleUrl = baseFileHandleUrl;
				this.baseProfileAttachmentUrl = baseProfileAttachmentUrl;
				prefix = suggestBox.getText();
			}
			
			public UserGroupHeader getHeader()		{	return header;			}
			public String getPrefix() 				{	return prefix;			}
			public void setPrefix(String prefix)	{	this.prefix = prefix;	}
			
			@Override
			public String getDisplayString() {
				StringBuilder result = new StringBuilder();
				result.append("<div class=\"padding-left-5 userGroupSuggestion\" style=\"height:23px; width:375px;\">");
				result.append("<img class=\"margin-right-5 vertical-align-center tiny-thumbnail-image-container\" onerror=\"this.style.display=\'none\';\" src=\"");
				if (header.getIsIndividual()) {
					result.append(baseProfileAttachmentUrl);
					result.append("?userId=" + header.getOwnerId() + "&waitForUrl=true\" />");
				} else {
					result.append(baseFileHandleUrl);
					result.append("?teamId=" + header.getOwnerId() + "\" />");
				}
				result.append("<span class=\"search-item movedown-1 margin-right-5\">");
				if (header.getIsIndividual()) {	// TODO: This?? Seems like it.
					result.append("<span class=\"font-italic\">" + header.getFirstName() + " " + header.getLastName() + "</span> ");
				}
				result.append("<span>" + header.getUserName() + "</span> ");
				result.append("</span>");
				if (!header.getIsIndividual()) {
					result.append("(Team)");
				}
				result.append("</div>");
				return result.toString();
			}

			@Override
			public String getReplacementString() {
				// Example output:
				// Pac Man  |  114085
				StringBuilder sb = new StringBuilder();
				if (!header.getIsIndividual())
					sb.append("(Team) ");
				
				String firstName = header.getFirstName();
				String lastName = header.getLastName();
				String username = header.getUserName();
				sb.append(DisplayUtils.getDisplayName(firstName, lastName, username));
				sb.append("  |  " + header.getOwnerId());
				return sb.toString();
			}
			
		} // end inner class UserGroupSuggestion	
	} // end inner class UserGroupSuggestOracle
}
