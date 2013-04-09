package org.sagebionetworks.web.client.presenter.users;

import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.PresenterProxy;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class PasswordResetPresenterProxy extends AbstractActivity implements PresenterProxy<PasswordReset>{

	AsyncProvider<PasswordResetPresenter> provider;
	
	@Inject
	public PasswordResetPresenterProxy(AsyncProvider<PasswordResetPresenter> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		this.provider.get(new AsyncCallback<PasswordResetPresenter>() {
			
			@Override
			public void onSuccess(PasswordResetPresenter result) {
				result.start(panel, eventBus);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
				
			}
		});
		
	}

	@Override
	public void setPlace(final PasswordReset place) {
		this.provider.get(new AsyncCallback<PasswordResetPresenter>() {
			
			@Override
			public void onSuccess(PasswordResetPresenter result) {
				result.setPlace(place);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
			}
		});
	}

}
