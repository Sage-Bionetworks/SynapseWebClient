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
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;

public class WidgetRegistrarImplTest {
		
	WidgetRegistrarImpl widgetRegistrar;
	PortalGinInjector mockGinInjector;
	NodeModelCreator mockNodeModelCreator;
	Map<String, String> testImageWidgetDescriptor;
	String testFileName = "testfile.png";
	@Before
	public void setup(){	
		mockGinInjector = mock(PortalGinInjector.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		widgetRegistrar= new WidgetRegistrarImpl(mockGinInjector,mockNodeModelCreator, new JSONObjectAdapterImpl() );
		testImageWidgetDescriptor = new HashMap<String, String>();
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
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetWidgetDescriptorMissingDelimiter() throws JSONObjectAdapterException {
		//from MD representation, verify that various widget descriptors can be constructed
		widgetRegistrar.getWidgetDescriptor("imagefileName=testing.png");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetWidgetDescriptorNoParams() throws JSONObjectAdapterException {
		//from MD representation, verify that various widget descriptors can be constructed
		widgetRegistrar.getWidgetDescriptor(WidgetConstants.IMAGE_CONTENT_TYPE);
	}
	
	public void testGetMDRepresentationDecoded() throws JSONObjectAdapterException {
		String aTestFilename = "getMDRepresentationDecodedTest.png";
		testImageWidgetDescriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, aTestFilename);
		String actualResult = widgetRegistrar.getMDRepresentation(WidgetConstants.IMAGE_CONTENT_TYPE, testImageWidgetDescriptor);
		//there's a single key/value pair, so there isn't an ordering problem in this test.  If another key/value were added to the Image WidgetDescriptor, 
		//then this equality test would be fragile (since the keys are not necessarily in order)
		Assert.assertEquals(WidgetConstants.IMAGE_CONTENT_TYPE+"?fileName="+aTestFilename, actualResult);
	}
	
	@Test
	public void testRoundTripEncodeDecode() {
		//round trip tests
		String in = "[{value1:\"test\" \nvalue2:[{v2a=\"1\"},{v2b=\"2\"}]}]";
		String encoded = widgetRegistrar.encodeValue(in);
		Assert.assertEquals("%5B%7Bvalue1%3A\"test\" %0Avalue2%3A%5B%7Bv2a%3D\"1\"%7D%2C%7Bv2b%3D\"2\"%7D%5D%7D%5D", encoded);
		String out = widgetRegistrar.decodeValue(encoded);
		Assert.assertEquals(in, out);
		
		in = "{}-_.!~*'()[]:;\n\r/?&=+,#";
		encoded = widgetRegistrar.encodeValue(in);
		out = widgetRegistrar.decodeValue(encoded);
		Assert.assertEquals(in, out);
		
		Assert.assertEquals("", widgetRegistrar.encodeValue(""));
		String oldParamName = "oldNonEscapedDot.png";  //now periods are escaped, but decoding one of these old values should not fail
		Assert.assertEquals(oldParamName, widgetRegistrar.decodeValue(oldParamName));
	}
	
	
	
}
