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

public class DiscussionThreadCountAlert implements RefreshAlertView.Presenter, SynapseWidgetPresenter {

	private RefreshAlertView view;
	SynapseJavascriptClient jsClient;
	private GWTWrapper gwt;
	private GlobalApplicationState globalAppState;
	private SynapseJSNIUtils utils;
	private Long count;
	private String forumId;

	private Callback invokeCheck;
	public static final int DELAY = 80000; // check every 80 seconds (until detached, configuration cleared, or a change has been detected)

	@Inject
	public DiscussionThreadCountAlert(RefreshAlertView view, GWTWrapper gwt, GlobalApplicationState globalAppState, SynapseJSNIUtils utils, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.jsClient = jsClient;
		this.gwt = gwt;
		this.globalAppState = globalAppState;
		this.utils = utils;
		view.setPresenter(this);
		invokeCheck = new Callback() {
			@Override
			public void invoke() {
				checkThreadCount();
			}
		};
	}

	public void clear() {
		view.setVisible(false);
		count = null;
		forumId = null;
	}

	/**
	 * 
	 * @param forumId
	 * @param refreshCallback Widget will call back with the number of new threads detected
	 */
	public void configure(String forumId) {
		clear();
		this.forumId = forumId;

		checkThreadCount();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onRefresh() {
		clear();
		globalAppState.refreshPage();
	}

	@Override
	public void onAttach() {
		checkThreadCount();
	}

	private void checkThreadCount() {
		if (view.isAttached() && forumId != null) {
			jsClient.getThreadCountForForum(forumId, DiscussionFilter.NO_FILTER, new AsyncCallback<Long>() {
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
}
