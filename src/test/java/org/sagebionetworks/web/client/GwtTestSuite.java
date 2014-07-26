package org.sagebionetworks.web.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sagebionetworks.client.exceptions.SynapseClientException;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.gwt.client.schema.adapter.GwtAdapterFactory;
import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Analysis;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Step;
import org.sagebionetworks.repo.model.Study;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.schema.FORMAT;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.TYPE;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererNone;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsView.ViewType;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.ColumnTypeSelector;
import org.sagebionetworks.web.client.widget.table.v2.ColumnTypeViewEnum;
import org.sagebionetworks.web.client.widget.table.v2.MaxSizeView;
import org.sagebionetworks.web.client.widget.table.v2.TextView;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Since the GWT test are so slow to start and we could not get the GWTTestSuite to work,
 * we put all GWT tests in one class.
 * @author jmhill
 *
 */
public class GwtTestSuite extends GWTTestCase {

	static PortalGinInjector ginjector;
	/**
	 * Must refer to a valid module that sources this class.
	 */
	public String getModuleName() { 
		return "org.sagebionetworks.web.Portal";
	}
	
	@Override
	public void gwtSetUp() {
		// Create a dataset with all fields filled in
	}
	
	@Test
	public void testNodeModelCreatorImpl_createDataset() throws JSONObjectAdapterException, RestServiceException{
		Study populatedDataset = new Study();
		initilaizedJSONEntityFromSchema(populatedDataset);
		assertNotNull(populatedDataset);
		// Get the JSON for the populate dataset
		JSONObjectAdapter adapter = populatedDataset.writeToJSONObject(JSONObjectGwt.createNewAdapter());
		String json = adapter.toJSONString();
		assertNotNull(json);
		// Use the factor to create a clone
		NodeModelCreatorImpl modelCreator = new NodeModelCreatorImpl(new JSONEntityFactoryImpl(new GwtAdapterFactory()), new JSONObjectGwt()); // jsonadapter and entitytypeprovider not needed for this deprecated model creation
		Study clone = modelCreator.createJSONEntity(json, Study.class);
		assertNotNull(clone);
		assertEquals(populatedDataset, clone);
	}
	
	@Test
	public void testNodeModelCreatorImpl_createLayer() throws JSONObjectAdapterException, RestServiceException{
		Data populatedLayer = new Data();
		initilaizedJSONEntityFromSchema(populatedLayer);
		assertNotNull(populatedLayer);
		// Get the JSON for the populate dataset
		JSONObjectAdapter adapter = populatedLayer.writeToJSONObject(JSONObjectGwt.createNewAdapter());
		String json = adapter.toJSONString();
		assertNotNull(json);
		// Use the factor to create a clone
		NodeModelCreatorImpl modelCreator = new NodeModelCreatorImpl(new JSONEntityFactoryImpl(new GwtAdapterFactory()), new JSONObjectGwt()); // jsonadapter and entitytypeprovider not needed for this deprecated model creation
		Data clone = modelCreator.createJSONEntity(json, Data.class);
		assertNotNull(clone);
		assertEquals(populatedLayer, clone);
	}
	
	@Test
	public void testNodeModelCreatorImpl_createProject() throws JSONObjectAdapterException, RestServiceException{
		Project populatedProject = new Project();
		initilaizedJSONEntityFromSchema(populatedProject);
		assertNotNull(populatedProject);
		// Get the JSON for the populate dataset
		JSONObjectAdapter adapter = populatedProject.writeToJSONObject(JSONObjectGwt.createNewAdapter());
		String json = adapter.toJSONString();
		assertNotNull(json);
		// Use the factor to create a clone
		NodeModelCreatorImpl modelCreator = new NodeModelCreatorImpl(new JSONEntityFactoryImpl(new GwtAdapterFactory()), new JSONObjectGwt()); // jsonadapter and entitytypeprovider not needed for this deprecated model creation
		Project clone = modelCreator.createJSONEntity(json, Project.class);
		assertNotNull(clone);
		assertEquals(populatedProject, clone);
	}
	
	
	@Test
	public void testNodeModelCreatorImpl_createAnalysis() throws JSONObjectAdapterException, RestServiceException{
		Analysis populatedAnalysis = new Analysis();
		initilaizedJSONEntityFromSchema(populatedAnalysis);
		assertNotNull(populatedAnalysis);
		// Get the JSON for the populate dataset
		JSONObjectAdapter adapter = populatedAnalysis.writeToJSONObject(JSONObjectGwt.createNewAdapter());
		String json = adapter.toJSONString();
		assertNotNull(json);
		// Use the factor to create a clone
		NodeModelCreatorImpl modelCreator = new NodeModelCreatorImpl(new JSONEntityFactoryImpl(new GwtAdapterFactory()), new JSONObjectGwt()); // jsonadapter and entitytypeprovider not needed for this deprecated model creation
		Analysis clone = modelCreator.createJSONEntity(json, Analysis.class);
		assertNotNull(clone);
		assertEquals(populatedAnalysis, clone);
	}
	
