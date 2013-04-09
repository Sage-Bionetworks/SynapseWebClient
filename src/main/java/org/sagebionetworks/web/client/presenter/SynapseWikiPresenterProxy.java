package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.Wiki;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
 * Code split point for SynapseWikiPresenter
 * @author John
 *
 */
public class SynapseWikiPresenterProxy  extends AbstractActivity implements PresenterProxy<Wiki>{

	AsyncProvider<SynapseWikiPresenter> provider;
	
	@Inject
	public SynapseWikiPresenterProxy(AsyncProvider<SynapseWikiPresenter> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		this.provider.get(new AsyncCallback<SynapseWikiPresenter>() {
			
			@Override
			public void onSuccess(SynapseWikiPresenter result) {
				result.start(panel, eventBus);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
				
			}
		});
		
	}

	@Override
	public void setPlace(final Wiki place) {
		this.provider.get(new AsyncCallback<SynapseWikiPresenter>() {
			
			@Override
			public void onSuccess(SynapseWikiPresenter result) {
				result.setPlace(place);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
			}
		});
	}

}
