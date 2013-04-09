package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.Governance;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
 * Code split point for GovernancePresenter
 * @author John
 *
 */
public class GovernancePresenterProxy  extends AbstractActivity implements PresenterProxy<Governance>{

	AsyncProvider<GovernancePresenter> provider;
	
	@Inject
	public GovernancePresenterProxy(AsyncProvider<GovernancePresenter> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		this.provider.get(new AsyncCallback<GovernancePresenter>() {
			
			@Override
			public void onSuccess(GovernancePresenter result) {
				result.start(panel, eventBus);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
				
			}
		});
		
	}

	@Override
	public void setPlace(final Governance place) {
		this.provider.get(new AsyncCallback<GovernancePresenter>() {
			
			@Override
			public void onSuccess(GovernancePresenter result) {
				result.setPlace(place);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// not sure what to do here.
			}
		});
	}

}
