package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.view.DownView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class DownPresenter extends AbstractActivity implements DownView.Presenter, Presenter<Down> {

	private DownView view;
	private EventBus bus;
	
	@Inject
	public DownPresenter(DownView view) {
		this.view = view;
		view.setPresenter(this);
	} 

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(this.view.asWidget());
		this.bus = eventBus;
	}
	
	@Override
	public void setPlace(final Down place) {
		view.setPresenter(this);
	}
	
}