	@Test
	public void testNodeModelCreatorImpl_createStep() throws JSONObjectAdapterException, RestServiceException{
		Step populatedStep = new Step();
		initilaizedJSONEntityFromSchema(populatedStep);
		assertNotNull(populatedStep);
		// Get the JSON for the populate dataset
		JSONObjectAdapter adapter = populatedStep.writeToJSONObject(JSONObjectGwt.createNewAdapter());
		String json = adapter.toJSONString();
		assertNotNull(json);
		// Use the factor to create a clone
		NodeModelCreatorImpl modelCreator = new NodeModelCreatorImpl(new JSONEntityFactoryImpl(new GwtAdapterFactory()), new JSONObjectGwt()); // jsonadapter and entitytypeprovider not needed for this deprecated model creation
		Step clone = modelCreator.createJSONEntity(json, Step.class);
		assertNotNull(clone);
		assertEquals(populatedStep, clone);
	}
	
	/**
	 * Populate a given entity using all of the fields from the schema.
	 * @param toPopulate
	 * @throws JSONObjectAdapterException 
	 */
	private void initilaizedJSONEntityFromSchema(JSONEntity toPopulate) throws JSONObjectAdapterException{
		JSONObjectAdapter adapter =  JSONObjectGwt.createNewAdapter().createNew(toPopulate.getJSONSchema());
		ObjectSchema schema = new ObjectSchema(adapter);
		// This adapter will be used to populate the entity
		JSONObjectAdapter adapterToPopulate = JSONObjectGwt.createNewAdapter();
		Map<String, ObjectSchema> properteis = schema.getProperties();
		assertNotNull(properteis);
		int index = 0;
		for(String propertyName: properteis.keySet()){
			ObjectSchema propertySchema = properteis.get(propertyName);
			if(TYPE.STRING == propertySchema.getType()){
				// This could be a date or an enumeration.
				String value = "StringValue for "+propertyName;
				if(propertySchema.getFormat() == null && propertySchema.getEnum() == null){
					// This is just a normal string
					value = "StringValue for "+propertyName;
				}else if(FORMAT.DATE_TIME == propertySchema.getFormat()){
					value = adapter.convertDateToString(FORMAT.DATE_TIME, new Date());
				}else if(propertySchema.getEnum() != null){
					int enumIndex = propertySchema.getEnum().length-1 % index;
					value = propertySchema.getEnum()[enumIndex];
				}else{
					if(propertySchema.isRequired()){
						throw new IllegalArgumentException("Unknown FORMAT: "+propertySchema.getFormat()+" for required property");
					}
				}
				// Set the string value
				adapterToPopulate.put(propertyName, value);
			}else if(TYPE.BOOLEAN == propertySchema.getType()){
				Boolean value = index % 2 == 0;
				adapterToPopulate.put(propertyName, value);
			}else if(TYPE.INTEGER == propertySchema.getType()){
				Long value = new Long(123+index);
				adapterToPopulate.put(propertyName, value);
			}else if(TYPE.NUMBER == propertySchema.getType()){
				Double value = new Double(456.0909+index);
				adapterToPopulate.put(propertyName, value);
			}else{
				if(propertySchema.isRequired()){
					throw new IllegalArgumentException("Unknown type:"+propertySchema.getType()+" for required property");
				}
			}
			index++;
		}
		// Now populate it with data
		toPopulate.initializeFromJSONObject(adapterToPopulate);
	}
	
