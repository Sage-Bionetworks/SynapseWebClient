package org.sagebionetworks.web.client.presenter;

import java.util.Collections;
import java.util.List;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DownView;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class DownPresenter extends AbstractActivity implements Presenter<Down> {
	private DownView view;
	GWTWrapper gwt;
	GlobalApplicationState globalAppState;
	Callback checkForRepoDownCallback;
	SynapseJavascriptClient jsClient;
	@Inject
	public DownPresenter(
			DownView view,
			GWTWrapper gwt,
			GlobalApplicationState globalAppState,
			final SynapseJavascriptClient jsClient) {
		this.view = view;
		this.gwt = gwt;
		this.globalAppState = globalAppState;
		this.jsClient = jsClient;
		checkForRepoDownCallback = new Callback() {
			@Override
			public void invoke() {
				checkForRepoDown();
			}
		};
	}
	
	public void checkForRepoDown() {
		jsClient.listUserProfiles(Collections.singletonList("-1"), new AsyncCallback<List<UserProfile>>() {
			@Override
			public void onSuccess(List<UserProfile> result) {
				globalAppState.gotoLastPlace();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof ReadOnlyModeException || caught instanceof SynapseDownException) {
					// still down
					scheduleRepoDownCheck();
				} else {
					repoIsUp();
				}
			}
			
			private void repoIsUp() {
				if (globalAppState.getLastPlace() != null) {
					globalAppState.gotoLastPlace();
				} else {
					globalAppState.getPlaceChanger().goTo(new Home(ClientProperties.DEFAULT_PLACE_TOKEN));
				}
			}
		});
	}
	
	public void scheduleRepoDownCheck() {
		gwt.scheduleExecution(checkForRepoDownCallback, 15000);
	}
	
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(this.view.asWidget());
	}
	
	@Override
	public void setPlace(Down place) {
		view.init();
		String message = gwt.decodeQueryString(place.toToken());
		view.setMessage(message);
		scheduleRepoDownCheck();
	}
	
}
