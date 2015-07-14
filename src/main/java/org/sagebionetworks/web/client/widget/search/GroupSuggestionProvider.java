package org.sagebionetworks.web.client.widget.search;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.inject.Inject;

public class GroupSuggestionProvider implements SuggestionProvider {

	private SynapseClientAsync synapseClient;
	// for rendering
	private String width;
	private String baseFileHandleUrl;
	private String baseProfileAttachmentUrl;
	
	@Inject
	public GroupSuggestionProvider(SynapseClientAsync synapseClient) {
		this.synapseClient = synapseClient;
	}
	
	@Override
	public void getSuggestions(final int offset, final int pageSize, final String prefix, final CallbackP<List<SynapseSuggestion>> callback) {
		synapseClient.getTeamsBySearch(prefix, pageSize, offset, new AsyncCallback<PaginatedResults<Team>>() {
			@Override
			public void onSuccess(PaginatedResults<Team> result) {
				List<SynapseSuggestion> suggestions = new LinkedList<SynapseSuggestion>();
				for (Team team: result.getResults()) {
					suggestions.add(makeTeamSuggestion(team, prefix));
				}
				callback.invoke(suggestions);
			}
			@Override
			public void onFailure(Throwable caught) {
				// how to invoke the outer block?	
			}
		});
	}
	
	private SynapseSuggestion makeTeamSuggestion(Team team, String prefix) {
		return null;
	}

	@Override
	public void configure(String width, String baseFileHandleUrl,
			String baseProfileAttachmentUrl) {
		this.width = width;
		this.baseFileHandleUrl = baseFileHandleUrl;
		this.baseProfileAttachmentUrl = baseProfileAttachmentUrl;
	}
	
	/*
	 * Suggestion
	 */
	public class GroupSuggestion implements IsSerializable, SynapseSuggestion {
		private Team team;
		private String prefix;
		
		public GroupSuggestion(Team team, String prefix) {
			this.team = team;
			this.prefix = prefix;
		}
		
		public Team getHeader()		{	return team;			}
		public String getPrefix() 				{	return prefix;			}
		public void setPrefix(String prefix)	{	this.prefix = prefix;	}
		
		@Override
		public String getDisplayString() {
//			return DisplayUtils.getUserGroupDisplaySuggestionHtml(header, width + "px",
//					baseFileHandleUrl, baseProfileAttachmentUrl);
			StringBuilder result = new StringBuilder();
			result.append("<div class=\"padding-left-5 userGroupSuggestion\" style=\"height:23px; width:" + width + ";\">");
			result.append("<img class=\"margin-right-5 vertical-align-center tiny-thumbnail-image-container\" onerror=\"this.style.display=\'none\';\" src=\"");
			result.append(baseFileHandleUrl);
			result.append("?teamId=" + team.getId() + "\" />");
			result.append("<span class=\"search-item movedown-1 margin-right-5\">");
			result.append("<span>" + team.getName() + "</span> ");
			result.append("</span>");
			result.append("(Team)");			
			result.append("</div>");
			return result.toString();
		
		}

		@Override
		public String getReplacementString() {
			// Example output:
			// Team Sage  |  114085
			return team.getName() + "  |  " + team.getId();
		}

		@Override
		public String getId() {
			return team.getId();
		}

		@Override
		public String isIndividual() {
			return "false";
		}
		
	}

}
