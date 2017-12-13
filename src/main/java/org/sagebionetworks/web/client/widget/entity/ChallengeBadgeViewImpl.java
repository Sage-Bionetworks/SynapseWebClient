package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Anchor;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.shared.ChallengeBundle;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeBadgeViewImpl implements ChallengeBadgeView {
	public interface Binder extends UiBinder<Widget, ChallengeBadgeViewImpl> {	}
	
	@UiField
	Anchor link;
	Widget widget;
	String projectId;
	
	@Inject
	public ChallengeBadgeViewImpl(
			final Binder uiBinder,
			GlobalApplicationState globalAppState) {
		widget = uiBinder.createAndBindUi(this);
		link.addClickHandler(event -> {
			event.preventDefault();
			globalAppState.getPlaceChanger().goTo(new Synapse(projectId));
		});
	}

	@Override
	public void setProjectId(String projectId) {
		this.projectId = projectId;
		link.setHref(DisplayUtils.getSynapseHistoryToken(projectId));
	}
	
	public void setChallenge(ChallengeBundle challenge) {
		link.setText(challenge.getProjectName());
	};
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
