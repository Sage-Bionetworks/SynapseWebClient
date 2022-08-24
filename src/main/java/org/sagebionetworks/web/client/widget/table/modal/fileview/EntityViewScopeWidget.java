package org.sagebionetworks.web.client.widget.table.modal.fileview;

import static org.sagebionetworks.web.shared.WebConstants.PROJECT;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic for viewing and editing the EntityView scope.
 * <p>
 * <p>
 * <p>
 * Scope Widget - these are the UI output elements in this widget:
 * <p>
 * +-------------------------------------------+ |Scope | | | | (EntityContainerListWidget, not
 * editable)| | | | +----+ | | |Edit| (shown if widget set to editable) | | +----+ |
 * +------------------------------------+------+ | ^ | onEdit (show modal) | onSave (update view
 * scope) v | +--+---------------------------------+------+ |Edit Scope (modal) | | | | (Editable
 * EntityContainerListWidget) | | | | +------+ +----+ | | |Cancel| |Save| | | +------+ +----+ |
 * +-------------------------------------------+
 *
 * @author Jay
 */
public class EntityViewScopeWidget implements SynapseWidgetPresenter, EntityViewScopeWidgetView.Presenter {
	boolean isEditable;
	EntityViewScopeWidgetView view;
	SynapseJavascriptClient jsClient;
	EntityBundle bundle;
	EntityContainerListWidget viewScopeWidget, editScopeWidget;
	SynapseAlert synAlert;
	EntityView currentView;
	TableType tableType;
	EventBus eventBus;

	/**
	 * New presenter with its view.
	 *
	 * @param view
	 */
	@Inject
	public EntityViewScopeWidget(EntityViewScopeWidgetView view, SynapseJavascriptClient jsClient, EntityContainerListWidget viewScopeWidget, EntityContainerListWidget editScopeWidget, SynapseAlert synAlert, EventBus eventBus) {
		this.jsClient = jsClient;
		this.view = view;
		this.viewScopeWidget = viewScopeWidget;
		this.editScopeWidget = editScopeWidget;
		this.synAlert = synAlert;
		this.eventBus = eventBus;
		view.setPresenter(this);
		view.setEditableEntityListWidget(editScopeWidget.asWidget());
		view.setEntityListWidget(viewScopeWidget.asWidget());
		view.setSynAlert(synAlert.asWidget());
	}

	private List<Reference> getReferencesFromIdList(List<String> ids) {
		if (ids == null) {
			ids = new ArrayList<>();
		}
		List<Reference> references = new ArrayList<>(ids.size());
		for (String entityId : ids) {
			Reference reference = new Reference();
			reference.setTargetId(entityId);
			references.add(reference);
		}
		return references;
	}

	public void configure(EntityBundle bundle, boolean isEditable) {
		this.isEditable = isEditable;
		this.bundle = bundle;
		boolean isVisible = false;
		if (bundle.getEntity() instanceof EntityView) {
			isVisible = true;

			currentView = (EntityView) bundle.getEntity();
			tableType = TableType.getTableType(currentView);
			List<Reference> references = getReferencesFromIdList(currentView.getScopeIds());

			view.setEditMaskAndScopeButtonVisible(isEditable);
			viewScopeWidget.configure(references, false, tableType);
		}

		view.setVisible(isVisible);
	}

	private static boolean isEditMaskSupportedInWebClient(TableType tableType) {
		if (tableType.getViewTypeMask() == PROJECT) {
			// Masks of project Views are not editable
			return false;
		} else {
			// If the entity view isn't a project view, then it can only have any combination of File | Folder | Table | Dataset
			// If it contains any other type, it's not editable because the editor doesn't display all of the fields required for the user to update this mask.
			return !(tableType.isIncludeEntityView() || tableType.isIncludeSubmissionView() || tableType.isIncludeDockerRepo() || tableType.isIncludeProject());
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateViewTypeMask() {
		tableType = TableType.getEntityViewTableType(view.isFileSelected(), view.isFolderSelected(), view.isTableSelected(), view.isDatasetSelected());
	}

	@Override
	public void onSave() {
		// update scope
		synAlert.clear();
		view.setLoading(true);
		currentView.setScopeIds(editScopeWidget.getEntityIds());
		currentView.setViewTypeMask(tableType.getViewTypeMask().longValue());
		currentView.setType(null);
		jsClient.updateEntity(currentView, null, null, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity entity) {
				view.setLoading(false);
				view.hideModal();
				eventBus.fireEvent(new EntityUpdatedEvent(entity.getId()));
			}

			@Override
			public void onFailure(Throwable caught) {
				view.setLoading(false);
				synAlert.handleException(caught);
			}
		});
	}

	@Override
	public void onEditScopeAndMask() {
		List<Reference> references = getReferencesFromIdList(currentView.getScopeIds());
		// configure edit list, and show modal
		editScopeWidget.configure(references, true, tableType);

		// The mask may not be editable since not all masks are supported in the web client
		boolean isMaskEditable = isEditable && isEditMaskSupportedInWebClient(tableType);
		if (!isMaskEditable) {
			synAlert.consoleError("View type mask is not supported by web client, blocking edit. Mask value:" + ((EntityView) currentView).getViewTypeMask());
		}

		view.setEditMaskVisible(isMaskEditable);
		if (isMaskEditable) {
			// update the checkbox state based on the view type mask
			view.setIsFileSelected(tableType.isIncludeFiles());
			view.setIsFolderSelected(tableType.isIncludeFolders());
			view.setIsTableSelected(tableType.isIncludeTables());
			view.setIsDatasetSelected(tableType.isIncludeDatasets());
		}
		view.showModal();
	}
}
