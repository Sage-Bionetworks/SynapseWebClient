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
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidgetView;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ShinySiteWidgetTest {
		
	ShinySiteWidget widget;
	ShinySiteWidgetView mockView;
	AuthenticationController mockAuthenticationController;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	String validSiteUrl = "http://glimmer.rstudio.com/rstudio/faithful/";
	String validSiteUrl2 = "https://shiny.synapse.org/rstudio/faithful/";

	String invalidSiteUrl = "http://google.com";
	
	@Before
	public void setup(){
		mockView = mock(ShinySiteWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget = new ShinySiteWidget(mockView, mockAuthenticationController);
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
		widget.configure(wikiKey, descriptor, null);
		verify(mockView).configure(eq(validSiteUrl), anyInt());
		
		descriptor.put(WidgetConstants.SHINYSITE_SITE_KEY, validSiteUrl2);
		widget.configure(wikiKey, descriptor, null);
		verify(mockView).configure(eq(validSiteUrl2), anyInt());
	}
	
	@Test
	public void testConfigureInvalid() {
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.SHINYSITE_SITE_KEY, invalidSiteUrl);
		widget.configure(wikiKey, descriptor, null);
		verify(mockView).showInvalidSiteUrl(invalidSiteUrl);
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
		assertTrue(ShinySiteWidget.isValidShinySite(validSiteUrl.toUpperCase()));
		assertFalse(ShinySiteWidget.isValidShinySite(invalidSiteUrl));
		assertFalse(ShinySiteWidget.isValidShinySite(null));
	}
	
	@Test
	public void testIsIncludePrincipalId() {
		Map<String, String> descriptor = new HashMap<String, String>();
		//default false
		assertFalse(ShinySiteWidget.isIncludePrincipalId(descriptor));
		//explicitly false
		descriptor.put(WidgetConstants.SHINYSITE_INCLUDE_PRINCIPAL_ID_KEY, "false");
		assertFalse(ShinySiteWidget.isIncludePrincipalId(descriptor));
		//parse true
		descriptor.put(WidgetConstants.SHINYSITE_INCLUDE_PRINCIPAL_ID_KEY, "tRuE");
		assertTrue(ShinySiteWidget.isIncludePrincipalId(descriptor));
		//invalid param should default to false
		descriptor.put(WidgetConstants.SHINYSITE_INCLUDE_PRINCIPAL_ID_KEY, "invalid");
		assertFalse(ShinySiteWidget.isIncludePrincipalId(descriptor));
	}
	
	
}
