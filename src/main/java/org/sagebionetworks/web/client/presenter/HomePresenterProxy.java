package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.Home;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
 * By using a proxy for 
 * @author jmhill
 *
 */
public class HomePresenterProxy extends AbstractActivity implements PresenterProxy<Home> {
	
	AsyncProvider<HomePresenter> provider;

	@Inject
	public HomePresenterProxy(AsyncProvider<HomePresenter> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		provider.get(new AsyncCallback<HomePresenter>() {
			@Override
			public void onSuccess(HomePresenter presenter) {
				// Pass it along to the real presenter
				presenter.start(panel, eventBus);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// Not sure what to do here.
			}
		});
	}

	@Override
	public void setPlace(final Home place) {

		provider.get(new AsyncCallback<HomePresenter>(){
			@Override
			public void onFailure(Throwable caught) {
				// Not sure what to do here
			}

			@Override
			public void onSuccess(HomePresenter result) {
				// Pass it along
				result.setPlace(place);
			}});
	}



}
