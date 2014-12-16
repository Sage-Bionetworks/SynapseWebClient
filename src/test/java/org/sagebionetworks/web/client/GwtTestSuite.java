package org.sagebionetworks.web.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

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
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.schema.FORMAT;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.TYPE;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.presenter.AccountPresenter;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererNone;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Since the GWT test are so slow to start and we could not get the GWTTestSuite to work,
 * we put all GWT tests in one class.
 * @author jmhill
 *
 */
public class GwtTestSuite extends GWTTestCase {

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
		
		TableBundle tableBundle = new TableBundle();
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.BOOLEAN);
		cm.setId("123");
		tableBundle.setColumnModels(Arrays.asList(cm));
		tableBundle.setMaxRowsPerPage(new Long(678));
		
		List<AccessRequirement> ars = new ArrayList<AccessRequirement>();
		TermsOfUseAccessRequirement ar = new TermsOfUseAccessRequirement();
		ar.setConcreteType(TermsOfUseAccessRequirement.class.getName());
		ar.setTermsOfUse("foo");
		ars.add(ar);
		
		JSONEntityFactoryImpl factory = new JSONEntityFactoryImpl(new GwtAdapterFactory());
		// the is our transport object
		EntityBundleTransport transport = new EntityBundleTransport();
		transport.setEntityJson(factory.createJsonStringForEntity(entity));
		transport.setAnnotationsJson(factory.createJsonStringForEntity(annos));
		transport.setPermissions(uep);
		transport.setEntityPath(path);
		transport.setFileHandlesJson(entityListToString(fileHandles));
		transport.setTableData(tableBundle);
	
		transport.setAccessRequirementsJson(entityListToString(ars));
		transport.setUnmetDownloadAccessRequirementsJson(entityListToString(ars));
		
		// Now make sure we can translate it
		NodeModelCreatorImpl modelCreator = new NodeModelCreatorImpl(factory, new JSONObjectGwt());
		EntityBundle results = modelCreator.createEntityBundle(transport);
		assertNotNull(results);
		assertEquals(entity, results.getEntity());
		assertEquals(annos, results.getAnnotations());
		assertEquals(path, results.getPath());
		assertEquals(uep, results.getPermissions());
		assertEquals(ars, results.getAccessRequirements());
		assertEquals(ars, results.getUnmetDownloadAccessRequirements());
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
		// See SWC-1590.  On windows the value will be 13.4568 due to rounding up.
		assertEquals("13.4567".length(), APITableColumnRendererNone.getColumnValue("13.456789", formatter).length());
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
	

	@Test
	public void testEncodeTokenFFRealWorld() {
		//email validation token is decoded in FF only!
		//first, real world case.  This was the actual token read in by FF:
		String token = "userid=273960&email=scicomp@sagebase.org&timestamp=2014-08-04T15:06:48.257+0000&domain=SYNAPSE&mac=XI45by8krakTcSPS7mEthZtQYrk=";
		String result = AccountPresenter.encodeTokenKeysAndValues(token);
		//should contain encoded version of "+0000"
		Assert.assertTrue(result.contains("%2B0000"));
		//should contain the entire mac address, which has "=" in it that must be encoded
		Assert.assertTrue(result.contains("mac=XI45by8krakTcSPS7mEthZtQYrk%3D"));
		//should still contain ampersands and equals
		Assert.assertTrue(result.contains("&"));
		Assert.assertTrue(result.contains("="));
	}
	
	@Test
	public void testEncodeTokenEmptyString() {
		String token = "";
		String result = AccountPresenter.encodeTokenKeysAndValues(token);
		Assert.assertEquals("", result);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testEncodeTokenInvalid() {
		String key = "mac";
		String value = "Many=Equal=Signs";
		String encodedValue = "Many%3DEqual%3DSigns";
		
		String token = "userid=273960&"+key + "=" + value + "&anotherkey=andvalue";
		String result = AccountPresenter.encodeTokenKeysAndValues(token);
		Assert.assertTrue(result.contains(key));
		Assert.assertTrue(result.contains(encodedValue));
	}
	
	@Override
	public String toString() {
		return "GwtTestSuite for Module: "+getModuleName();
	}

}
