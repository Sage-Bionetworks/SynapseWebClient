package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Page;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidgetView;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ImageWidgetTest {
		
	ImageWidget widget;
	ImageWidgetView mockView;
	Page testPage;
	Map<String, String> descriptor;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	AuthenticationController mockAuthenticationController;

	@Before
	public void setup() throws JSONObjectAdapterException{
		mockView = mock(ImageWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		widget = new ImageWidget(mockView, mockAuthenticationController);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, "test name");
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		widget.configure(wikiKey,descriptor, null);
		verify(mockView).configure(any(WikiPageKey.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
	}
}
