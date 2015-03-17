package org.sagebionetworks.web.client.widget.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowFactory;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget shows the properties and annotations as a non-editable table grid.
 * 
 * @author John
 *
 */
public class AnnotationsWidget implements AnnotationsWidgetView.Presenter, IsWidget {
	
	private AdapterFactory factory;
	private EntitySchemaCache cache;
	private List<EntityRow<?>> rows;
	private AnnotationsWidgetView propertyView;
	private EntityBundle bundle;
	private EntityUpdatedHandler entityUpdatedHandler;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	/**
	 * 
	 * @param factory
	 * @param cache
	 * @param propertyView
	 */
	@Inject
	public AnnotationsWidget(AnnotationsWidgetView propertyView, AdapterFactory factory, EntitySchemaCache cache,
			 SynapseClientAsync synapseClient, JSONObjectAdapter jsonObjectAdapter, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController) {
		super();
		this.factory = factory;
		this.cache = cache;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.propertyView = propertyView;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.propertyView.setPresenter(this);
	}
	
	
	@Override
	public void configure(EntityBundle bundle, boolean canEdit) {
		this.bundle = bundle;
		// Create an adapter
		try {
			rows = getRows(bundle.getEntity(), bundle.getAnnotations(), factory, cache);
			// Pass the rows to the two views
			propertyView.configure(rows, canEdit);
		} catch (JSONObjectAdapterException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean isEmpty() {
		return rows.isEmpty();
	}
	
	public static List<EntityRow<?>> getRows(Entity entity, Annotations annotations, AdapterFactory factory, EntitySchemaCache cache) throws JSONObjectAdapterException {
		JSONObjectAdapter adapter = factory.createNew();
		entity.writeToJSONObject(adapter);
		ObjectSchema schema = cache.getSchemaEntity(entity);
		// Get the list of rows
		// Filter out all versionable properties
		ObjectSchema versionableScheam = cache.getEntitySchema(Versionable.EFFECTIVE_SCHEMA, Versionable.class);
		Set<String> filter = new HashSet<String>();
		// filter out all properties from versionable
		filter.addAll(versionableScheam.getProperties().keySet());
		// Filter all transient properties
		EntityRowFactory.addTransientToFilter(schema, filter);
		// Add all objects to the filter
		EntityRowFactory.addObjectTypeToFilter(schema, filter);
		List<EntityRow<?>> rows = EntityRowFactory.createEntityRowListForProperties(adapter, schema, filter);
		// Add the annotations to this list.
		rows.addAll(EntityRowFactory.createEntityRowListForAnnotations(annotations));
		return rows;
	}
	
	@Override
	public Widget asWidget() {
		// The view is the real widget.
		return propertyView.asWidget();
	}
	
	@Override
	public void deleteAnnotation(EntityRow row) {
		Annotations annos = bundle.getAnnotations();
		annos.deleteAnnotation(row.getLabel());
		updateEntity();
	}
	
	@Override
	public void updateAnnotation(EntityRow row) {
		Annotations annos = bundle.getAnnotations();
		annos.replaceAnnotation(row.getLabel(), row.getValue());
		updateEntity();
	}
	
	@Override
	public void addAnnotation(String name, ANNOTATION_TYPE type) {
		Annotations annos = bundle.getAnnotations();
		DisplayUtils.addAnnotation(annos, name, type);
		//save, and fire an entity updated event
		updateEntity();
	}
	
	public void updateEntity() {
		JSONObjectAdapter entityAdapter = jsonObjectAdapter.createNew();
		try {
			bundle.getEntity().writeToJSONObject(entityAdapter);
			JSONObjectAdapter annosAdapter = jsonObjectAdapter.createNew();
			bundle.getAnnotations().writeToJSONObject(annosAdapter);
			
			// update the entity
			synapseClient.createOrUpdateEntity(entityAdapter.toJSONString(), annosAdapter.toJSONString(), false, new AsyncCallback<String>() {			
				@Override
				public void onSuccess(String result) {
					fireEntityUpdatedEvent();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), propertyView)) {
						propertyView.showErrorMessage(DisplayConstants.ERROR_UPDATE_FAILED);			
					}
				}
			});
		} catch (JSONObjectAdapterException e) {
			DisplayUtils.handleJSONAdapterException(e, globalApplicationState.getPlaceChanger(), authenticationController.getCurrentUserSessionData());
		}

	}
	
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
	}

}
