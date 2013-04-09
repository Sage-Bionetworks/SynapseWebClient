package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.Synapse;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
 * Code split point for EntityPresenter.
 * @author jmhill
 *
 */
public class EntityPresenterProxy extends AbstractActivity implements PresenterProxy<Synapse>{

	AsyncProvider<EntityPresenter> provider;
	
	@Inject
	public EntityPresenterProxy(AsyncProvider<EntityPresenter> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		this.provider.get(new AsyncCallback<EntityPresenter>() {
			
			@Override
			public void onSuccess(EntityPresenter result) {
				result.start(panel, eventBus);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
				
			}
		});
		
	}

	@Override
	public void setPlace(final Synapse place) {
		this.provider.get(new AsyncCallback<EntityPresenter>() {
			
			@Override
			public void onSuccess(EntityPresenter result) {
				result.setPlace(place);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
			}
		});
	}

}
