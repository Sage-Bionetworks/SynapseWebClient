package org.sagebionetworks.web.unitclient.widget.entity.registration;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.sagebionetworks.web.client.widget.WidgetFactory;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;

public class WidgetRegistrarImplTest {
		
	WidgetRegistrarImpl widgetRegistrar;
	WidgetFactory mockWidgetFactory;
	
	@Before
	public void setup(){	
		mockWidgetFactory = mock(WidgetFactory.class);
		widgetRegistrar= new WidgetRegistrarImpl(mockWidgetFactory);
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
		verify(mockWidgetFactory).createYouTubeWidget();
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.IMAGE_CONTENT_TYPE, null);
		verify(mockWidgetFactory).createImageWidget();
		widgetRegistrar.getWidgetRendererForWidgetDescriptor(null, WidgetConstants.PROVENANCE_CONTENT_TYPE, null);
		verify(mockWidgetFactory).createProvenanceWidget();
	}
	@Test
	public void testCreateWidgetEditors() {
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.YOUTUBE_CONTENT_TYPE, null);
		verify(mockWidgetFactory).createYouTubeWidgetEditor();
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.IMAGE_CONTENT_TYPE, null);
		verify(mockWidgetFactory).createImageWidgetEditor();
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(null, WidgetConstants.PROVENANCE_CONTENT_TYPE, null);
		verify(mockWidgetFactory).createProvenanceWidgetEditor();
	}
}
