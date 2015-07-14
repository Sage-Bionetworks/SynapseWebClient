package org.sagebionetworks.web.client.widget.search;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.inject.Inject;

public class UserGroupSuggestionProvider implements SuggestionProvider {
	
	private SynapseClientAsync synapseClient;
	// for rendering
	private String width;
	private String baseFileHandleUrl;
	private String baseProfileAttachmentUrl;
	
	@Inject
	public UserGroupSuggestionProvider(SynapseClientAsync synapseClient) {
		this.synapseClient = synapseClient;
	}
	
	@Override
	public void configure(String width, String baseFileHandleUrl, String baseProfileAttachmentUrl) {
		this.width = width;
		this.baseFileHandleUrl = baseFileHandleUrl;
		this.baseProfileAttachmentUrl = baseProfileAttachmentUrl;
	}
	
	@Override
	public void getSuggestions(final int offset, final int pageSize, final String prefix, final CallbackP<List<SynapseSuggestion>> callback) {
		synapseClient.getUserGroupHeadersByPrefix(prefix, pageSize, offset, new AsyncCallback<UserGroupHeaderResponsePage>() {
			@Override
			public void onSuccess(UserGroupHeaderResponsePage result) {
				List<SynapseSuggestion> suggestions = new LinkedList<SynapseSuggestion>();
				for (UserGroupHeader header: result.getChildren()) {
					suggestions.add(makeUserGroupSuggestion(header, prefix));
				}
				callback.invoke(suggestions);
			}
			@Override
			public void onFailure(Throwable caught) {
				// how to invoke the outer block?	
			}
		});
	}
	
	public SynapseSuggestion makeUserGroupSuggestion(UserGroupHeader header, String prefix) {
		return new UserGroupSuggestion(header, prefix);
	}
	
	/*
	 * Suggestion
	 */
	public class UserGroupSuggestion implements IsSerializable, SynapseSuggestion {
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
			StringBuilder result = new StringBuilder();
			result.append("<div class=\"padding-left-5 userGroupSuggestion\" style=\"height:23px; width:" + width + ";\">");
			result.append("<img class=\"margin-right-5 vertical-align-center tiny-thumbnail-image-container\" onerror=\"this.style.display=\'none\';\" src=\"");
			if (header.getIsIndividual()) {
				result.append(baseProfileAttachmentUrl);
				result.append("?userId=" + header.getOwnerId() + "&waitForUrl=true\" />");
			} else {
				result.append(baseFileHandleUrl);
				result.append("?teamId=" + header.getOwnerId() + "\" />");
			}
			result.append("<span class=\"search-item movedown-1 margin-right-5\">");
			if (header.getIsIndividual()) {
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

		@Override
		public String getId() {
			return header.getOwnerId();
		}

		@Override
		public String isIndividual() {
			return header.getIsIndividual().toString();
		}
		
	}
	
}