	@Test
	public void testCreateEntity() throws JSONObjectAdapterException{
		JSONObjectGwt adapter = new JSONObjectGwt();
		Analysis populatedAnalysis = new Analysis();
		initilaizedJSONEntityFromSchema(populatedAnalysis);
		// the entity type must be set correctly for this to work
		populatedAnalysis.setEntityType(Analysis.class.getName());
		populatedAnalysis.writeToJSONObject(adapter);
		String jsonString = adapter.toJSONString();
		
		JSONEntityFactoryImpl factory = new JSONEntityFactoryImpl(new GwtAdapterFactory());
		Analysis clone = (Analysis) factory.createEntity(jsonString);
		assertEquals(populatedAnalysis, clone);
	}
	
	@Test
	public void testCreateException(){
		// This will fail if the project is not configured correctly.
		SynapseException e = new SynapseClientException();
	}
	
	@Test
	public void testGwtJSONEntityFactory() throws JSONObjectAdapterException{
		// Make sure the GWT version of the factory works with the client
		JSONEntityFactoryImpl factory = new JSONEntityFactoryImpl(new GwtAdapterFactory());
		ObjectSchema projectSchema = factory.initializeEntity(Project.EFFECTIVE_SCHEMA, new ObjectSchema());
		assertNotNull(projectSchema);
		assertEquals("Project", projectSchema.getName());
	}
	
	/**
	 * Make sure we can use the GwtJSONEntityFactory to create each entity type registered.
	 * 
	 * @throws JSONObjectAdapterException
	 */
	@Test
	public void testGwtJSONEntityFactoryAllTypesRoundTrip() throws JSONObjectAdapterException{
		// Make sure the GWT version of the factory works with the client
		JSONEntityFactoryImpl factory = new JSONEntityFactoryImpl(new GwtAdapterFactory());
		AutoGenFactory autoGenFactory = new AutoGenFactory();
		Iterator<String> keyIt = autoGenFactory.getKeySetIterator();
		int index = 0;
		while(keyIt.hasNext()){
			String className = keyIt.next();
			JSONEntity jsonEntity = autoGenFactory.newInstance(className);
			assertNotNull(jsonEntity);
			if(jsonEntity instanceof Entity){
				Entity entity = (Entity) jsonEntity;
				entity.setName("Name:"+className);
				entity.setId(""+index);
				entity.setEtag("345");
				entity.setCreatedBy("someTest@sagebase.org");
				entity.setCreatedOn(new Date(System.currentTimeMillis()));
				entity.setModifiedBy("others@world.org");
				entity.setModifiedOn(new Date(entity.getCreatedOn().getTime()+10001));
				// Now create the json for this entity
				String json= factory.createJsonStringForEntity(entity);
				assertNotNull(json);
				// Now use the JSON and factory to create a clone
				JSONEntity clone = factory.createEntity(json, className);
				assertEquals(entity, clone);
				// Make sure we can do the same using the class
				clone = factory.createEntity(json, entity.getClass());
				assertEquals(entity, clone);
			}
			index++;
		}
	}
	
	@Test
	public void testEntitySchemaCache() throws JSONObjectAdapterException{
		// Use the GWT factory
		JSONEntityFactoryImpl factory = new JSONEntityFactoryImpl(new GwtAdapterFactory());
		EntitySchemaCacheImpl cache = new EntitySchemaCacheImpl(new GwtAdapterFactory());
		Project project = new Project();
		ObjectSchema projectSchema = cache.getSchemaEntity(project);
		assertNotNull(projectSchema);
		assertEquals("Project", projectSchema.getName());
		// The next time we get it from the cache it should be a cache hit.
		ObjectSchema projectSchemaSecond = cache.getSchemaEntity(project);
		assertTrue("The second fetch from the cache should have returned the same instance as the first call",projectSchema == projectSchemaSecond);
		
	}
	
