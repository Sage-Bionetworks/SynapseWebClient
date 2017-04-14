package org.sagebionetworks.web.client.widget.accessrequirements.submission;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmission;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenSubmissionWidget implements OpenSubmissionWidgetView.Presenter, IsWidget {

	private OpenSubmissionWidgetView view;
	private ACTAccessRequirementWidget accessRequirementWidget;
	private DataAccessClientAsync dataAccessClient;
	private SynapseAlert synAlert;
	private LazyLoadHelper lazyLoadHelper;
	private long accessRequirementId;

	@Inject
	public OpenSubmissionWidget(
			OpenSubmissionWidgetView view,
			ACTAccessRequirementWidget accessRequirementWidget,
			DataAccessClientAsync dataAccessClient,
			SynapseAlert synAlert,
			LazyLoadHelper lazyLoadHelper) {
		this.view = view;
		this.accessRequirementWidget = accessRequirementWidget;
		this.dataAccessClient = dataAccessClient;
		this.synAlert = synAlert;
		this.lazyLoadHelper = lazyLoadHelper;
		view.setSynAlert(synAlert);
		view.setACTAccessRequirementWidget(accessRequirementWidget);
		view.setPresenter(this);
		Callback loadDataCallback = new Callback() {
			@Override
			public void invoke() {
				loadAccessRequirement();
			}
		};
		
		lazyLoadHelper.configure(loadDataCallback, view);
	}

	public void loadAccessRequirement() {
		dataAccessClient.getAccessRequirement(accessRequirementId, new AsyncCallback<AccessRequirement>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(AccessRequirement ar) {
				if (ar instanceof ACTAccessRequirement) {
					accessRequirementWidget.setRequirement((ACTAccessRequirement)ar);
				} else {
					onFailure(new IllegalStateException("Expected an ACTAccessRequirement, but get "+ar.getConcreteType()));
				}
			}
			
		});
	}

	public void configure(OpenSubmission openSubmission) {
		view.setNumberOfSubmissions(openSubmission.getNumberOfSubmittedSubmission());
		accessRequirementId = Long.parseLong(openSubmission.getAccessRequirementId());
		lazyLoadHelper.setIsConfigured();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
