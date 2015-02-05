package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.ChallengeSummary;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeBadgeViewImpl implements ChallengeBadgeView {
	
	private Presenter presenter;
	
	
	public interface Binder extends UiBinder<Widget, ChallengeBadgeViewImpl> {	}
	
	@UiField
	Anchor link;
	@UiField
	Anchor participantsLink;
	
	Widget widget;
	
	@Inject
	public ChallengeBadgeViewImpl(
			final Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		//init click handler
		link.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClick();
			}
		});
		participantsLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onParticipantsClick();
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}
	
	public void setChallenge(ChallengeSummary header) {
		link.setText(header.getName());
	};
	
	@Override
	public Widget asWidget() {
		return widget;
	}
}
