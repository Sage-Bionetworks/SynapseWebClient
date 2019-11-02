package org.sagebionetworks.web.client.widget.refresh;

import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReplyCountAlert implements RefreshAlertView.Presenter, SynapseWidgetPresenter {

	private RefreshAlertView view;
	SynapseJavascriptClient jsClient;
	private GWTWrapper gwt;
	private GlobalApplicationState globalAppState;
	private SynapseJSNIUtils utils;
	private Long count;
	private String threadId;
	private Callback refreshCallback;
	private Callback invokeCheck;
	public static final int DELAY = 70000; // check every 70 seconds (until detached, configuration cleared, or a change has been detected)

	@Inject
	public ReplyCountAlert(RefreshAlertView view, GWTWrapper gwt, GlobalApplicationState globalAppState, SynapseJSNIUtils utils, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.gwt = gwt;
		this.globalAppState = globalAppState;
		this.utils = utils;
		this.jsClient = jsClient;
		view.setPresenter(this);
		invokeCheck = new Callback() {
			@Override
			public void invoke() {
				checkCount();
			}
		};
	}

	public void clear() {
		view.setVisible(false);
		count = null;
		threadId = null;
	}

	public void configure(String threadId) {
		clear();
		this.threadId = threadId;

		checkCount();
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
		checkCount();
	}

	private void checkCount() {
		if (view.isAttached() && threadId != null) {
			jsClient.getReplyCountForThread(threadId, DiscussionFilter.NO_FILTER, new AsyncCallback<Long>() {
				@Override
				public void onSuccess(Long result) {
					if (count == null) {
						count = result;
					}
					if (!count.equals(result)) {
						// count changed!
						view.setVisible(true);
					} else {
						// no change, reschedule
						if (view.isAttached()) {
							gwt.scheduleExecution(invokeCheck, DELAY);
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

	/**
	 * If you set this callback, it will be invoked when the user elects to refresh the data (instead of
	 * causing a page refresh)
	 * 
	 * @param c
	 */
	public void setRefreshCallback(Callback c) {
		refreshCallback = c;
	}

}
