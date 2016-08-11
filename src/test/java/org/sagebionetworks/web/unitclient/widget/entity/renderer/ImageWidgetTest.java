package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidgetView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ImageWidgetTest {
		
	ImageWidget widget;
	ImageWidgetView mockView;
	Map<String, String> descriptor;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	AuthenticationController mockAuthenticationController;
	String xsrfToken = "12345";
	@Before
	public void setup() throws JSONObjectAdapterException{
		mockView = mock(ImageWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		widget = new ImageWidget(mockView, mockAuthenticationController);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, "test name");
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentXsrfToken()).thenReturn(xsrfToken);
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		widget.configure(wikiKey,descriptor, null, null);
		verify(mockView).configure(any(WikiPageKey.class), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(Long.class), eq(xsrfToken));
	}
	
	@Test
	public void testConfigureDefaultResponsive() {
		descriptor.put(WidgetConstants.IMAGE_WIDGET_RESPONSIVE_KEY, null);
		widget.configure(wikiKey,descriptor, null, null);
		verify(mockView, never()).addStyleName(ImageWidget.MAX_WIDTH_NONE);
	}
	@Test
	public void testConfigureResponsive() {
		descriptor.put(WidgetConstants.IMAGE_WIDGET_RESPONSIVE_KEY, Boolean.TRUE.toString());
		widget.configure(wikiKey,descriptor, null, null);
		verify(mockView, never()).addStyleName(ImageWidget.MAX_WIDTH_NONE);
	}
	@Test
	public void testConfigureNotResponsive() {
		descriptor.put(WidgetConstants.IMAGE_WIDGET_RESPONSIVE_KEY, Boolean.FALSE.toString());
		widget.configure(wikiKey,descriptor, null, null);
		verify(mockView).addStyleName(ImageWidget.MAX_WIDTH_NONE);
	}
	
}
