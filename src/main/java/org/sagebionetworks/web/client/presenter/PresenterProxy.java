package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.utils.AlertUtils;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
 * A reusable PresenterProxyImp that provides a code split point for a presenter.
 * 
 * @author John
 *
 * @param <P> - Presenter
 * @param <T> - The Presenter's place
 */
public class PresenterProxy<P extends Presenter<T>,T extends Place> extends AbstractActivity {
	
	AsyncProvider<P> provider;
	T place;
	
	@Inject
	public PresenterProxy(AsyncProvider<P> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		provider.get(new AsyncCallback<P>() {

			@Override
			public void onFailure(Throwable caught) {
				// Not sure what to do here.
				AlertUtils.showErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(P result) {
				// forward the place and start
				result.setPlace(place);
				result.start(panel, eventBus);
			}
		});
	}

	public void setPlace(T place) {
		// This will get forwarded to the presenter when we get it in start()
		this.place = place;
	}

}
