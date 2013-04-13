package org.sagebionetworks.web.unitclient.widget.entity.registration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.factory.EditorFactory;
import org.sagebionetworks.web.client.factory.RendererFactory;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;

public class WidgetRegistrarImplTest {
		
	WidgetRegistrarImpl widgetRegistrar;
	RendererFactory mockRendererFactory;
	EditorFactory mockEditorFactory;
	NodeModelCreator mockNodeModelCreator;
	Map<String, String> testImageWidgetDescriptor;
	String testFileName = "testfile.png";
	@Before
	public void setup(){	
		mockRendererFactory = mock(RendererFactory.class);
		mockEditorFactory = mock(EditorFactory.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		widgetRegistrar= new WidgetRegistrarImpl(mockNodeModelCreator, new JSONObjectAdapterImpl(), mockRendererFactory, mockEditorFactory);
		testImageWidgetDescriptor = new HashMap<String, String>();
	}
	
	@Test
	public void testCreateWidgets() {
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.YOUTUBE_CONTENT_TYPE, null, true);
		verify(mockRendererFactory).getYouTubeRenderer();
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.IMAGE_CONTENT_TYPE, null, false);
		verify(mockRendererFactory).getOldImageRenderer();
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.IMAGE_CONTENT_TYPE, null, true);
		verify(mockRendererFactory).getImageRenderer();
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.PROVENANCE_CONTENT_TYPE, null, true);
		verify(mockRendererFactory).getProvenanceRenderer();
	}
	@Test
	public void testCreateWidgetEditors() {
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.YOUTUBE_CONTENT_TYPE, null, true);
		verify(mockEditorFactory).getYouTubeConfigEditor();
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.IMAGE_CONTENT_TYPE, null, false);
		verify(mockEditorFactory).getOldImageConfigEditor();
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.IMAGE_CONTENT_TYPE, null, true);
		verify(mockEditorFactory).getImageConfigEditor();
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.PROVENANCE_CONTENT_TYPE, null, true);
		verify(mockEditorFactory).getProvenanceConfigEditor();
	}
	
	@Test
	public void testGetWidgetDescriptor() throws JSONObjectAdapterException {
		//from MD representation, verify that various widget descriptors can be constructed
		Map<String, String> actualWidgetDescriptor = widgetRegistrar.getWidgetDescriptor(WidgetConstants.IMAGE_CONTENT_TYPE+"?fileName="+testFileName);
		Assert.assertEquals(testFileName, actualWidgetDescriptor.get(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetWidgetDescriptorWithNull () throws JSONObjectAdapterException {
		//from MD representation, verify that various widget descriptors can be constructed
		widgetRegistrar.getWidgetDescriptor(null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetWidgetDescriptorWithEmpty() throws JSONObjectAdapterException {
		//from MD representation, verify that various widget descriptors can be constructed
		widgetRegistrar.getWidgetDescriptor("");
	}
	
	@Test
	public void testGetWidgetDescriptorNoParams() throws JSONObjectAdapterException {
		//from MD representation, verify that various widget descriptors can be constructed
		widgetRegistrar.getWidgetDescriptor(WidgetConstants.TOC_CONTENT_TYPE);
	}
	
	@Test
	public void testGetMDRepresentationDecoded() throws JSONObjectAdapterException {
		String aTestFilename = "getMDRepresentationDecodedTest.png";
		testImageWidgetDescriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, aTestFilename);
		String actualResult = widgetRegistrar.getMDRepresentation(WidgetConstants.IMAGE_CONTENT_TYPE, testImageWidgetDescriptor);
		//there's a single key/value pair, so there isn't an ordering problem in this test.  If another key/value were added to the Image WidgetDescriptor, 
		//then this equality test would be fragile (since the keys are not necessarily in order)
		Assert.assertEquals(WidgetConstants.IMAGE_CONTENT_TYPE+"?fileName=getMDRepresentationDecodedTest%2Epng", actualResult);
	}	
	
}
