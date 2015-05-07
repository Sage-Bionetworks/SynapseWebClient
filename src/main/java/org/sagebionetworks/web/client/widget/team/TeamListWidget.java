package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.TeamRequestBundle;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamListWidget implements TeamListWidgetView.Presenter{

	private TeamListWidgetView view;
	private GlobalApplicationState globalApplicationState;

	@Inject
	public TeamListWidget(TeamListWidgetView view, 
			GlobalApplicationState globalApplicationState) {
		this.view = view;
		view.setPresenter(this);
		this.globalApplicationState = globalApplicationState;
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	public void configure(List<Team> teams, boolean isBig) {
		view.configure(teams, isBig);
	}	
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
	public void clear() {
		view.clear();
	}
	
	public void showLoading() {
		view.showLoading();
	}

}
