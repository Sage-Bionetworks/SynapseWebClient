package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamMemberCountWidget implements WidgetRendererPresenter {
	SynapseClientAsync synapseClient;
	TeamMemberCountView view;
	SynapseAlert synAlert;
	private String teamId;
	
	private static final Integer LIMIT = 1;
	private static final Integer OFFSET = 0;
	
	@Inject
	public TeamMemberCountWidget(TeamMemberCountView view,
		SynapseClientAsync synapseClient,
		SynapseAlert synAlert) {
		this.synapseClient = synapseClient;
		this.view = view;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert.asWidget());
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		synAlert.clear();
		teamId = widgetDescriptor.get(WidgetConstants.TEAM_ID_KEY);
		if (teamId == null) {
			synAlert.showError(WidgetConstants.TEAM_ID_KEY + " is required.");
		} else {
			synapseClient.getTeamMembers(teamId, "", LIMIT, OFFSET, new AsyncCallback<TeamMemberPagedResults>() {
				@Override
				public void onSuccess(TeamMemberPagedResults results) {
					view.setCount(results.getTotalNumberOfResults().toString());
				}
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
			});
		}
		
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
