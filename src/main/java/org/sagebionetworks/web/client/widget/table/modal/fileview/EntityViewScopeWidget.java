package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.DatasetItem;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.View;
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
 *
 *
 *
 * Scope Widget - these are the UI output elements in this widget:
 *
 * +-------------------------------------------+ |Scope | | | | (EntityContainerListWidget, not
 * editable)| | | | +----+ | | |Edit| (shown if widget set to editable) | | +----+ |
 * +------------------------------------+------+ | ^ | onEdit (show modal) | onSave (update view
 * scope) v | +--+---------------------------------+------+ |Edit Scope (modal) | | | | (Editable
 * EntityContainerListWidget) | | | | +------+ +----+ | | |Cancel| |Save| | | +------+ +----+ |
 * +-------------------------------------------+
 *
 *
 * @author Jay
 *
 */
public class EntityViewScopeWidget implements SynapseWidgetPresenter, EntityViewScopeWidgetView.Presenter {
	boolean isEditable;
	EntityViewScopeWidgetView view;
	SynapseJavascriptClient jsClient;
	EntityBundle bundle;
	EntityContainerListWidget viewScopeWidget, editScopeWidget;
	SynapseAlert synAlert;
	View currentView;
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

	private List<Reference> getReferencesFromDatasetItems(List<DatasetItem> datasetItems) {
		if (datasetItems == null) {
			datasetItems = new ArrayList<>();
		}
		List<Reference> references = new ArrayList<>(datasetItems.size());
		for (DatasetItem datasetItem : datasetItems) {
			Reference reference = new Reference();
			reference.setTargetId(datasetItem.getEntityId());
			reference.setTargetVersionNumber(datasetItem.getVersionNumber());
			references.add(reference);
		}
		return references;
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
		if (bundle.getEntity() instanceof EntityView || bundle.getEntity() instanceof Dataset) {
			isVisible = true;
			List<Reference> references = new ArrayList<>();
			if (bundle.getEntity() instanceof EntityView) {
				currentView = (EntityView) bundle.getEntity();
				references = getReferencesFromIdList(((EntityView) currentView).getScopeIds());
				if (tableType == null) {
					synAlert.consoleError("View type mask is not supported by web client, blocking edit." + ((EntityView) currentView).getViewTypeMask());
				}
			} else if (bundle.getEntity() instanceof Dataset) {
				currentView = (Dataset) bundle.getEntity();
				references = getReferencesFromDatasetItems(((Dataset) currentView).getItems());
			}
			tableType = TableType.getTableType(currentView);
			viewScopeWidget.configure(references, false, tableType);
			view.setEditButtonVisible(isEditable && tableType != null);

		}

		view.setVisible(isVisible);
	}


	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateViewTypeMask() {
		tableType = TableType.getTableType(view.isFileSelected(), view.isFolderSelected(), view.isTableSelected());
	}

	@Override
	public void onSave() {
		// update scope
		synAlert.clear();
		view.setLoading(true);
		if (currentView instanceof EntityView) {
			((EntityView) currentView).setScopeIds(editScopeWidget.getEntityIds());
			((EntityView) currentView).setViewTypeMask(tableType.getViewTypeMask().longValue());
			((EntityView) currentView).setType(null);
		} else if (currentView instanceof Dataset) {
			List<Reference> referenceList = editScopeWidget.getReferences();
			List<DatasetItem> datasetItems = new ArrayList<>(referenceList.size());
			for (Reference reference : referenceList) {
				DatasetItem datasetItem = new DatasetItem();
				datasetItem.setEntityId(reference.getTargetId());
				datasetItem.setVersionNumber(reference.getTargetVersionNumber());
				datasetItems.add(datasetItem);
			}
			((Dataset) currentView).setItems(datasetItems);
		}
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
		List<Reference> references = new ArrayList<>();
		// configure edit list, and show modal
		if (currentView instanceof EntityView) {
            references = getReferencesFromIdList(((EntityView) currentView).getScopeIds());
		} else if (currentView instanceof Dataset) {
			references = getReferencesFromDatasetItems(((Dataset) currentView).getItems());
		}
		editScopeWidget.configure(references, true, tableType);

		if (TableType.table.equals(tableType) || TableType.projects.equals(tableType) || TableType.dataset.equals(tableType)) {
			view.setViewTypeOptionsVisible(false);
		} else {
			view.setViewTypeOptionsVisible(true);
			// update the checkbox state based on the view type mask
			view.setIsFileSelected(tableType.isIncludeFiles());
			view.setIsFolderSelected(tableType.isIncludeFolders());
			view.setIsTableSelected(tableType.isIncludeTables());
		}

		view.showModal();
	}
}
