package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.dialog.EntityEditorDialog;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowFactory;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.inject.Inject;

/**
 * A temp presenter
 * @author jmhill
 *
 */
public class TempPropertyPresenter extends LayoutContainer {

	EntitySchemaCache cache;
	AdapterFactory factory;
	AutoGenFactory entityFactory;
	EntityPropertyGrid view;
	EntityBundle bundle;
	List<EntityRow<?>> rows;
	VerticalPanel vp;
	EntityEditorDialog editorDialog;
	
	@Inject
	public TempPropertyPresenter(EntitySchemaCache cache,
			AdapterFactory factory, EntityPropertyGrid view, EntityEditorDialog editorDialog) {
		super();
		this.cache = cache;
		this.factory = factory;
		this.view = view;
		this.editorDialog = editorDialog;
		this.entityFactory = new AutoGenFactory();
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		this.clearState();
		// Create the grid
		vp = new VerticalPanel();
		vp.setSpacing(10);
		Button editButton = new Button();
		editButton.setText("Edit");
		// On edit we will show the edit dialog.
		editButton.addSelectionListener(new SelectionListener<ButtonEvent>() {				
	    	@Override
	    	public void componentSelected(ButtonEvent ce) {
	    	    Entity entity = bundle.getEntity();
	    	    Annotations annos = bundle.getAnnotations();
    	    	// We want to filter out all transient properties.
    	    	ObjectSchema schema = cache.getSchemaEntity(entity);
    	    	Set<String> filter = new HashSet<String>();
    			ObjectSchema versionableScheam = cache.getEntitySchema(Versionable.EFFECTIVE_SCHEMA, Versionable.class);
    			filter.addAll(versionableScheam.getProperties().keySet());
    	    	// Filter transient fields
    	    	EntityRowFactory.addTransientToFilter(schema, filter);
    	    	// Filter objects
    	    	EntityRowFactory.addObjectTypeToFilter(schema, filter);
	    	    // Create a new Adapter to capture the editor's changes
	    	    final JSONObjectAdapter newAdapter = factory.createNew();
	    	    final Annotations newAnnos = new Annotations();
	    	    if(annos != null){
	    	    	newAnnos.addAll(annos);
	    	    }
	    	    try {
	    	    	// Write the current entity to an adapter
	    	    	entity.writeToJSONObject(newAdapter);
	    		} catch (JSONObjectAdapterException e) {
	    			throw new RuntimeException(e);
	    		}
	    	    
	    	    // Show the edit dialog.
	    	    editorDialog.showEditEntityDialog(newAdapter, schema, newAnnos, filter, new EntityEditorDialog.Callback(){

					@Override
					public void saveEntity(JSONObjectAdapter newAdapter, Annotations newAnnos) {
						Entity newEntity;
						try {
							newEntity = (Entity) entityFactory.newInstance(bundle.getEntity().getEntityType());
							newEntity.initializeFromJSONObject(newAdapter);
							// Let the caller know about the save
							EntityBundle newBundel = new EntityBundle(newEntity, newAnnos, null, null, null);
							setEntity(newBundel);
						} catch (JSONObjectAdapterException e) {
							throw new RuntimeException(e);
						}
					}});
	    	}
	    });
		vp.add(editButton);
		ContentPanel cp = new ContentPanel();
		cp.setLayout(new FitLayout());
		cp.setHeight(10);
		cp.setBorders(false);
		cp.setBodyBorder(false);
		cp.setHeaderVisible(false);
		vp.add(cp);
		vp.add(this.view);
		this.add(vp);
	}
	


	public void setEntity(EntityBundle bundle){
		this.bundle = bundle;
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
			view.setRows(rows);
			this.layout(true);
			// Create the list of fields
		} catch (JSONObjectAdapterException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Create a sample entity
	 * 
	 * @return
	 */
	public static ExampleEntity createSample() {
		ExampleEntity example = new ExampleEntity();
		example.setId("12345");
		example.setName("My name is coolness");
		example.setConcept("Concept value");
		example.setSingleDate(new Date(System.currentTimeMillis()));
		example.setEntityType(example.getClass().getName());
		example.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque risus sapien, elementum a elementum adipiscing, laoreet condimentum odio. Vivamus pretium purus ac tellus tempor lobortis. Suspendisse nec nibh sit amet ligula consectetur tincidunt in vitae mi. Donec in pretium odio. Quisque lacus nunc, condimentum tincidunt placerat at, convallis in leo. Aliquam erat volutpat. Suspendisse sodales nisl sit amet quam eleifend fermentum. Vivamus interdum, arcu at tempor gravida, dolor neque tempus mi, ac viverra risus quam sit amet lacus. Fusce eget purus magna, nec lacinia neque. Pellentesque pretium metus ac velit mattis sed tempus sem adipiscing. Vivamus molestie lorem in dui viverra interdum. Sed nec elementum diam. ");
		example.setSingleString("This could be a very long string that wraps way off the screen. When that happens we want a portion of it to be shown on the screen, but the rest on tool tips.");
		example.setSingleDouble(123.45);
		example.setSingleInteger(42l);
		example.setStringList(new ArrayList<String>());
		example.getStringList().add("one");
		example.getStringList().add("two");
		example.getStringList().add("three");
		
		return example;
	}

	public void initializeWithTestData() {
		// Create an example entity
		ExampleEntity example = createSample();
		Annotations annos = new Annotations();
		annos.addAnnotation("stringAnno", "one");
		annos.addAnnotation("stringAnno", "two");
		annos.addAnnotation("longAnno", 123445667788l);
		annos.addAnnotation("doubleAnno", Double.MAX_VALUE);
		annos.addAnnotation("dataAnno", new Date());
		
		EntityBundle bundle = new EntityBundle(example, annos, null, null, null);
		this.setEntity(bundle);
	}
	
}
