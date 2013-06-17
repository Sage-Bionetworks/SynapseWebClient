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
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.CookieHelper;
import org.sagebionetworks.web.client.factory.SystemFactory;
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
	SystemFactory mockSystemFactory;
	CookieHelper mockCookieHelper;

	@Before
	public void setup() throws JSONObjectAdapterException{
		mockView = mock(ImageWidgetView.class);
		mockSystemFactory = mock(SystemFactory.class);
		mockCookieHelper = mock(CookieHelper.class);
		when(mockSystemFactory.getCookieHelper()).thenReturn(mockCookieHelper);
		widget = new ImageWidget(mockView, mockSystemFactory);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, "test name");
		when(mockCookieHelper.isLoggedIn()).thenReturn(true);
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		widget.configure(wikiKey,descriptor);
		verify(mockView).configure(any(WikiPageKey.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
	}
}
