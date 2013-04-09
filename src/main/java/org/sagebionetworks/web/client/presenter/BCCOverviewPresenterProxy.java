package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.BCCOverview;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class BCCOverviewPresenterProxy  extends AbstractActivity implements PresenterProxy<BCCOverview>{

	AsyncProvider<BCCOverviewPresenter> provider;
	
	@Inject
	public BCCOverviewPresenterProxy(AsyncProvider<BCCOverviewPresenter> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		this.provider.get(new AsyncCallback<BCCOverviewPresenter>() {
			
			@Override
			public void onSuccess(BCCOverviewPresenter result) {
				result.start(panel, eventBus);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
				
			}
		});
		
	}

	@Override
	public void setPlace(final BCCOverview place) {
		this.provider.get(new AsyncCallback<BCCOverviewPresenter>() {
			
			@Override
			public void onSuccess(BCCOverviewPresenter result) {
				result.setPlace(place);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
			}
		});
	}

}