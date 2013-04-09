package org.sagebionetworks.web.client.presenter.users;

import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.PresenterProxy;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
 * Code split point for RegisterAccountPresenter.
 * @author John
 *
 */
public class RegisterAccountPresenterProxy extends AbstractActivity implements PresenterProxy<RegisterAccount>{

	AsyncProvider<RegisterAccountPresenter> provider;
	
	@Inject
	public RegisterAccountPresenterProxy(AsyncProvider<RegisterAccountPresenter> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		this.provider.get(new AsyncCallback<RegisterAccountPresenter>() {
			
			@Override
			public void onSuccess(RegisterAccountPresenter result) {
				result.start(panel, eventBus);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
				
			}
		});
		
	}

	@Override
	public void setPlace(final RegisterAccount place) {
		this.provider.get(new AsyncCallback<RegisterAccountPresenter>() {
			
			@Override
			public void onSuccess(RegisterAccountPresenter result) {
				result.setPlace(place);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
			}
		});
	}

}
