package org.sagebionetworks.web.client.widget.table.modal.fileview;

import static org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget.getTableType;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic for viewing and editing the EntityView scope.
 * 
 *  

Scope Widget - these are the UI output elements in this widget:

+-------------------------------------------+
|Scope                                      |
|                                           |
|  (EntityContainerListWidget, not editable)|
|                                           |
| +----+                                    |
| |Edit| (shown if widget set to editable)  |
| +----+                                    |
+------------------------------------+------+
   |                                 ^
   | onEdit (show modal)             | onSave (update view scope)
   v                                 |
+--+---------------------------------+------+
|Edit Scope (modal)                         |
|                                           |
|  (Editable EntityContainerListWidget)     |
|                                           |
|                        +------+ +----+    |
|                        |Cancel| |Save|    |
|                        +------+ +----+    |
+-------------------------------------------+

 * 
 * @author Jay
 *
 */
public class ScopeWidget implements SynapseWidgetPresenter, ScopeWidgetView.Presenter {
	boolean isEditable;
	ScopeWidgetView view;
	SynapseClientAsync synapseClient;
	EntityBundle bundle;
	EntityUpdatedHandler updateHandler;
	EntityContainerListWidget viewScopeWidget, editScopeWidget;
	SynapseAlert synAlert;
	EntityView currentView;
	ViewType currentViewType;
	
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public ScopeWidget(ScopeWidgetView view, 
			SynapseClientAsync synapseClient, 
			EntityContainerListWidget viewScopeWidget, 
			EntityContainerListWidget editScopeWidget,
			SynapseAlert synAlert){
		this.synapseClient = synapseClient;
		this.view = view;
		this.viewScopeWidget = viewScopeWidget;
		this.editScopeWidget = editScopeWidget;
		this.synAlert = synAlert;
		
		view.setPresenter(this);
		view.setEditableEntityListWidget(editScopeWidget.asWidget());
		view.setEntityListWidget(viewScopeWidget.asWidget());
		view.setSynAlert(synAlert.asWidget());
	}

	public void configure(EntityBundle bundle, boolean isEditable, EntityUpdatedHandler updateHandler) {
		this.isEditable = isEditable;
		this.bundle = bundle;
		this.updateHandler = updateHandler;
		boolean isVisible = bundle.getEntity() instanceof EntityView;
		if (isVisible) {
			currentView = (EntityView) bundle.getEntity();
			viewScopeWidget.configure(currentView.getScopeIds(), false, getTableType(currentView));
			view.setEditButtonVisible(isEditable);
		}
		view.setVisible(isVisible);
	}

	@Override
	public void onSelectFilesAndTablesView() {
		currentViewType = ViewType.file_and_table;
	}
	
	@Override
	public void onSelectFilesOnlyView() {
		currentViewType = ViewType.file;
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void onSave() {
		// update scope
		synAlert.clear();
		view.setLoading(true);
		currentView.setScopeIds(editScopeWidget.getEntityIds());
		currentView.setType(currentViewType);
		synapseClient.updateEntity(currentView, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity entity) {
				view.setLoading(false);
				view.hideModal();
				updateHandler.onPersistSuccess(new EntityUpdatedEvent());
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
		// configure edit list, and show modal
		editScopeWidget.configure(currentView.getScopeIds(), true, getTableType(currentView));
		
		currentViewType = currentView.getType();
		boolean isFileView = ViewType.file.equals(currentViewType) || ViewType.file_and_table.equals(currentViewType);
		view.setFileViewTypeSelectionVisible(isFileView);
		if (isFileView) {
			boolean isIncludeTables = ViewType.file_and_table.equals(currentViewType);
			view.setIsIncludeTables(isIncludeTables);	
		}
		
		view.showModal();
	}
}
