package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;

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
	
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public ScopeWidget(ScopeWidgetView view, 
			SynapseClientAsync synapseClient, 
			EntityContainerListWidget viewScopeWidget, 
			EntityContainerListWidget editScopeWidget){
		this.synapseClient = synapseClient;
		this.view = view;
		this.viewScopeWidget = viewScopeWidget;
		this.editScopeWidget = editScopeWidget;
	}

	public void configure(EntityBundle bundle, boolean isEditable, EntityUpdatedHandler updateHandler) {
		this.isEditable = isEditable;
		this.bundle = bundle;
		this.updateHandler = updateHandler;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void onSave() {
		// update scope
	}
}
