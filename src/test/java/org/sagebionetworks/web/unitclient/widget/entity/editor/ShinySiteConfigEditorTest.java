package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ShinySiteConfigEditorTest {
		
	ShinySiteConfigEditor editor;
	ShinySiteConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
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
		descriptor.put(WidgetConstants.SHINYSITE_HEIGHT_KEY, "500");
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).configure(validSiteUrl, 500);		
	}

	@Test
	public void testUpdateDescriptorFromView() {
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, null);
		
		when(mockView.getSiteUrl()).thenReturn(validSiteUrl);
		when(mockView.getSiteHeight()).thenReturn(500);

		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getSiteUrl();
		verify(mockView, atLeastOnce()).getSiteHeight();
		
		assertEquals(validSiteUrl, descriptor.get(WidgetConstants.SHINYSITE_SITE_KEY));
		assertEquals("500", descriptor.get(WidgetConstants.SHINYSITE_HEIGHT_KEY));
	}
}
