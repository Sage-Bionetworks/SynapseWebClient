package org.sagebionetworks.web.unitclient.widget.entity.registration;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.widget.ImageAttachmentWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;

public class WidgetRegistrarImplTest {
		
	WidgetRegistrarImpl widgetRegistrar;
	PortalGinInjector mockGinInjector;
	NodeModelCreator mockNodeModelCreator;
	ImageAttachmentWidgetDescriptor testImageWidgetDescriptor;
	String testFileName = "testfile.png";
	@Before
	public void setup(){	
		mockGinInjector = mock(PortalGinInjector.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		widgetRegistrar= new WidgetRegistrarImpl(mockGinInjector,mockNodeModelCreator, new JSONObjectAdapterImpl() );
		testImageWidgetDescriptor = new ImageAttachmentWidgetDescriptor();
		when(mockNodeModelCreator.newInstance(anyString())).thenReturn(testImageWidgetDescriptor);
		
	}
	
	@Test
	public void testKnownWidgetTypes() {
		assertNotNull(widgetRegistrar.getWidgetClass(WidgetConstants.YOUTUBE_CONTENT_TYPE));
		assertNotNull(widgetRegistrar.getWidgetClass(WidgetConstants.IMAGE_CONTENT_TYPE));
		assertNotNull(widgetRegistrar.getWidgetClass(WidgetConstants.PROVENANCE_CONTENT_TYPE));
	}
	
	@Test
	public void testCreateWidgets() {
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.YOUTUBE_CONTENT_TYPE, null);
		verify(mockGinInjector).getYouTubeRenderer();
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.IMAGE_CONTENT_TYPE, null);
		verify(mockGinInjector).getImageRenderer();
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.PROVENANCE_CONTENT_TYPE, null);
		verify(mockGinInjector).getProvenanceRenderer();
	}
	@Test
	public void testCreateWidgetEditors() {
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.YOUTUBE_CONTENT_TYPE, null);
		verify(mockGinInjector).getYouTubeConfigEditor();
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.IMAGE_CONTENT_TYPE, null);
		verify(mockGinInjector).getImageConfigEditor();
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.PROVENANCE_CONTENT_TYPE, null);
		verify(mockGinInjector).getProvenanceConfigEditor();
	}
	@Test
	public void testGetWidgetDescriptor() throws JSONObjectAdapterException {
		//from MD representation, verify that various widget descriptors can be constructed
		ImageAttachmentWidgetDescriptor actualWidgetDescriptor = (ImageAttachmentWidgetDescriptor)widgetRegistrar.getWidgetDescriptorFromDecoded(WidgetConstants.IMAGE_CONTENT_TYPE+"?fileName="+testFileName);
		Assert.assertEquals(testFileName, actualWidgetDescriptor.getFileName());
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetWidgetDescriptorWithNull () throws JSONObjectAdapterException {
		//from MD representation, verify that various widget descriptors can be constructed
		widgetRegistrar.getWidgetDescriptorFromDecoded(null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetWidgetDescriptorWithEmpty() throws JSONObjectAdapterException {
		//from MD representation, verify that various widget descriptors can be constructed
		widgetRegistrar.getWidgetDescriptorFromDecoded("");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetWidgetDescriptorMissingDelimiter() throws JSONObjectAdapterException {
		//from MD representation, verify that various widget descriptors can be constructed
		widgetRegistrar.getWidgetDescriptorFromDecoded("imagefileName=testing.png");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetWidgetDescriptorNoParams() throws JSONObjectAdapterException {
		//from MD representation, verify that various widget descriptors can be constructed
		widgetRegistrar.getWidgetDescriptorFromDecoded(WidgetConstants.IMAGE_CONTENT_TYPE);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetWidgetDescriptorWrongParams() throws JSONObjectAdapterException {
		//from MD representation, verify that various widget descriptors can be constructed
		widgetRegistrar.getWidgetDescriptorFromDecoded(WidgetConstants.IMAGE_CONTENT_TYPE+"?invalidParam="+testFileName);
	}
	
	@Test
	public void testGetMDRepresentationDecoded() throws JSONObjectAdapterException {
		String aTestFilename = "getMDRepresentationDecodedTest.png";
		testImageWidgetDescriptor.setFileName(aTestFilename);
		String actualResult = widgetRegistrar.getMDRepresentationDecoded(testImageWidgetDescriptor);
		//there's a single key/value pair, so there isn't an ordering problem in this test.  If another key/value were added to the Image WidgetDescriptor, 
		//then this equality test would be fragile (since the keys are not necessarily in order)
		Assert.assertEquals(WidgetConstants.IMAGE_CONTENT_TYPE+"?fileName="+aTestFilename, actualResult);
	}
	
	
}
