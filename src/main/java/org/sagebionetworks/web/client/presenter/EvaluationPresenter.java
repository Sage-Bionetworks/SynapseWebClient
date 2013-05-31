package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Evaluation;
import org.sagebionetworks.web.client.view.EvaluationView;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class EvaluationPresenter extends AbstractActivity implements EvaluationView.Presenter {
		
	private Evaluation place;
	private EvaluationView view;
	private SynapseClientAsync synapseClient;
	
	@Inject
	public EvaluationPresenter(EvaluationView view, SynapseClientAsync synapseClient){
		this.view = view;
		this.synapseClient = synapseClient;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	public void setPlace(Evaluation place) {
		this.place = place;
		this.view.setPresenter(this);
		
		configure(place.toToken());
	}
	
	@Override
	public void configure(final String evaluationId) {
		synapseClient.hasAccess(evaluationId, ObjectType.EVALUATION.toString(), ACCESS_TYPE.UPDATE.toString(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(final Boolean canEdit) {
				view.showPage(new WikiPageKey(evaluationId, ObjectType.EVALUATION.toString(), null), canEdit);		
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.EVALUATION_USER_ACCESS_ERROR + caught.getMessage());
			}
		});
	}
		
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
}
