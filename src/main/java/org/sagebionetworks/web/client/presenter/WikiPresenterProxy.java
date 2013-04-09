package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.Governance;
import org.sagebionetworks.web.client.place.WikiPlace;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
 * Code split point for WikiPresenter
 * 
 * @author John
 *
 */
public class WikiPresenterProxy  extends AbstractActivity implements PresenterProxy<WikiPlace>{

	AsyncProvider<WikiPresenter> provider;
	
	@Inject
	public WikiPresenterProxy(AsyncProvider<WikiPresenter> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		this.provider.get(new AsyncCallback<WikiPresenter>() {
			
			@Override
			public void onSuccess(WikiPresenter result) {
				result.start(panel, eventBus);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
				
			}
		});
		
	}

	@Override
	public void setPlace(final WikiPlace place) {
		this.provider.get(new AsyncCallback<WikiPresenter>() {
			
			@Override
			public void onSuccess(WikiPresenter result) {
				result.setPlace(place);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
			}
		});
	}

}
