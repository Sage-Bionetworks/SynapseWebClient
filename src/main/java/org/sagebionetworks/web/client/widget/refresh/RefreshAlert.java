package org.sagebionetworks.web.client.widget.refresh;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.subscription.Etag;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RefreshAlert implements RefreshAlertView.Presenter, SynapseWidgetPresenter {
	
	private RefreshAlertView view;
	private SynapseClientAsync synapseClient;
	private GWTWrapper gwt;
	private GlobalApplicationState globalAppState;
	private SynapseJSNIUtils utils;
	private Etag etag;
	private String objectId;
	private ObjectType objectType;
	private Callback invokeCheckEtag;
	private Callback refreshCallback;
	public static final int DELAY = 15000; // check every 15 seconds (until detached, configuration cleared, or a change has been detected)
	@Inject
	public RefreshAlert(RefreshAlertView view, 
			SynapseClientAsync synapseClient, 
			GWTWrapper gwt,
			GlobalApplicationState globalAppState,
			SynapseJSNIUtils utils) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.gwt = gwt;
		this.globalAppState = globalAppState;
		this.utils = utils;
		view.setPresenter(this);
		invokeCheckEtag = new Callback() {
			@Override
			public void invoke() {
				checkEtag();
			}
		};
	}
	
	public void clear() {
		view.setVisible(false);
		etag = null;
		objectId = null;
		objectType = null;
	}
	
	public void configure(String objectId, ObjectType objectType) {
		clear();
		this.objectId = objectId;
		this.objectType = objectType;
		checkEtag();
	}
	
	/**
	 * If you set this callback, it will be invoked when the user elects to refresh the data (instead of causing a page refresh)
	 * @param c
	 */
	public void setRefreshCallback(Callback c) {
		refreshCallback = c;
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void onRefresh() {
		if (refreshCallback == null) {
			globalAppState.refreshPage();	
		} else {
			refreshCallback.invoke();
		}
	}
	
	@Override
	public void onAttach() {
		checkEtag();
	}
	
	private void checkEtag() {
		if (view.isAttached() && objectId != null && objectType != null) {
			synapseClient.getEtag(objectId, objectType, new AsyncCallback<Etag>() {
				@Override
				public void onSuccess(Etag result) {
					if (etag == null) {
						etag = result;
					}
					if (!etag.equals(result)) {
						//etag changed!  
						view.setVisible(true);
					} else {
						//no etag change, reschedule
						gwt.scheduleExecution(invokeCheckEtag, DELAY);		
					}
				}
				@Override
				public void onFailure(Throwable caught) {
					utils.consoleError(caught.getMessage());
				}
			});
		}
	}
}
