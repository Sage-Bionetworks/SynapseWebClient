package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationList;
import org.sagebionetworks.web.client.widget.evaluation.SubmissionViewScopeEditor;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic for viewing and editing the SubmissionView scope.
 * 
 * Scope Widget - these are the UI output elements in this widget:
 * 
 * @author Jay
 *
 */
public class SubmissionViewScopeWidget implements SynapseWidgetPresenter, SubmissionViewScopeWidgetView.Presenter {
	boolean isEditable;
	SubmissionViewScopeWidgetView view;
	SynapseJavascriptClient jsClient;
	EntityBundle bundle;
	EvaluationList viewScopeWidget;
	SubmissionViewScopeEditor editScopeWidget;
	SynapseAlert synAlert;
	SubmissionView currentView;
	EventBus eventBus;
	List<Evaluation> evaluationsList = new ArrayList<Evaluation>();
	int jobKey = 0;
	/**
	 * New presenter with its view.
	 * 
	 * @param view
	 */
	@Inject
	public SubmissionViewScopeWidget(SubmissionViewScopeWidgetView view, SynapseJavascriptClient jsClient, EvaluationList viewScopeWidget, SubmissionViewScopeEditor editScopeWidget, SynapseAlert synAlert, EventBus eventBus) {
		this.jsClient = jsClient;
		this.view = view;
		this.viewScopeWidget = viewScopeWidget;
		this.editScopeWidget = editScopeWidget;
		this.synAlert = synAlert;
		this.eventBus = eventBus;
		view.setPresenter(this);
		view.setSubmissionViewScopeEditor(editScopeWidget);
		view.setEvaluationListWidget(viewScopeWidget.asWidget());
		view.setSynAlert(synAlert.asWidget());
	}

	public void configure(EntityBundle bundle, boolean isEditable) {
		this.isEditable = isEditable;
		this.bundle = bundle;
		boolean isVisible = bundle.getEntity() instanceof SubmissionView;
		jobKey++;
		if (isVisible) {
			int currentJobKey = jobKey;
			currentView = (SubmissionView) bundle.getEntity();
			evaluationsList.clear();
			synAlert.clear();
			// get the evaluation queues
			// TODO: currently getting these individually, but would like to get them in bulk (PLFM-6288).
			AsyncCallback<Evaluation> cb = new AsyncCallback<Evaluation>() {
				@Override
				public void onFailure(Throwable caught) {
					if (currentJobKey != jobKey) {
						return;
					}
					synAlert.handleException(caught);
				}
				@Override
				public void onSuccess(Evaluation evaluation) {
					if (currentJobKey != jobKey) {
						return;
					}
					evaluationsList.add(evaluation);
					if (evaluationsList.size() == currentView.getScopeIds().size()) {
						// got them all!
						boolean isSelectable = false;
						viewScopeWidget.configure(evaluationsList, isSelectable);
					}
				}
			};
			for (String evaluationId : currentView.getScopeIds()) {
				jsClient.getEvaluation(evaluationId, cb);
			}
			
			view.setEditButtonVisible(isEditable);
		}
		view.setVisible(isVisible);
	}


	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onSave() {
		// update scope
		synAlert.clear();
		List<String> newScopeIds = editScopeWidget.getEvaluationIds();
		if (newScopeIds.isEmpty()) {
			synAlert.showError(CreateTableViewWizardStep1.EMPTY_SCOPE_MESSAGE);
			return;
		}
		view.setLoading(true);
		currentView.setScopeIds(newScopeIds);
		jsClient.updateEntity(currentView, null, null, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity entity) {
				view.setLoading(false);
				view.hideModal();
				eventBus.fireEvent(new EntityUpdatedEvent());
			}

			@Override
			public void onFailure(Throwable caught) {
				view.setLoading(false);
				synAlert.handleException(caught);
			}
		});
	}

	@Override
	public void onEdit() {
		editScopeWidget.configure(evaluationsList);
		view.showModal();
	}
}
