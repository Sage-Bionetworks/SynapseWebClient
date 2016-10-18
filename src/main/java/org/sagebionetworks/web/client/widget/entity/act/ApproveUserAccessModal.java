package org.sagebionetworks.web.client.widget.entity.act;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.EvaluationStatus;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ApproveUserAccessModal implements ApproveUserAccessModalView.Presenter, IsWidget {
	
	private ApproveUserAccessModalView view;
	private boolean isCreate;
	@Inject
	public ApproveUserAccessModal(ApproveUserAccessModalView view) {
		this.view = view;

		view.setPresenter(this);
	}
	
	public void configure(boolean isCreate) {
		this.isCreate = isCreate;
	}
	
	public void show() {
		view.show();
	}
	
	@Override
	public void onSave() {
	}
	
	public Widget asWidget() {
		view.setPresenter(this);			
		return view.asWidget();
	}

	public void configure(List<AccessRequirement> accessRequirements) {
		List<String> list = new ArrayList<String>();
		for (AccessRequirement ar : accessRequirements) {
			list.add(Long.toString(ar.getId()));
		}
		view.setStates(list);
	}
		
}
