package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamMembersWidget implements WidgetRendererPresenter, PageChangeListener {
	
	private TeamMembersWidgetView view;
	private Map<String,String> descriptor;
	private SynapseClientAsync synapseClient;
	private String teamId;
	private SynapseAlert synAlert;
	private PortalGinInjector ginInjector;
	private BasicPaginationWidget paginationWidget;
	public static final Long DEFAULT_USER_LIMIT = 30L;
	public static final Long DEFAULT_OFFSET = 0L;
	
	@Inject
	public TeamMembersWidget(TeamMembersWidgetView view, 
			BasicPaginationWidget paginationWidget, 
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector
			) {
		this.view = view;
		this.paginationWidget = paginationWidget;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		view.setPaginationWidget(paginationWidget);
		view.setSynapseAlert(synAlert);
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		synAlert.clear();
		this.descriptor = widgetDescriptor;
		teamId = descriptor.get(WidgetConstants.TEAM_ID_KEY);
		descriptor = widgetDescriptor;
		if (teamId == null) {
			synAlert.showError(WidgetConstants.TEAM_ID_KEY + " is required.");
		} else {
			//get the team members
			onPageChange(DEFAULT_OFFSET);
		}
	}
	
	@Override
	public void onPageChange(final Long newOffset) {
		synAlert.clear();
		view.clearRows();
		view.setLoadingVisible(true);
		synapseClient.getTeamMembers(teamId, "", DEFAULT_USER_LIMIT.intValue(), newOffset.intValue(), new AsyncCallback<TeamMemberPagedResults>() {
			@Override
			public void onSuccess(TeamMemberPagedResults results) {
				//configure the pager, and the participant list
				long rowCount = new Integer(results.getResults().size()).longValue();
				paginationWidget.configure(DEFAULT_USER_LIMIT, newOffset, rowCount, TeamMembersWidget.this);
				for (TeamMemberBundle bundle : results.getResults()) {
					TeamMemberRowWidget row = ginInjector.getTeamMemberRowWidget();
					row.configure(bundle.getUserProfile());
					view.addRow(row);
				}
				view.setLoadingVisible(false);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.setLoadingVisible(false);
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
