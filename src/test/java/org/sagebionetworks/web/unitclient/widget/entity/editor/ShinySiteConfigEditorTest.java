package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants.MarkdownWidthParam;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ShinySiteConfigEditorTest {
		
	ShinySiteConfigEditor editor;
	ShinySiteConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", WidgetConstants.WIKI_OWNER_ID_ENTITY, null);
	String validSiteUrl = "http://glimmer.rstudio.com/rstudio/faithful/";
	
	@Before
	public void setup(){
		mockView = mock(ShinySiteConfigView.class);
		editor = new ShinySiteConfigEditor(mockView);
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.SHINYSITE_SITE_KEY, validSiteUrl);
		descriptor.put(WidgetConstants.SHINYSITE_WIDTH_KEY, MarkdownWidthParam.NARROW.toString());
		descriptor.put(WidgetConstants.SHINYSITE_HEIGHT_KEY, "500");
		editor.configure(wikiKey, descriptor);
		verify(mockView).configure(validSiteUrl, DisplayUtils.getMarkdownWidth(MarkdownWidthParam.NARROW), 500);		
	}

	@Test
	public void testUpdateDescriptorFromView() {
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor);
		
		when(mockView.getSiteUrl()).thenReturn(validSiteUrl);
		when(mockView.getSiteHeight()).thenReturn(500);
		when(mockView.getSiteWidth()).thenReturn(MarkdownWidthParam.WIDE);

		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getSiteUrl();
		verify(mockView).getSiteWidth();
		verify(mockView, atLeastOnce()).getSiteHeight();
		
		assertEquals(validSiteUrl, descriptor.get(WidgetConstants.SHINYSITE_SITE_KEY));
		assertEquals("500", descriptor.get(WidgetConstants.SHINYSITE_HEIGHT_KEY));
		assertEquals("WIDE", descriptor.get(WidgetConstants.SHINYSITE_WIDTH_KEY));

	}
}
