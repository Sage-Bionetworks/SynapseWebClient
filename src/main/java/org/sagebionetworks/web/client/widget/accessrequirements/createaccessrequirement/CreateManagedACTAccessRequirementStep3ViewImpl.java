package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateManagedACTAccessRequirementStep3ViewImpl implements CreateManagedACTAccessRequirementStep3View {

	@UiField
	Div actTeamContainer;
	@UiField
	Div otherUserTeamContainer;
	@UiField
	Div userTeamSearchContainer;

	public interface Binder extends UiBinder<Widget, CreateManagedACTAccessRequirementStep3ViewImpl> {
	}

	Widget widget;
	
	Presenter presenter;

	@Inject
	public CreateManagedACTAccessRequirementStep3ViewImpl(Binder binder, TeamBadge actTeamBadge, SynapseProperties synapseProperties) {
		widget = binder.createAndBindUi(this);
		
		actTeamBadge.configure(synapseProperties.getSynapseProperty("org.sagebionetworks.portal.act.team_id"));
		actTeamContainer.add(actTeamBadge);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	@Override
	public void setReviewerBadge(IsWidget w) {
		otherUserTeamContainer.clear();
		otherUserTeamContainer.add(w);
	}
	@Override
	public void setReviewerUIVisible(boolean visible) {
		otherUserTeamContainer.setVisible(visible);
	}

	@Override
	public void setReviewerSearchBox(IsWidget w) {
		userTeamSearchContainer.clear();
		userTeamSearchContainer.add(w);
	}
	
	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}
}
