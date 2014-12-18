package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle;

public class UserGroupSuggestOracle extends SuggestOracle {
	private SuggestOracle.Request request;
	private SuggestOracle.Callback callback;
	
	private UserGroupSuggestBox suggestBox;
	
	public void configure(UserGroupSuggestBox suggestBox) {
		this.suggestBox = suggestBox;
	}
	
	private Timer timer = new Timer() {

		@Override
		public void run() {
			
			// If you backspace quickly the contents of the field are emptied but a
			// query for a single character is still executed. Workaround for this
			// is to check for an empty string field here.
			if (!suggestBox.getText().trim().isEmpty()) {
				suggestBox.setOffset(0);
				suggestBox.getSuggestions(request, callback);
			}
		}
		
	};
	
	@Override
	public boolean isDisplayStringHTML() {
		return true;
	}
	
	@Override
	public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
		this.request = request;
		this.callback = callback;
		
		timer.cancel();
		timer.schedule(UserGroupSuggestBox.DELAY);
	}
	
	public SuggestOracle.Request getRequest()	{	return request;		}
	public SuggestOracle.Callback getCallback()	{	return callback;	}
	
	public UserGroupSuggestion makeUserGroupSuggestion(UserGroupHeader header, String prefix) {
		return new UserGroupSuggestion(header, prefix);
	}


	/*
	 * Suggestion
	 */
	public class UserGroupSuggestion implements IsSerializable, Suggestion {
		private UserGroupHeader header;
		private String prefix;
		
		public UserGroupSuggestion(UserGroupHeader header, String prefix) {
			this.header = header;
			this.prefix = prefix;
		}
		
		public UserGroupHeader getHeader()		{	return header;			}
		public String getPrefix() 				{	return prefix;			}
		public void setPrefix(String prefix)	{	this.prefix = prefix;	}
		
		@Override
		public String getDisplayString() {
			return DisplayUtils.getUserGroupDisplaySuggestionHtml(header, suggestBox.getWidth() + "px",
					suggestBox.getBaseFileHandleUrl(), suggestBox.getBaseProfileAttachmentUrl());
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
}
