package org.sagebionetworks.web.client.widget.entity;

import java.util.HashSet;
import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.dialog.EntityEditorDialog;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowFactory;
import org.sagebionetworks.web.shared.EntityConstants;
import org.sagebionetworks.web.shared.EntityType;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * This class can be used to edit existing entities or create new ones.
 * 
 * @author John
 *
 */
public class EntityEditor {
	
	EntitySchemaCache cache;
	AdapterFactory factory;
	AutoGenFactory entityFactory;
	EntityPropertyForm editorDialog;
	ClientLogger log;
	GlobalApplicationState globalApplicationState;
	SynapseClientAsync client;
	EntityUpdatedHandler entityUpdatedHandler;
	
	@Inject
	public EntityEditor(EntitySchemaCache cache, AdapterFactory factory,
			AutoGenFactory entityFactory, EntityPropertyForm editorDialog,
			ClientLogger log, GlobalApplicationState globalApplicationState, SynapseClientAsync client) {
		super();
		this.cache = cache;
		this.factory = factory;
		this.entityFactory = entityFactory;
		this.editorDialog = editorDialog;
		this.log = log;
		this.globalApplicationState = globalApplicationState;
		this.client = client;
	}

	/**
	 * Edit the given entity.
	 * @param bundle
	 * @param isNew
	 */
	public void editEntity(final EntityBundle bundle, final boolean isNew){
	    Entity entity = bundle.getEntity();
	    Annotations annos = bundle.getAnnotations();
    	// We want to filter out all transient properties.
    	ObjectSchema schema = cache.getSchemaEntity(entity);
    	Set<String> filter = createFilter(schema);
	    final Annotations newAnnos = copyAnnotations(annos);
	    // Create a new Adapter to capture the editor's changes
	    final JSONObjectAdapter newAdapter = copyEntityToAdapter(entity);
	    
	    // Create dialog title
	    StringBuilder title = new StringBuilder();
	    if (isNew) {
		title.append("Create ");
	    } else {
		title.append(DisplayConstants.LABEL_RENAME + " ");
	    }
	    title.append(DisplayUtils.getEntityTypeDisplay(schema));
	    
	    // Show the edit dialog.
	    editorDialog.showEditEntityDialog(title.toString(), bundle, newAdapter, schema, newAnnos, filter, new EntityEditorDialog.Callback(){

			@Override
			public void saveEntity(JSONObjectAdapter newAdapter, Annotations newAnnos) {
				onSaveEntity(newAdapter, newAnnos, isNew);
			}});
	}
	
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
	}

	/**
	 * Make a copy of the passed entity.
	 * @param entity
	 * @return
	 */
	public JSONObjectAdapter copyEntityToAdapter(Entity entity) {
		final JSONObjectAdapter newAdapter = factory.createNew();
	    try {
	    	// Write the current entity to an adapter
	    	entity.writeToJSONObject(newAdapter);
		} catch (JSONObjectAdapterException e) {
			throw new RuntimeException(e);
		}
		return newAdapter;
	}

	/**
	 * Create a copy of the annotaions.
	 * @param annos
	 * @return
	 */
	public Annotations copyAnnotations(Annotations annos) {
		final Annotations newAnnos = new Annotations();
	    if(annos != null){
	    	newAnnos.addAll(annos);
	    }
		return newAnnos;
	}

	/**
	 * Create a filter for the dialog.
	 * @param schema
	 * @return
	 */
	public Set<String> createFilter(ObjectSchema schema) {
		Set<String> filter = new HashSet<String>();
		ObjectSchema versionableScheam = cache.getEntitySchema(Versionable.EFFECTIVE_SCHEMA, Versionable.class);
		filter.addAll(versionableScheam.getProperties().keySet());
    	// Filter transient fields
    	EntityRowFactory.addTransientToFilter(schema, filter);
    	// Filter objects
    	EntityRowFactory.addObjectTypeToFilter(schema, filter);
		return filter;
	}
	
	/**
	 * Save a change to an entity.
	 * @param newAdapter
	 * @param newAnnos
	 * @param isNew
	 */
	public void onSaveEntity(final JSONObjectAdapter newAdapter, Annotations newAnnos, final boolean isNew){
		try {
			String annosJson = null;

			if(newAnnos != null){
				JSONObjectAdapter annoAdapter = factory.createNew();
				newAnnos.writeToJSONObject(annoAdapter);
				annosJson = annoAdapter.toJSONString();
			}
			String entityJson = newAdapter.toJSONString();
			// we are ready to send the changes to the server
			client.createOrUpdateEntity(entityJson, annosJson, isNew, new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String result) {
					//entity updated
					fireEntityUpdatedEvent();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					// If there was a failure then tell the client
					editorDialog.showErrorMessage(caught.getMessage());
					// Send the user back the start entity
					try{
						String toId = null;
						if(isNew){
							// For a failure to create a new entity go back to the parent entity
							toId = newAdapter.getString(EntityConstants.PARENT_ID);
						}else{
							// go back to the entity
							toId = newAdapter.getString(EntityConstants.ENTITY_ID);
						}
						globalApplicationState.getPlaceChanger().goTo(new Synapse(toId));
					} catch (JSONObjectAdapterException e) {
						// This now a runtime error
						String message = "Failed to get the parent id of an entity on a failure";
						log.error(message);
						editorDialog.showErrorMessage(e.getMessage());
						// go home
						globalApplicationState.getPlaceChanger().goTo(new Home("0"));
					}
				}
			});

		} catch (Exception e) {
			// This should not occur.
			log.error(e.getMessage());
			editorDialog.showErrorMessage(e.getMessage());
		}
	}

	/**
	 * Add a new entity to the given parent.
	 * @param type
	 * @param parentId
	 */
	public void addNewEntity(EntityType type, String parentId) {
		// Create a new entity.
		EntityBundle newBundle = createNewEntity(type.getClassName(), parentId);
		// edit the entity
		editEntity(newBundle, true);
		
	}

	/**
	 * Create a new entity of the given type and give parent.
	 * @param type
	 * @param parentId
	 * @return
	 */
	public EntityBundle createNewEntity(String className, String parentId) {
		Entity entity = (Entity) entityFactory.newInstance(className);
		entity.setParentId(parentId);
		entity.setEntityType(className);
		EntityBundle newBundle = new EntityBundle(entity, null, null, null, null, null, null, null);
		return newBundle;
	}

}
