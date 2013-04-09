package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.Profile;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
 * Code split point for ProfilePresenter.
 * 
 * @author John
 *
 */
public class ProfilePresenterProxy extends AbstractActivity implements PresenterProxy<Profile>{

	AsyncProvider<ProfilePresenter> provider;
	
	@Inject
	public ProfilePresenterProxy(AsyncProvider<ProfilePresenter> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		this.provider.get(new AsyncCallback<ProfilePresenter>() {
			
			@Override
			public void onSuccess(ProfilePresenter result) {
				result.start(panel, eventBus);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
				
			}
		});
		
	}

	@Override
	public void setPlace(final Profile place) {
		this.provider.get(new AsyncCallback<ProfilePresenter>() {
			
			@Override
			public void onSuccess(ProfilePresenter result) {
				result.setPlace(place);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
			}
		});
	}

}
