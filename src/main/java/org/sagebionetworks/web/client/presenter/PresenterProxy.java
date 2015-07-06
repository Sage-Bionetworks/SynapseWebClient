package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.Portal;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
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
	PortalGinInjector ginjector;
	private static boolean prefetchedBulk = false;
	GWTWrapper gwt;
	SynapseJSNIUtils jsniUtils;
	
	@Inject
	public PresenterProxy(AsyncProvider<P> provider,
			GWTWrapper gwt,
			SynapseJSNIUtils jsniUtils){
		this.provider = provider;
		this.gwt = gwt;
		this.jsniUtils = jsniUtils;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		provider.get(new AsyncCallback<P>() {

			@Override
			public void onFailure(Throwable caught) {
				//SWC-2444: if there is a problem getting the code, try to reload the app
				jsniUtils.consoleError(caught.getMessage());
				gwt.scheduleExecution(new Callback() {
					@Override
					public void invoke() {
						Window.Location.reload();		
					}
				}, Portal.CODE_LOAD_DELAY);
			}

			@Override
			public void onSuccess(P result) {
				// forward the place and start
				result.setPlace(place);
				result.start(panel, eventBus);
				
				// Prefetch the BulkPresenter code split
				if(!prefetchedBulk) {
					if(ginjector != null) {
						BulkPresenterProxy bulkPresenterProxy = ginjector.getBulkPresenterProxy();
						bulkPresenterProxy.start(null, null);
						prefetchedBulk = true;
					}
				}
			}
		});
	}

	public void setPlace(T place) {
		// This will get forwarded to the presenter when we get it in start()
		this.place = place;
	}

	public void setGinInjector(PortalGinInjector ginjector) {
		this.ginjector = ginjector;
	}

}
