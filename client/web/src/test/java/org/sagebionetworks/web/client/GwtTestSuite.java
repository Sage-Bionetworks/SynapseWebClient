package org.sagebionetworks.web.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.gwt.client.schema.adapter.GwtAdapterFactory;
import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.repo.model.Agreement;
import org.sagebionetworks.repo.model.Analysis;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Dataset;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Eula;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.Layer;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Step;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.schema.FORMAT;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.TYPE;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

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
		Dataset populatedDataset = new Dataset();
		initilaizedJSONEntityFromSchema(populatedDataset);
		assertNotNull(populatedDataset);
		// Get the JSON for the populate dataset
		JSONObjectAdapter adapter = populatedDataset.writeToJSONObject(JSONObjectGwt.createNewAdapter());
		String json = adapter.toJSONString();
		assertNotNull(json);
		// Use the factor to create a clone
		NodeModelCreatorImpl modelCreator = new NodeModelCreatorImpl(new JSONEntityFactoryImpl(new GwtAdapterFactory()), new JSONObjectGwt()); // jsonadapter and entitytypeprovider not needed for this deprecated model creation
		Dataset clone = modelCreator.createEntity(json, Dataset.class);
		assertNotNull(clone);
		assertEquals(populatedDataset, clone);
	}
	
	@Test
	public void testNodeModelCreatorImpl_createLayer() throws JSONObjectAdapterException, RestServiceException{
		Layer populatedLayer = new Layer();
		initilaizedJSONEntityFromSchema(populatedLayer);
		assertNotNull(populatedLayer);
		// Get the JSON for the populate dataset
		JSONObjectAdapter adapter = populatedLayer.writeToJSONObject(JSONObjectGwt.createNewAdapter());
		String json = adapter.toJSONString();
		assertNotNull(json);
		// Use the factor to create a clone
		NodeModelCreatorImpl modelCreator = new NodeModelCreatorImpl(new JSONEntityFactoryImpl(new GwtAdapterFactory()), new JSONObjectGwt()); // jsonadapter and entitytypeprovider not needed for this deprecated model creation
		Layer clone = modelCreator.createEntity(json, Layer.class);
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
		Project clone = modelCreator.createEntity(json, Project.class);
		assertNotNull(clone);
		assertEquals(populatedProject, clone);
	}
	
	@Test
	public void testNodeModelCreatorImpl_createEULA() throws JSONObjectAdapterException, RestServiceException{
		Eula populatedEula = new Eula();
		initilaizedJSONEntityFromSchema(populatedEula);
		assertNotNull(populatedEula);
		// Get the JSON for the populate dataset
		JSONObjectAdapter adapter = populatedEula.writeToJSONObject(JSONObjectGwt.createNewAdapter());
		String json = adapter.toJSONString();
		assertNotNull(json);
		// Use the factor to create a clone
		NodeModelCreatorImpl modelCreator = new NodeModelCreatorImpl(new JSONEntityFactoryImpl(new GwtAdapterFactory()), new JSONObjectGwt()); // jsonadapter and entitytypeprovider not needed for this deprecated model creation
		Eula clone = modelCreator.createEntity(json, Eula.class);
		assertNotNull(clone);
		assertEquals(populatedEula, clone);
	}
	
	@Test
	public void testNodeModelCreatorImpl_Agreement() throws JSONObjectAdapterException, RestServiceException{
		Agreement populatedAgreement = new Agreement();
		initilaizedJSONEntityFromSchema(populatedAgreement);
		assertNotNull(populatedAgreement);
		// Get the JSON for the populate dataset
		JSONObjectAdapter adapter = populatedAgreement.writeToJSONObject(JSONObjectGwt.createNewAdapter());
		String json = adapter.toJSONString();
		assertNotNull(json);
		// Use the factor to create a clone
		NodeModelCreatorImpl modelCreator = new NodeModelCreatorImpl(new JSONEntityFactoryImpl(new GwtAdapterFactory()), new JSONObjectGwt()); // jsonadapter and entitytypeprovider not needed for this deprecated model creation
		Agreement clone = modelCreator.createEntity(json, Agreement.class);
		assertNotNull(clone);
		assertEquals(populatedAgreement, clone);
		// Make sure we can go back to json
		String jsonClone = modelCreator.createAgreementJSON(clone);
		assertEquals(json, jsonClone);
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
		Analysis clone = modelCreator.createEntity(json, Analysis.class);
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
		Step clone = modelCreator.createEntity(json, Step.class);
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
		SynapseException e = new SynapseException();
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
		EntitySchemaCacheImpl cache = new EntitySchemaCacheImpl(factory);
		Project project = new Project();
		ObjectSchema projectSchema = cache.getSchemaEntity(project);
		assertNotNull(projectSchema);
		assertEquals("Project", projectSchema.getName());
		// The next time we get it from the cache it should be a cache hit.
		ObjectSchema projectSchemaSecond = cache.getSchemaEntity(project);
		assertTrue("The second fetch from the cache should have returned the same instance as the first call",projectSchema == projectSchemaSecond);
		
	}
	
	@Test
	public void testEntityBundleTranslation() throws JSONObjectAdapterException, RestServiceException{
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
		
		JSONEntityFactoryImpl factory = new JSONEntityFactoryImpl(new GwtAdapterFactory());
		// the is our transport object
		EntityBundleTransport transport = new EntityBundleTransport();
		transport.setEntityJson(factory.createJsonStringForEntity(entity));
		transport.setAnnotaionsJson(factory.createJsonStringForEntity(annos));
		transport.setPermissionsJson(factory.createJsonStringForEntity(uep));
		transport.setEntityPathJson(factory.createJsonStringForEntity(path));
		
		// Now make sure we can translate it
		NodeModelCreatorImpl modelCreator = new NodeModelCreatorImpl(factory, new JSONObjectGwt());
		EntityBundle results = modelCreator.createEntityBundle(transport);
		assertNotNull(results);
		assertEquals(entity, results.getEntity());
		assertEquals(annos, results.getAnnotations());
		assertEquals(path, results.getPath());
		assertEquals(uep, results.getPermissions());
	}
	


	@Override
	public String toString() {
		return "GwtTestSuite for Module: "+getModuleName();
	}
	

}
