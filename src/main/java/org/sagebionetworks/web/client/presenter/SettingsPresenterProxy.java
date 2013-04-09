package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.Settings;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
 * Code split point for SettingsPresenter
 * @author John
 *
 */
public class SettingsPresenterProxy extends AbstractActivity implements PresenterProxy<Settings>{

	AsyncProvider<SettingsPresenter> provider;
	
	@Inject
	public SettingsPresenterProxy(AsyncProvider<SettingsPresenter> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		this.provider.get(new AsyncCallback<SettingsPresenter>() {
			
			@Override
			public void onSuccess(SettingsPresenter result) {
				result.start(panel, eventBus);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
				
			}
		});
		
	}

	@Override
	public void setPlace(final Settings place) {
		this.provider.get(new AsyncCallback<SettingsPresenter>() {
			
			@Override
			public void onSuccess(SettingsPresenter result) {
				result.setPlace(place);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
			}
		});
	}

}
