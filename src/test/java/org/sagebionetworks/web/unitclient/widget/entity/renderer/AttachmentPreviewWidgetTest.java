package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Page;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.renderer.AttachmentPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.AttachmentPreviewWidgetView;
import org.sagebionetworks.web.shared.WikiPageKey;

public class AttachmentPreviewWidgetTest {
		
	AttachmentPreviewWidget widget;
	AttachmentPreviewWidgetView mockView;
	Page testPage;
	Map<String, String> descriptor;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	
	@Before
	public void setup() throws JSONObjectAdapterException{
		mockView = mock(AttachmentPreviewWidgetView.class);
		widget = new AttachmentPreviewWidget(mockView);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, "test name");
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		widget.configure(wikiKey,descriptor, null);
		verify(mockView).configure(any(WikiPageKey.class), anyString());
	}
}
