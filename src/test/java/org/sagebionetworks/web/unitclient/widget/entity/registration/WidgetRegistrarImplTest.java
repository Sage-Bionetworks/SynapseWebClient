package org.sagebionetworks.web.unitclient.widget.entity.registration;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;

public class WidgetRegistrarImplTest {
		
	WidgetRegistrarImpl widgetRegistrar;
	PortalGinInjector mockGinInjector;
	
	@Before
	public void setup(){	
		mockGinInjector = mock(PortalGinInjector.class);
		widgetRegistrar= new WidgetRegistrarImpl(mockGinInjector);
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
}
