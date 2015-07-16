package org.sagebionetworks.web.client.widget.search;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.inject.Inject;

public class GroupSuggestionProvider implements SuggestionProvider {

	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils jsniUtils;
	// for rendering
	private String width;
	
	@Inject
	public GroupSuggestionProvider(SynapseClientAsync synapseClient, SynapseJSNIUtils jsniUtils) {
		this.synapseClient = synapseClient;
		this.jsniUtils = jsniUtils;
	}
	
	@Override
	public void getSuggestions(final int offset, final int pageSize, int width, final String prefix, final AsyncCallback<SynapseSuggestionBundle> callback) {
		this.width = String.valueOf(width);
		synapseClient.getTeamsBySearch(prefix, pageSize, offset, new AsyncCallback<PaginatedResults<Team>>() {
			@Override
			public void onSuccess(PaginatedResults<Team> result) {
				List<SynapseSuggestion> suggestions = new LinkedList<SynapseSuggestion>();
				for (Team team: result.getResults()) {
					suggestions.add(new GroupSuggestion(team, prefix));
				}
				SynapseSuggestionBundle suggestionBundle = new SynapseSuggestionBundle(suggestions, result.getTotalNumberOfResults());
				callback.onSuccess(suggestionBundle);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
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
		
		public Team getTeam()		{	return team;			}
		public String getPrefix() 				{	return prefix;			}
		public void setPrefix(String prefix)	{	this.prefix = prefix;	}
		
		@Override
		public String getDisplayString() {
			StringBuilder result = new StringBuilder();
			result.append("<div class=\"padding-left-5 userGroupSuggestion\" style=\"height:23px; width:" + width + "px;\">");
			result.append("<img class=\"margin-right-5 vertical-align-center tiny-thumbnail-image-container\" onerror=\"this.style.display=\'none\';\" src=\"");
			result.append(jsniUtils.getBaseFileHandleUrl());
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
			return team.getName();
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
