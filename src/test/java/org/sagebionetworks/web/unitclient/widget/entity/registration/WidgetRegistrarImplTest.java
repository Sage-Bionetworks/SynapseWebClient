package org.sagebionetworks.web.unitclient.widget.entity.registration;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.user.client.rpc.AsyncCallback;

import junit.framework.Assert;

public class WidgetRegistrarImplTest {
		
	WidgetRegistrarImpl widgetRegistrar;
	@Mock
	PortalGinInjector mockGinInjector;
	Map<String, String> testImageWidgetDescriptor;
	String testFileName = "testfile.png";
	@Mock
	AsyncCallback<WidgetEditorPresenter> mockAsyncCallback; 
	@Before
	public void setup(){	
		MockitoAnnotations.initMocks(this);
		widgetRegistrar= new WidgetRegistrarImpl(mockGinInjector, new JSONObjectAdapterImpl());
		testImageWidgetDescriptor = new HashMap<String, String>();
	}
	
	@Test
	public void testCreateWidgets() {
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.YOUTUBE_CONTENT_TYPE, null, null, null);
		verify(mockGinInjector).getVideoWidget();
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.IMAGE_CONTENT_TYPE, null, null, null);
		verify(mockGinInjector).getImageRenderer();
		reset(mockGinInjector);
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.IMAGE_LINK_EDITOR_CONTENT_TYPE, null, null, null);
		verify(mockGinInjector).getImageRenderer();
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.PROVENANCE_CONTENT_TYPE, null, null, null);
		verify(mockGinInjector).getProvenanceRenderer();
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.API_TABLE_CONTENT_TYPE, null, null, null);
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.QUERY_TABLE_CONTENT_TYPE, null, null, null);
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.LEADERBOARD_CONTENT_TYPE, null, null, null);
		verify(mockGinInjector, times(3)).getSynapseAPICallRenderer();
	}
	@Test
	public void testCreateWidgetEditors() {
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.YOUTUBE_CONTENT_TYPE, null, null, mockAsyncCallback);
		verify(mockGinInjector).getVideoConfigEditor();
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.IMAGE_CONTENT_TYPE, null, null, mockAsyncCallback);
		verify(mockGinInjector).getImageConfigEditor();
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.IMAGE_LINK_EDITOR_CONTENT_TYPE, null, null, mockAsyncCallback);
		verify(mockGinInjector).getImageLinkConfigEditor();
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.PROVENANCE_CONTENT_TYPE, null, null, mockAsyncCallback);
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
