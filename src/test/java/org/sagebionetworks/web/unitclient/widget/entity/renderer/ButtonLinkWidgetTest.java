package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.renderer.ButtonLinkWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ButtonLinkWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidgetView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ButtonLinkWidgetTest {
		
	ButtonLinkWidget widget;
	ButtonLinkWidgetView mockView;
	GWTWrapper mockGwt;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	
	String baseUrl = "http://my.synapse.org";
	String subpage1 = "#!Synapse:1234";
	String subpage1Full = baseUrl + "/" + subpage1;
	String validExternalUrl = "http://www.jayhodgson.com";
	String buttonText = "Click here";
	
	@Before
	public void setup(){
		mockView = mock(ButtonLinkWidgetView.class);
		mockGwt = mock(GWTWrapper.class);
		when(mockGwt.getHostPrefix()).thenReturn(baseUrl);
		widget = new ButtonLinkWidget(mockView, mockGwt);
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	private Map<String, String> getDefaultDescriptor() {
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.LINK_URL_KEY, validExternalUrl);
		descriptor.put(WidgetConstants.TEXT_KEY, buttonText);
		return descriptor;
	}
	@Test
	public void testConfigureHighlightNull() {
		Map<String, String> descriptor = getDefaultDescriptor();
		widget.configure(wikiKey, descriptor, null, null);
		//Should pass through all params.  Should not highlight, and should open in a new window
		verify(mockView).configure(eq(wikiKey), eq(buttonText), eq(validExternalUrl), eq(false), eq(true));
	}
	
	@Test
	public void testConfigureHighlightFalse() {
		Map<String, String> descriptor = getDefaultDescriptor();
		descriptor.put(WebConstants.HIGHLIGHT_KEY, Boolean.FALSE.toString());
		widget.configure(wikiKey, descriptor, null, null);
		//Should pass through all params.  Should not highlight, and should open in a new window
		verify(mockView).configure(eq(wikiKey), eq(buttonText), eq(validExternalUrl), eq(false), eq(true));
	}
	
	@Test
	public void testConfigureHighlightTrue() {
		Map<String, String> descriptor = getDefaultDescriptor();
		descriptor.put(WebConstants.HIGHLIGHT_KEY, Boolean.TRUE.toString());
		widget.configure(wikiKey, descriptor, null, null);
		//Should pass through all params.  Should highlight, and should open in a new window
		verify(mockView).configure(eq(wikiKey), eq(buttonText), eq(validExternalUrl), eq(true), eq(true));
	}
	
	
	@Test
	public void testIsOpenNewWindow() {
		assertTrue(widget.isOpenInNewWindow(validExternalUrl));
		assertFalse(widget.isOpenInNewWindow(null));
		assertFalse(widget.isOpenInNewWindow(subpage1));
		assertFalse(widget.isOpenInNewWindow(subpage1Full));
	}
	
	@Test
	public void testOverrideOpenNewWindowTrue() {
		Map<String, String> descriptor = getDefaultDescriptor();
		descriptor.put(ButtonLinkWidget.LINK_OPENS_NEW_WINDOW, Boolean.TRUE.toString());
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).configure(eq(wikiKey), eq(buttonText), eq(validExternalUrl), eq(false), eq(true));
	}
	
	@Test
	public void testOverrideOpenNewWindowFalse() {
		Map<String, String> descriptor = getDefaultDescriptor();
		descriptor.put(ButtonLinkWidget.LINK_OPENS_NEW_WINDOW, Boolean.FALSE.toString());
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).configure(eq(wikiKey), eq(buttonText), eq(validExternalUrl), eq(false), eq(false));
	}

	
}
