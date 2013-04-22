package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Evaluation;
import org.sagebionetworks.web.client.view.EvaluationView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class EvaluationPresenter extends AbstractActivity implements EvaluationView.Presenter {
		
	private Evaluation place;
	private EvaluationView view;
	private SynapseClientAsync synapseClient;
	private String evaluationId;
	
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
		this.evaluationId = evaluationId;
		synapseClient.hasAccess(evaluationId, WidgetConstants.WIKI_OWNER_ID_EVALUATION, ACCESS_TYPE.UPDATE.toString(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(final Boolean canEdit) {
				try {
					synapseClient.getUserEvaluationState(evaluationId, new AsyncCallback<UserEvaluationState>() {
						@Override
						public void onSuccess(UserEvaluationState state) {
							view.showPage(new WikiPageKey(evaluationId, WidgetConstants.WIKI_OWNER_ID_EVALUATION, null), state, canEdit);		
						}
						@Override
						public void onFailure(Throwable caught) {
							view.showErrorMessage(caught.getMessage());
						}
					});
				} catch (RestServiceException e) {
					view.showErrorMessage(e.getMessage());
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	@Override
	public void register() {
		try {
			synapseClient.createParticipant(evaluationId, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					view.showInfo("Successfully Joined!", "");
					configure(evaluationId);
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(e.getMessage());
		}
	}

	@Override
	public void unregister() {
		try {
			synapseClient.deleteParticipant(evaluationId, new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					view.showInfo("Successful", "");
					configure(evaluationId);
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(e.getMessage());
		}
	}

	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
}
