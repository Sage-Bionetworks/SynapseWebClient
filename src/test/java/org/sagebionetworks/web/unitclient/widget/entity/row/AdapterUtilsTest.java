package org.sagebionetworks.web.unitclient.widget.entity.row;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.widget.entity.AdapterUtils;

public class AdapterUtilsTest {
	@Test
	public void testGetEntityForBadgeInfoProject() throws JSONObjectAdapterException {
		AdapterFactoryImpl adapterFactory = new AdapterFactoryImpl();
		
		Project project = new Project();
		project.setName("very cool project");
		project.setCreatedBy("very cool creator");
		project.setCreatedOn(new Date(2112112112));
		Project result;
		result = (Project) AdapterUtils.getEntityForBadgeInfo(adapterFactory, Project.class.getName(), EntityFactory.createJSONStringForEntity(project));
		
		assertEquals(project.getName(), result.getName());
		assertEquals(project.getCreatedBy(), result.getCreatedBy());
		assertEquals(project.getCreatedOn(), result.getCreatedOn());
	}
	
	@Test
	public void testGetEntityForBadgeInfoFolder() throws JSONObjectAdapterException {
		AdapterFactoryImpl adapterFactory = new AdapterFactoryImpl();
		
		Folder folder = new Folder();
		folder.setName("very cool project");
		folder.setCreatedBy("very cool creator");
		folder.setCreatedOn(new Date(2112112112));
		Folder result;
		result = (Folder) AdapterUtils.getEntityForBadgeInfo(adapterFactory, Folder.class.getName(), EntityFactory.createJSONStringForEntity(folder));
		
		assertEquals(folder.getName(), result.getName());
		assertEquals(folder.getCreatedBy(), result.getCreatedBy());
		assertEquals(folder.getCreatedOn(), result.getCreatedOn());
	}
	
	@Test
	public void testGetEntityForBadgeInfoFile() throws JSONObjectAdapterException {
		AdapterFactoryImpl adapterFactory = new AdapterFactoryImpl();
		
		FileEntity file = new FileEntity();
		file.setName("very cool project");
		file.setCreatedBy("very cool creator");
		file.setCreatedOn(new Date(2112112112));
		FileEntity result;
		result = (FileEntity) AdapterUtils.getEntityForBadgeInfo(adapterFactory, FileEntity.class.getName(), EntityFactory.createJSONStringForEntity(file));
		
		assertEquals(file.getName(), result.getName());
		assertEquals(file.getCreatedBy(), result.getCreatedBy());
		assertEquals(file.getCreatedOn(), result.getCreatedOn());
	}
	
	@Test
	public void testGetEntityForBadgeInfoNotHappyCase() throws JSONObjectAdapterException {
		AdapterFactoryImpl adapterFactory = new AdapterFactoryImpl();
		
		Data data = new Data();
		data.setName("very cool project");
		data.setCreatedBy("very cool creator");
		data.setCreatedOn(new Date(2112112112));
		Data result;
		result = (Data) AdapterUtils.getEntityForBadgeInfo(adapterFactory, Data.class.getName(), EntityFactory.createJSONStringForEntity(data));
		
		assertEquals(result, null);
	}
	
}
