package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.EntityView;
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

Scope Widget design

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
   | onEdit (show modal)             | onSave
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
			viewScopeWidget.configure(currentView.getScopeIds(), false);
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
		currentView.setScopeIds(editScopeWidget.getEntityIds());
		synapseClient.updateEntity(currentView, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity entity) {
				view.hideModal();
				updateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void onEdit() {
		// configure edit list, and show modal
		editScopeWidget.configure(currentView.getScopeIds(), true);
		view.showModal();
	}
}
