package org.sagebionetworks.web.client.widget.table.modal.fileview;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.web.client.SynapseClientAsync;
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
	EntityContainerListWidget viewScopeWidget, editScopeWidget;
	SynapseAlert synAlert;
	EntityView currentView;
	TableType tableType;
	EventBus eventBus;
	
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public ScopeWidget(ScopeWidgetView view, 
			SynapseClientAsync synapseClient, 
			EntityContainerListWidget viewScopeWidget, 
			EntityContainerListWidget editScopeWidget,
			SynapseAlert synAlert,
			EventBus eventBus){
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
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

	public void configure(EntityBundle bundle, boolean isEditable) {
		this.isEditable = isEditable;
		this.bundle = bundle;
		boolean isVisible = bundle.getEntity() instanceof EntityView;
		if (isVisible) {
			currentView = (EntityView) bundle.getEntity();
			tableType = TableType.getTableType(currentView);
			viewScopeWidget.configure(currentView.getScopeIds(), false, tableType);
			view.setEditButtonVisible(isEditable && tableType != null);
			if (tableType == null) {
				synAlert.consoleError("View type mask is not supported by web client, blocking edit." + currentView.getViewTypeMask());
			}
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
		currentView.setScopeIds(editScopeWidget.getEntityIds());
		currentView.setViewTypeMask(tableType.getViewTypeMask().longValue());
		currentView.setType(null);
		synapseClient.updateEntity(currentView, new AsyncCallback<Entity>() {
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
		// configure edit list, and show modal
		editScopeWidget.configure(currentView.getScopeIds(), true, tableType);
		if (TableType.table.equals(tableType) || TableType.projects.equals(tableType)) {
			view.setViewTypeOptionsVisible(false);
		} else {
			view.setViewTypeOptionsVisible(true);
			//update the checkbox state based on the view type mask
			view.setIsFileSelected(tableType.isIncludeFiles());
			view.setIsFolderSelected(tableType.isIncludeFolders());
			view.setIsTableSelected(tableType.isIncludeTables());
		}
		
		view.showModal();
	}
}
