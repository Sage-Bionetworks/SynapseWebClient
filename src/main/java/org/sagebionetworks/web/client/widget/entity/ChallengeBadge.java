package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeBadge implements SynapseWidgetPresenter {

	private ChallengeBadgeView view;
	private EntityHeaderAsyncHandler entityHeaderAsyncHandler;
	private SynapseJSNIUtils jsniUtils;

	@Inject
	public ChallengeBadge(ChallengeBadgeView view, EntityHeaderAsyncHandler entityHeaderAsyncHandler, SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.entityHeaderAsyncHandler = entityHeaderAsyncHandler;
		this.jsniUtils = jsniUtils;
	}

	public void configure(Challenge challenge) {
		if (challenge != null) {
			view.setProjectId(challenge.getProjectId());
			entityHeaderAsyncHandler.getEntityHeader(challenge.getProjectId(), new AsyncCallback<EntityHeader>() {

				@Override
				public void onSuccess(EntityHeader header) {
					view.setProjectName(header.getName());
				}

				@Override
				public void onFailure(Throwable ex) {
					jsniUtils.consoleError("Unable to load challenge project: " + ex.getMessage());
				}
			});
		}
	}

	public void clearState() {}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
