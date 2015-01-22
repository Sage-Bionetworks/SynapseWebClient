package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterTeamDialog implements RegisterTeamDialogView.Presenter {
	private RegisterTeamDialogView view;
	String teamId;
	
	@Inject
	public RegisterTeamDialog(RegisterTeamDialogView view) {
		this.view = view;
		view.showTeamSelector(true);
		view.setRecruitmentMessage("");
		view.setPresenter(this);
	}		
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void setPreSelectedTeam(String recruitmentMessage, String teamId) {
		this.teamId = teamId;
		view.setRecruitmentMessage(recruitmentMessage);
		view.showTeamSelector(false);
	}
	
	@Override
	public void okClicked() {
		//TODO: create or update team registration
		fdsfds
	}
}
