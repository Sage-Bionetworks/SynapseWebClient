package org.sagebionetworks.web.client.widget.entity;

import java.util.LinkedList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.Pager;
import org.gwtbootstrap3.client.ui.html.UnorderedList;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class UserGroupSuggestBox extends SuggestBox {
	private UserGroupSuggestOracle.UserGroupSuggestion selectedSuggestion;
	
	public UserGroupSuggestBox(SuggestOracle oracle) {
		super(oracle);
	}
	
	public UserGroupSuggestBox(UserGroupSuggestOracle oracle, UserGroupSuggestionDisplay display) {
		super(oracle, new TextBox(), display);
		oracle.setDisplay(display);
		display.setOracle(oracle);
	}
	
	public UserGroupSuggestOracle.UserGroupSuggestion getSelectedUserGroupSuggestion() {
		return selectedSuggestion;
	}
	
	private void setSelectedUserGroupSuggestion(UserGroupSuggestOracle.UserGroupSuggestion selectedSuggestion) {
		this.selectedSuggestion = selectedSuggestion;
	}
	
	public static class UserGroupSuggestionDisplay extends SuggestBox.DefaultSuggestionDisplay {
		private UserGroupSuggestOracle oracle;
		private Label resultsLabel;
		private org.gwtbootstrap3.client.ui.Button prevButton;
		private org.gwtbootstrap3.client.ui.Button nextButton;
		private Widget popupContents; // to save when loading.
		
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
		
		public void setOracle(UserGroupSuggestOracle oracle) {
			this.oracle = oracle;
		}
		
		public void showLoading() {
			popupContents = getPopupPanel().getWidget();
			getPopupPanel().setWidget(new Label("Loading..."));
			getPopupPanel().showRelativeTo(oracle.getSuggestBox());
		}
		
		public void hideLoading() {
			getPopupPanel().setWidget(popupContents);
		}
		
		public Label getResultsLabel() { return resultsLabel; }
		public Button getPrevButton() { return prevButton; }
		public Button getNextButton() { return nextButton; }
		
		private org.gwtbootstrap3.client.ui.ButtonGroup createPager() {
			
//			Button prevButton = new Button("Prev");
//			prevButton.setEnabled(false);
//			Button nextButton = new Button("Next");
//			ListItem prevItem = new ListItem();
//			ListItem nextItem = new ListItem();
//			prevItem.add(prevButton);
//			nextItem.add(nextButton);
//			UnorderedList list = new UnorderedList(prevItem, nextItem);
//			list.addStyleName("pager");
//			return list;
			setUpFields();
			org.gwtbootstrap3.client.ui.ButtonGroup group = new org.gwtbootstrap3.client.ui.ButtonGroup();
			group.addStyleName("btn-group btn-group-xs userGroupSuggestionPager");
			group.add(prevButton);
			group.add(nextButton);
			
			return group;
			
		}
		
		private void setUpFields() {
			resultsLabel = new Label();
			resultsLabel.addStyleName("userGroupSuggesionResultsLabel");
			prevButton = new org.gwtbootstrap3.client.ui.Button("Prev");
			prevButton.setEnabled(false);
			nextButton = new org.gwtbootstrap3.client.ui.Button("Next");
			
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
	}
	
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
				
				/* If you backspace quickly the contents of the field are emptied but a
				 * query for a single character is still executed. Workaround for this
				 * is to check for an empty string field here.
	             */
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
					if (event.getSelectedItem() instanceof UserGroupSuggestion) {
						UserGroupSuggestion suggestion = (UserGroupSuggestion) event.getSelectedItem();
						suggestBox.getValueBox().setFocus(false);
						// Update the SuggestBox's selected suggestion.
						suggestBox.setSelectedUserGroupSuggestion(suggestion);
						suggestBox.setText(suggestion.getReplacementString());
					}
				}
				
			});
			
			suggestBox.getValueBox().addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					if (suggestBox.getSelectedUserGroupSuggestion() != null) {
						suggestBox.setText(suggestBox.getSelectedUserGroupSuggestion().getPrefix());
						suggestBox.showSuggestionList();
						suggestBox.setSelectedUserGroupSuggestion(null);
					}
				}
				
			});
		}
		
		// For display showRelativeTo.
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
					display.getPrevButton().setEnabled(offset != 0);
					boolean moreResults = offset + PAGE_SIZE < result.getTotalNumberOfResults();
					display.getNextButton().setEnabled(moreResults);
					String resultsLabel = "Displaying " + (offset + 1) + " - " + (moreResults ? offset + PAGE_SIZE : result.getTotalNumberOfResults())
											+ " of " + result.getTotalNumberOfResults();
					display.getResultsLabel().setText(resultsLabel);
					
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
					System.out.println("CRY!!");
					// TODO: Something appropriate.
				}

			});
			
		}

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
			
			public String getDisplayString() {
				return getDisplayStringHtml();
			}

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
			
			public UserGroupHeader getHeader() {
				return header;
			}
			
			public String getPrefix() {
				return prefix;
			}
			
			public void setPrefix(String prefix) {
				this.prefix = prefix;
			}
			
			public String getDisplayStringHtml() {
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
			
		} // end inner class UserGroupSuggestion	
	}
}
