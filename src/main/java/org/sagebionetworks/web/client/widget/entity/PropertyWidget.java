package org.sagebionetworks.web.client.widget.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowFactory;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget shows the properties and annotations as a non-editable table grid.
 * 
 * @author John
 *
 */
public class PropertyWidget implements PropertyWidgetView.Presenter, IsWidget {
	
	private AdapterFactory factory;
	private EntitySchemaCache cache;
	private List<EntityRow<?>> rows;
	private PropertyWidgetView propertyView;

	/**
	 * 
	 * @param factory
	 * @param cache
	 * @param propertyView
	 */
	@Inject
	public PropertyWidget(AdapterFactory factory, EntitySchemaCache cache,
			PropertyWidgetView propertyView) {
		super();
		this.factory = factory;
		this.cache = cache;
		this.propertyView = propertyView;
	}
	
	
	@Override
	public void setEntityBundle(EntityBundle bundle) {
		// Create an adapter
		try {
			rows = getRows(bundle.getEntity(), bundle.getAnnotations(), factory, cache);
			// Pass the rows to the two views
			propertyView.setRows(rows);
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



}
