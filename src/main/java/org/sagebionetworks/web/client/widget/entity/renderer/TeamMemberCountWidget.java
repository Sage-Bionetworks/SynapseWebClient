package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.Map;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamMemberCountWidget implements WidgetRendererPresenter {
	SynapseClientAsync synapseClient;
	TeamMemberCountView view;
	SynapseAlert synAlert;

	@Inject
	public TeamMemberCountWidget(TeamMemberCountView view, SynapseClientAsync synapseClient, SynapseAlert synAlert) {
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.view = view;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert.asWidget());
	}

	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		synAlert.clear();
		String teamId = widgetDescriptor.get(WidgetConstants.TEAM_ID_KEY);
		if (teamId == null) {
			synAlert.showError(WidgetConstants.TEAM_ID_KEY + " is required.");
		} else {
			configure(teamId);
		}
	}

	public void configure(String teamId) {
		synapseClient.getTeamMemberCount(teamId, new AsyncCallback<Long>() {
			@Override
			public void onSuccess(Long count) {
				view.setCount(count.toString());
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
