package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.view.DownloadCartPageView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class DownloadCartPresenter extends AbstractActivity implements Presenter<org.sagebionetworks.web.client.place.DownloadCartPlace> {

	private DownloadCartPageView view;

	@Inject
	public DownloadCartPresenter(DownloadCartPageView view) {
		this.view = view;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view.asWidget());
	}

	@Override
	public void setPlace(final org.sagebionetworks.web.client.place.DownloadCartPlace place) {
		view.render();
	}
}
