package org.sagebionetworks.web.client.widget.refresh;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityRefreshAlert implements RefreshAlertView.Presenter, SynapseWidgetPresenter {

	private RefreshAlertView view;
	private SynapseJavascriptClient jsClient;
	private GWTWrapper gwt;
	private GlobalApplicationState globalAppState;
	private SynapseJSNIUtils utils;
	private String etag;
	private String entityId;
	Callback invokeCheckEtag;
	private Callback refreshCallback;
	public static final int DELAY = 60000; // check every minute (until detached, configuration cleared, or a change has been detected)

	@Inject
	public EntityRefreshAlert(RefreshAlertView view, SynapseJavascriptClient jsClient, GWTWrapper gwt, GlobalApplicationState globalAppState, SynapseJSNIUtils utils) {
		this.view = view;
		this.jsClient = jsClient;
		this.gwt = gwt;
		this.globalAppState = globalAppState;
		this.utils = utils;
		view.setPresenter(this);
		invokeCheckEtag = this::checkEtag;
	}

	public void clear() {
		view.setVisible(false);
		etag = null;
		entityId = null;
	}

	public void configure(String entityId) {
		clear();
		this.entityId = entityId;
		checkEtag();
	}

	/**
	 * If you set this callback, it will be invoked when the user elects to refresh the data (instead of
	 * causing a page refresh)
	 * 
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
		clear();
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

	void checkEtag() {
		if (view.isAttached() && entityId != null) {
			jsClient.getEntity(entityId, new AsyncCallback<Entity>() {
				@Override
				public void onSuccess(Entity result) {
					if (etag == null) {
						etag = result.getEtag();
					}
					if (!etag.equals(result.getEtag())) {
						// etag changed!
						view.setVisible(true);
					} else {
						// no etag change, reschedule
						if (view.isAttached()) {
							gwt.scheduleExecution(invokeCheckEtag, DELAY);
						}
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
