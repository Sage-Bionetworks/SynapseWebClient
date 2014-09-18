package org.sagebionetworks.web.client.widget.sharing;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;

//TODO: PUT THIS ELSEWHERE??
public class UserGroupSuggestOracle extends SuggestOracle {
	public static final int DELAY = 750;
	public static final int PAGE_SIZE = 10;
	
	private int offset;
	
	private SuggestOracle.Request request;
	private SuggestOracle.Callback callback;
	
	private SuggestBox suggestBox;
	private SynapseClientAsync synapseClient;
	private String baseFileHandleUrl;
	private String baseProfileAttachmentUrl;
	
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
	
	public void configure(SuggestBox suggestBox, SynapseClientAsync synapseClient, String baseFileHandleUrl, String baseProfileAttachmentUrl) {
		this.suggestBox = suggestBox;
		this.synapseClient = synapseClient;
		this.baseFileHandleUrl = baseFileHandleUrl;
		this.baseProfileAttachmentUrl = baseProfileAttachmentUrl;
		
		suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				if (event.getSelectedItem() instanceof DownSuggestion) {
					offset += PAGE_SIZE;
					getSuggestions();
				}
			}
			
		});
		
		suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				if (event.getSelectedItem() instanceof UpSuggestion) {
					offset -= PAGE_SIZE;
					getSuggestions();
				}
			}
			
		});
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
		// TODO <HACKY>
		// Loading
		List<Suggestion> load = new LinkedList<Suggestion>();
		load.add(new LoadingSuggestion());
		Suggestion sugg = new LoadingSuggestion();
		// Set up response
		SuggestOracle.Response loadResponse = new SuggestOracle.Response(load);
		callback.onSuggestionsReady(null, loadResponse);
		// TODO </HACKY>
		
		String prefix = request.getQuery();
		final List<Suggestion> suggestions = new LinkedList<Suggestion>();
		
		synapseClient.getUserGroupHeadersByPrefix(prefix, PAGE_SIZE, offset, new AsyncCallback<UserGroupHeaderResponsePage>() {
			
			@Override
			public void onSuccess(UserGroupHeaderResponsePage result) {
				if (offset != 0)
					suggestions.add(new UpSuggestion());
				
				for (UserGroupHeader header : result.getChildren()) {
					suggestions.add(new UserGroupSuggestion(header, baseFileHandleUrl, baseProfileAttachmentUrl));
				}
				
				if (offset + PAGE_SIZE < result.getTotalNumberOfResults())
					suggestions.add(new DownSuggestion());
				
				// Set up response
				SuggestOracle.Response response = new SuggestOracle.Response(suggestions);
						    	   
				callback.onSuggestionsReady(request, response);
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				System.out.println("CRY!!");
				// TODO: Something appropriate.
			}

		});
		
	}

	public class LoadingSuggestion implements IsSerializable, Suggestion {
		
		@Override
		public String getDisplayString() {
			// TODO Auto-generated method stub
			return "Loading ... ";
		}
		
		@Override
		public String getReplacementString() {
			// TODO Auto-generated method stub
			return "OH NO DON't CLICK ME!!";
		}
		
	}
	
	public class DownSuggestion implements IsSerializable, Suggestion {
		
		@Override
		public String getDisplayString() {
			return "DOWN";
		}
		
		@Override
		public String getReplacementString() {
			return suggestBox.getText();
		}
		
	}
	
	public class UpSuggestion implements IsSerializable, Suggestion {
		
		@Override
		public String getDisplayString() {
			return "UP";
		}
		
		@Override
		public String getReplacementString() {
			return suggestBox.getText();
		}
		
	}
	
	public class UserGroupSuggestion implements IsSerializable, Suggestion {

		private UserGroupHeader header;
		private String baseFileHandleUrl;
		private String baseProfileAttachmentUrl;
		
		// Required for IsSerializable to work
		//public UserGroupSuggestion() {}	// TODO: this? I don't think so.

		// Convenience method for creation of a suggestion
		public UserGroupSuggestion(UserGroupHeader header, String baseFileHandleUrl, String baseProfileAttachmentUrl) {
			this.header = header;
			this.baseFileHandleUrl = baseFileHandleUrl;
			this.baseProfileAttachmentUrl = baseProfileAttachmentUrl;
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
	
	private static native String getTemplate(String baseFileHandleUrl, String baseProfileAttachmentUrl) /*-{
		return [ '<tpl for=".">',
				'<div class="margin-left-5" style="height:23px">',
				'<img class="margin-right-5 vertical-align-center tiny-thumbnail-image-container" onerror="this.style.display=\'none\';" src="',
				'<tpl if="isIndividual">',
					baseProfileAttachmentUrl,
					'?userId={ownerId}&waitForUrl=true" />',
				'</tpl>',
				
				'<tpl if="!isIndividual">',
					baseFileHandleUrl,
					'?teamId={ownerId}" />',
			    '</tpl>',
				'<span class="search-item movedown-1 margin-right-5">',
				'<span class="font-italic">{firstName} {lastName} </span> ',
				'<span>{userName} </span> ',
				'</span>',
				'<tpl if="!isIndividual">',
			        '(Team)',
			    '</tpl>',
				
				'</div>',
				'</tpl>' ].join("");
				
	}-*/;
	
}
