package org.sagebionetworks.web.client.widget.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		Entity entity = bundle.getEntity();
		// Create an adapter
		JSONObjectAdapter adapter = factory.createNew();
		try {
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
			rows = EntityRowFactory.createEntityRowListForProperties(adapter, schema, filter);
			// Add the annotations to this list.
			rows.addAll(EntityRowFactory.createEntityRowListForAnnotations(bundle.getAnnotations()));
			// Pass the rows to the two views
			propertyView.setRows(rows);
		} catch (JSONObjectAdapterException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public Widget asWidget() {
		// The view is the real widget.
		return propertyView.asWidget();
	}



}
