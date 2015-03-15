package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Anchor;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.ChallengeBundle;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeBadgeViewImpl implements ChallengeBadgeView {
	public interface Binder extends UiBinder<Widget, ChallengeBadgeViewImpl> {	}
	private Presenter presenter;
	
	@UiField
	Anchor link;
	Widget widget;
	
	@Inject
	public ChallengeBadgeViewImpl(
			final Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public void setHref(String href) {
		link.setHref(href);
	}
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
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