	@Test
	public void testEntityBundleTranslation() throws Exception{
		// The entity
		ExampleEntity entity = new ExampleEntity();
		initilaizedJSONEntityFromSchema(entity);
		entity.setEntityType(ExampleEntity.class.getName());
		// annotaions
		Annotations annos = new Annotations();
		annos.setId(entity.getId());
		annos.setEtag(entity.getEtag());
		annos.addAnnotation("doubleKey", new Double(123.677));
		// The permission
		UserEntityPermissions uep = new UserEntityPermissions();
		uep.setCanAddChild(false);
		uep.setCanChangePermissions(true);
		uep.setCanView(false);
		// The path
		EntityPath path = new EntityPath();
		path.setPath(new ArrayList<EntityHeader>());
		EntityHeader header = new EntityHeader();
		header.setId(entity.getId());
		header.setName("RomperRuuuu");
		path.getPath().add(header);
		
		//File Handles
		List<FileHandle> fileHandles = new ArrayList<FileHandle>();
		FileHandle fh=new S3FileHandle();
		fh.setConcreteType(S3FileHandle.class.getName());
		fh.setFileName("not-a-virus.exe");
		fh.setId("20");
		fileHandles.add(fh);
		
		List<AccessRequirement> ars = new ArrayList<AccessRequirement>();
		TermsOfUseAccessRequirement ar = new TermsOfUseAccessRequirement();
		ar.setEntityType(TermsOfUseAccessRequirement.class.getName());
		ar.setTermsOfUse("foo");
		ars.add(ar);
		
		JSONEntityFactoryImpl factory = new JSONEntityFactoryImpl(new GwtAdapterFactory());
		// the is our transport object
		EntityBundleTransport transport = new EntityBundleTransport();
		transport.setEntityJson(factory.createJsonStringForEntity(entity));
		transport.setAnnotationsJson(factory.createJsonStringForEntity(annos));
		transport.setPermissionsJson(factory.createJsonStringForEntity(uep));
		transport.setEntityPathJson(factory.createJsonStringForEntity(path));
		transport.setFileHandlesJson(entityListToString(fileHandles));
	
		transport.setAccessRequirementsJson(entityListToString(ars));
		transport.setUnmetAccessRequirementsJson(entityListToString(ars));
		
		// Now make sure we can translate it
		NodeModelCreatorImpl modelCreator = new NodeModelCreatorImpl(factory, new JSONObjectGwt());
		EntityBundle results = modelCreator.createEntityBundle(transport);
		assertNotNull(results);
		assertEquals(entity, results.getEntity());
		assertEquals(annos, results.getAnnotations());
		assertEquals(path, results.getPath());
		assertEquals(uep, results.getPermissions());
		assertEquals(ars, results.getAccessRequirements());
		assertEquals(ars, results.getUnmetAccessRequirements());
		assertEquals(fileHandles, results.getFileHandles());
	}
	
	@Test
	public void testDecimalNumberFormat() {
		assertNull(APITableColumnRendererNone.getDecimalNumberFormat(null));
		NumberFormat formatter = APITableColumnRendererNone.getDecimalNumberFormat(1);
		assertEquals("12.3",formatter.format(12.3456));
		assertEquals("12.0",formatter.format(12));
	}
	
	@Test
	public void testGetColumnValue() {
		assertEquals("abc", APITableColumnRendererNone.getColumnValue("abc", null));
		assertEquals("13.456", APITableColumnRendererNone.getColumnValue("13.456", null));
		NumberFormat formatter = APITableColumnRendererNone.getDecimalNumberFormat(4);
		assertEquals("13.4567", APITableColumnRendererNone.getColumnValue("13.456789", formatter));
		assertEquals("hello", APITableColumnRendererNone.getColumnValue("hello", formatter));
	}
	
	public static String entityListToString(List<? extends JSONEntity> list) throws JSONObjectAdapterException {		
		JSONArrayAdapter aa = JSONObjectGwt.createNewAdapter().createNewArray();
		for (int i=0; i<list.size(); i++) {
			JSONObjectAdapter oa = JSONObjectGwt.createNewAdapter();
			list.get(i).writeToJSONObject(oa);
			aa.put(i, oa);
		}
		return aa.toJSONString();
	}
	
	/**
	 * Tests that we can get the text from a table TextView for both editors and viewers.
	 */
	@Test
	public void testTableTextView(){
		// null viewer
		TextView view = new TextView(null, false);
		assertEquals("", view.getText());
		// null editor
		view =new TextView(null, true);
		assertEquals("", view.getText());
		// non-null viewer
		view =new TextView("one", false);
		assertEquals("one", view.getText());
		// non-null editor
		view =new TextView("one", true);
		assertEquals("one", view.getText());
	}
	
