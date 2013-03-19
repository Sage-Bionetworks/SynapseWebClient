package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants.MarkdownWidthParam;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidgetView;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ShinySiteWidgetTest {
		
	ShinySiteWidget widget;
	ShinySiteWidgetView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", WidgetConstants.WIKI_OWNER_ID_ENTITY, null);
	String validSiteUrl = "http://glimmer.rstudio.com/rstudio/faithful/";
	String invalidSiteUrl = "http://google.com";
	
	@Before
	public void setup(){
		mockView = mock(ShinySiteWidgetView.class);
		widget = new ShinySiteWidget(mockView);
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.SHINYSITE_SITE_KEY, validSiteUrl);
		widget.configure(wikiKey, descriptor);
		verify(mockView).configure(eq(validSiteUrl), anyInt(), anyInt());
	}
	
	@Test
	public void testConfigureInvalid() {
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.SHINYSITE_SITE_KEY, invalidSiteUrl);
		widget.configure(wikiKey, descriptor);
		verify(mockView).showInvalidSiteUrl(invalidSiteUrl);
	}

	@Test
	public void testGetWidthFromDescriptor() {
		Map<String, String> descriptor = new HashMap<String, String>();
		assertEquals(DisplayUtils.getMarkdownWidth(null), ShinySiteWidget.getWidthFromDescriptor(descriptor));
		
		descriptor.put(WidgetConstants.SHINYSITE_WIDTH_KEY, MarkdownWidthParam.NARROW.toString());
		assertEquals(DisplayUtils.getMarkdownWidth(MarkdownWidthParam.NARROW), ShinySiteWidget.getWidthFromDescriptor(descriptor));

		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.SHINYSITE_WIDTH_KEY, MarkdownWidthParam.WIDE.toString());
		assertEquals(DisplayUtils.getMarkdownWidth(MarkdownWidthParam.WIDE), ShinySiteWidget.getWidthFromDescriptor(descriptor));
	}
	
	@Test
	public void testGetHeightFromDescriptor() {
		Map<String, String> descriptor = new HashMap<String, String>();
		assertEquals(WidgetConstants.SHINYSITE_DEFAULT_HEIGHT_PX,ShinySiteWidget.getHeightFromDescriptor(descriptor));
				
		descriptor.put(WidgetConstants.SHINYSITE_HEIGHT_KEY, "500");
		assertEquals(500, ShinySiteWidget.getHeightFromDescriptor(descriptor));
	}
	
	@Test 
	public void testIsValidShinySite() {
		assertTrue(ShinySiteWidget.isValidShinySite(validSiteUrl));
		assertFalse(ShinySiteWidget.isValidShinySite(invalidSiteUrl));
		assertFalse(ShinySiteWidget.isValidShinySite(null));
	}
}
