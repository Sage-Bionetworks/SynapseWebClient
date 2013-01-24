package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAnnotationDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAnnotationDialog.TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.DeleteAnnotationDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.EntityEditorDialog.Callback;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.row.EntityFormModel;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowFactory;
import org.sagebionetworks.web.shared.EntityBundleTransport;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * This is a form for editing entity properties.
 * 
 */
public class EntityPropertyForm implements EntityPropertyFormView.Presenter {

	EventBus bus;
	
	JSONObjectAdapter adapter;
	ObjectSchema schema;
	Annotations annos;
	Set<String> filter;
	EntityBundle bundle;
	EntityUpdatedHandler entityUpdatedHandler;
	NodeModelCreator nodeModelCreator;
	SynapseClientAsync synapseClient;
	EntityPropertyFormView view;
	WidgetRegistrar widgetRegistrar;
	Callback callback;
	
	@Inject
	public EntityPropertyForm(EntityPropertyFormView view, EventBus bus, NodeModelCreator nodeModelCreator, SynapseClientAsync synapseClient, SynapseJSNIUtils synapseJSNIUtils, WidgetRegistrar widgetRegistrar) {
		this.view = view;
		this.bus = bus;
		this.nodeModelCreator = nodeModelCreator;
		this.synapseClient = synapseClient;
		this.widgetRegistrar = widgetRegistrar;
		init();
	}
	
	public void init() {
		//if attachments should change, the entity must be updated. we should update the attachments, and etag.  then let our version (which may have other modifications) update
		if (entityUpdatedHandler == null) {
			entityUpdatedHandler = new EntityUpdatedHandler() {
				@Override
				public void onPersistSuccess(EntityUpdatedEvent event) {
					//ask for the new entity, update our attachments and etag, and reconfigure the attachments widget
					if (view.isComponentVisible() && bundle != null && bundle.getEntity() != null && bundle.getEntity().getId() != null)
						refreshEntityAttachments();
				}
			};
			bus.addHandler(EntityUpdatedEvent.getType(), entityUpdatedHandler);	
		}
	}
	
	public void showEditEntityDialog( String windowTitle, EntityBundle bundle, JSONObjectAdapter newAdapter,
			ObjectSchema schema, Annotations newAnnos, Set<String> filter, Callback callback){
		// Create the property from
	    setDataCopies(newAdapter, schema, newAnnos, filter, bundle);
		view.showEditEntityDialog(windowTitle);
		this.callback = callback;
 	}
	
	@Override
	public void saveButtonClicked() {
		callback.saveEntity(adapter, annos);	
	}

	@Override
	public void removeAnnotation() {
		// Show a form for adding an Annotations
		List<String> keys = new ArrayList<String>();
		keys.addAll(annos.keySet());
		DeleteAnnotationDialog.showDeleteAnnotationsDialog(keys, new DeleteAnnotationDialog.Callback() {
			@Override
			public void deletAnnotations(List<String> keysToDelete) {
				// Delete all of the selected annotations.
				for(String key: keysToDelete){
					annos.deleteAnnotation(key);
				}
				// Rebuild the models
				rebuildModel();
			}
		});
	}
	
	@Override
	public void addAnnotation() {
		// Show a form for adding an Annotations
		AddAnnotationDialog.showAddAnnotation(new AddAnnotationDialog.Callback(){

			@Override
			public void addAnnotation(String name, TYPE type) {
				// Add a new annotation
				if(TYPE.STRING == type){
					annos.addAnnotation(name, "");
				}else if(TYPE.DOUBLE == type){
					annos.addAnnotation(name, 0.0);
				}else if(TYPE.LONG == type){
					annos.addAnnotation(name, 0l);
				}else if(TYPE.DATE == type){
					annos.addAnnotation(name, new Date());
				}else{
					throw new IllegalArgumentException("Unknown type: "+type);
				}
				// Rebuild the models
				rebuildModel();
			}
		});
	}
	
	public void refreshEntityAttachments(Entity newEntity) throws JSONObjectAdapterException {
		bundle.setEntity(newEntity);
		String name = (String)adapter.get("name");
		boolean hasDescription = adapter.has("description");
		String description = null;
		if (hasDescription)
			description = (String)adapter.get("description");
		newEntity.setDescription(description);
		newEntity.setName(name);
		newEntity.writeToJSONObject(adapter);
		view.refresh();
	}
	
	public void refreshEntityAttachments() {
		// We need to refresh the entity, and update our entity bundle with the most current attachments and etag.
		view.showLoading();
		int mask = ENTITY;
		AsyncCallback<EntityBundleTransport> callback = new AsyncCallback<EntityBundleTransport>() {
			@Override
			public void onSuccess(EntityBundleTransport transport) {
				view.hideLoading();
				EntityBundle newBundle = null;
				try {
					newBundle = nodeModelCreator.createEntityBundle(transport);
					Entity newEntity = newBundle.getEntity();
					refreshEntityAttachments(newEntity);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.hideLoading();
				view.showErrorMessage(DisplayConstants.ERROR_UNABLE_TO_LOAD + caught.getMessage());
			}			
		};
		
		synapseClient.getEntityBundle(bundle.getEntity().getId(), mask, callback);
	}
	
	@Override
	public void showPreview(String descriptionMarkdown, String baseUrl) {
	    //get the html for the markdown
	    synapseClient.markdown2Html(descriptionMarkdown, baseUrl, true, new AsyncCallback<String>() {
	    	@Override
			public void onSuccess(String result) {
	    		try {
					view.showPreview(result, bundle, widgetRegistrar, synapseClient, nodeModelCreator, adapter);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				//preview failed
				view.showErrorMessage(DisplayConstants.ENTITY_DESCRIPTION_PREVIEW_FAILED_TEXT + caught.getMessage());
			}
		});	
	}
	
	/**
	 * Pass editable copies of all objects.
	 * @param adapter
	 * @param schema
	 * @param annos
	 * @param filter
	 */
	public void setDataCopies(JSONObjectAdapter adapter, ObjectSchema schema, Annotations annos, Set<String> filter, EntityBundle bundle){
		this.adapter = adapter;
		this.schema = schema;
		this.annos = annos;
		this.filter = filter;
		this.bundle = bundle;
		rebuildModel();
	}
	
	@Override
	public EntityFormModel getFormModel() {
		return EntityRowFactory.createEntityRowList(this.adapter, this.schema, this.annos, filter);
	}
	/**
	 * Rebuild the model
	 */
	@SuppressWarnings("unchecked")
	private void rebuildModel(){
		view.setPresenter(this);
		view.refresh();
	}

	@Override
	public Entity getEntity() {
		return bundle.getEntity();
	}
	
	@Override
	public void insertWidget(String contentTypeKey) {
		BaseEditWidgetDescriptorPresenter.editNewWidget(view.getWidgetDescriptorEditor(), bundle.getEntity().getId(), WidgetConstants.WIKI_OWNER_ID_ENTITY,contentTypeKey, new WidgetDescriptorUpdatedHandler() {
			@Override
		public void onUpdate(WidgetDescriptorUpdatedEvent event) {
			if (event.getInsertValue()!=null) {
				view.insertMarkdown(event.getInsertValue());
			}
			refreshEntityAttachments();
		}
		});
	}
	@Override
	public void clear() {
		view.clear();
	}
	@Override
	public void showErrorMessage(String message) {
		view.showErrorMessage(message);
	}
	@Override
	public void showInfo(String title, String message) {
		view.showInfo(title, message);
	}
	@Override
	public void showLoading() {
		view.showLoading();
	}
}
