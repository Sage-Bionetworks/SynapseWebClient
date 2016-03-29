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
	public static final int DELAY = 10000; // recheck every 10 seconds
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
		etag = null;
		objectId = null;
		view.setPresenter(this);
		invokeCheckEtag = new Callback() {
			@Override
			public void invoke() {
				checkEtag();
			}
		};
	}
	
	public void configure(String objectId, ObjectType objectType) {
		this.objectId = objectId;
		this.objectType = objectType;
		checkEtag();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void onRefresh() {
		globalAppState.refreshPage();
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