	/**
	 * The max size view changes behavior depending on the type and isEditable.
	 */
	@Test
	public void testTableMaxSizeView(){
		// editor
		boolean isEditable = true;
		//String
		MaxSizeView msv = new MaxSizeView(ColumnTypeViewEnum.String, isEditable, null);
		assertEquals("", msv.getText());
		// non-string
		msv = new MaxSizeView(ColumnTypeViewEnum.Double, isEditable, null);
		assertEquals("", msv.getText());
		// String non-null
		msv = new MaxSizeView(ColumnTypeViewEnum.String, isEditable, 123L);
		assertEquals("123", msv.getText());
		// non-string
		msv = new MaxSizeView(ColumnTypeViewEnum.Double, isEditable, 123L);
		assertEquals("", msv.getText());
		
		// viewer
		isEditable = false;
		// String
		msv = new MaxSizeView(ColumnTypeViewEnum.String, isEditable, null);
		assertEquals("", msv.getText());
		// non-string
		msv = new MaxSizeView(ColumnTypeViewEnum.Double, isEditable, null);
		assertEquals("", msv.getText());
		// String non-null
		msv = new MaxSizeView(ColumnTypeViewEnum.String, isEditable, 123L);
		assertEquals("123", msv.getText());
		// non-string
		msv = new MaxSizeView(ColumnTypeViewEnum.Double, isEditable, 123L);
		assertEquals("", msv.getText());
	}
	
	@Test
	public void testTableMaxSizeViewChangeType(){
		boolean isEditable = true;
		//String
		MaxSizeView msv = new MaxSizeView(ColumnTypeViewEnum.String, isEditable, 123L);
		assertEquals("123", msv.getText());
		// change the type
		msv.onTypeChanged(ColumnTypeViewEnum.Double);
		assertEquals("", msv.getText());
		// Changing back to string should keep the original value
		msv.onTypeChanged(ColumnTypeViewEnum.String);
		assertEquals("123", msv.getText());
	}
	
	@Test
	public void testTableColumnTypeSelector(){
		// editor
		// We can only test strings since the Selector uses Scheduler.get().scheduleDeferred() 
		// to set values.
		ColumnTypeSelector selector = new ColumnTypeSelector(ColumnTypeViewEnum.String, true);
		assertEquals(ColumnTypeViewEnum.String, selector.getSelectedColumnType());

		// viewer
		selector = new ColumnTypeSelector(ColumnTypeViewEnum.String, false);
		assertEquals(ColumnTypeViewEnum.String, selector.getSelectedColumnType());
	}
	
	@Test
	public void testColumnModelTableRowRoundTrip(){
		ColumnModel original = new ColumnModel();
		original.setId("456");
		original.setColumnType(ColumnType.STRING);
		original.setMaximumSize(99L);
		original.setName("a name");
		original.setDefaultValue("some default");
		ColumnModelTableRow cmrow = new ColumnModelTableRow("id123", ViewType.EDITOR, original, true);
		// Get the value out of the row
		ColumnModel clone = cmrow.getColumnModel();
		assertFalse("A copy should have been created",clone == original);
		assertEquals("Did not get the same column out as was put in", original, clone);
		
		// Viewer
		cmrow = new ColumnModelTableRow("id123", ViewType.VIEWER, original, true);
		clone = cmrow.getColumnModel();
		assertEquals("Did not get the same column out as was put in", original, clone);
		
		// Non-editable
		cmrow = new ColumnModelTableRow("id123", ViewType.VIEWER, original, false);
		clone = cmrow.getColumnModel();
		assertEquals("Did not get the same column out as was put in", original, clone);
		
		// null fields
		original = new ColumnModel();
		original.setColumnType(ColumnType.STRING);
		cmrow = new ColumnModelTableRow("id123", ViewType.EDITOR, original, true);
		clone = cmrow.getColumnModel();
		assertEquals("Did not get the same column out as was put in", original, clone);
	}

	@Override
	public String toString() {
		return "GwtTestSuite for Module: "+getModuleName();
	}

}
