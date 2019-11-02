package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.view.ErrorView;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ErrorPresenter extends AbstractActivity implements Presenter<org.sagebionetworks.web.client.place.ErrorPlace> {

	private ErrorView view;
	private GWTWrapper gwt;

	@Inject
	public ErrorPresenter(ErrorView view, GWTWrapper gwt) {
		this.view = view;
		this.gwt = gwt;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view.asWidget());
	}

	@Override
	public void setPlace(final org.sagebionetworks.web.client.place.ErrorPlace place) {
		String token = place.toToken();
		view.refreshHeader();
		// decode error
		view.setErrorMessage(gwt.decodeQueryString(token));
	}
}
