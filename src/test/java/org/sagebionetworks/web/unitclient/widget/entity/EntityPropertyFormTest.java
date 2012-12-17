package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

public class EntityPropertyFormTest {
	
	ExampleEntity entity1, entity2;
	String changedValue = "value modified from original";
	@Before
	public void before() throws JSONObjectAdapterException {
		// Setup the the entity
		String entityId = "123";
		String aclString = "acl string";
		String createdByString = "joe";
		String annotationsString = "annotations string";
		String descriptionString = "description string";
		String md5 = "md5";
		String name = "entity name";
		String uri = "uri";
		List<AttachmentData> entityAttachments = new ArrayList<AttachmentData>();
		String attachment1Name = "attachment1";
		AttachmentData attachment1 = new AttachmentData();
		attachment1.setName(attachment1Name);
		attachment1.setTokenId("token1");
		attachment1.setContentType(WidgetConstants.YOUTUBE_CONTENT_TYPE);
		entityAttachments.add(attachment1);
		Date modifiedOn = new Date();
		entity1 = getExampleEntity(entityId, aclString,  annotationsString,  createdByString,
				 descriptionString,  md5,  name,  uri, entityAttachments, modifiedOn);
		entity2 = getExampleEntity(entityId, aclString,  annotationsString,  createdByString,
				 descriptionString,  md5,  name,  uri, entityAttachments, modifiedOn);
	}
	
	private ExampleEntity getExampleEntity(String entityId, String aclString, String annotationsString, String createdByString,
			String descriptionString, String md5, String name, String uri, List<AttachmentData> entityAttachments, Date modifiedOn) {
		ExampleEntity entity1 = new ExampleEntity();
		entity1.setId(entityId);
		entity1.setEntityType(ExampleEntity.class.getName());
		entity1.setAccessControlList(aclString);
		entity1.setAnnotations(annotationsString);
		entity1.setCreatedBy(createdByString);
		entity1.setDescription(descriptionString);
		entity1.setMd5(md5);
		entity1.setName(name);
		entity1.setUri(uri);
		entity1.setAttachments(entityAttachments);
		entity1.setModifiedOn(modifiedOn);
		return entity1;
	}
	
	@Test
	public void testEqual() {
		assertTrue(DisplayUtils.isEqualDuringWidgetEditing(entity1, entity2));
	}
	
	@Test
	public void testSufficientlyEqual() {
		entity1.setDescription(changedValue);
		entity1.setModifiedOn(new Date());
		entity1.setEtag(changedValue);
		entity1.setAttachments(new ArrayList());
		assertTrue(DisplayUtils.isEqualDuringWidgetEditing(entity1, entity2));
	}
	@Test
	public void testUnEqual1() {
		entity1.setAccessControlList(changedValue);
		assertFalse(DisplayUtils.isEqualDuringWidgetEditing(entity1, entity2));
	}
	
	@Test
	public void testUnEqual2() {
		entity1.setAnnotations(changedValue);
		assertFalse(DisplayUtils.isEqualDuringWidgetEditing(entity1, entity2));
	}
	@Test
	public void testUnEqual3() {
		entity1.setModifiedBy(changedValue);
		assertFalse(DisplayUtils.isEqualDuringWidgetEditing(entity1, entity2));
	}
	@Test
	public void testUnEqual4() {
		entity1.setMd5(changedValue);
		assertFalse(DisplayUtils.isEqualDuringWidgetEditing(entity1, entity2));
	}
	@Test
	public void testUnEqual5() {
		entity1.setUri(changedValue);
		assertFalse(DisplayUtils.isEqualDuringWidgetEditing(entity1, entity2));
	}
}
