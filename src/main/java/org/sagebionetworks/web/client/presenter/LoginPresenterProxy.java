package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.LoginPlace;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
 * The code split point for the LoginPresenter.
 * 
 * @author John
 *
 */
public class LoginPresenterProxy extends AbstractActivity implements PresenterProxy<LoginPlace> {
	
	AsyncProvider<LoginPresenter> provider;
	
	@Inject
	public LoginPresenterProxy(AsyncProvider<LoginPresenter> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		provider.get(new AsyncCallback<LoginPresenter>() {

			@Override
			public void onSuccess(LoginPresenter result) {
				result.start(panel, eventBus);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// Not sure what do here.
			}
		});
		
	}

	@Override
	public void setPlace(final LoginPlace place) {
		provider.get(new AsyncCallback<LoginPresenter>() {
			
			@Override
			public void onSuccess(LoginPresenter result) {
				result.setPlace(place);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// Not sure what do here.
			}
		});
		
	}

}
