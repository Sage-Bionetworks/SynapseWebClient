package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.IFrameView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ShinySiteWidgetTest {
		
	ShinySiteWidget widget;
	@Mock
	IFrameView mockView;
	@Mock
	AuthenticationController mockAuthenticationController;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	String validSiteUrl = "https://docs.google.com/a/sagebase.org/forms/myform";
	String validSiteUrl2 = "https://s3.amazonaws.com/static.synapse.org/rstudio/faithful/";

	String invalidSiteUrl = "http://glimmer.rstudio.com/rstudio/faithful/";
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget = new ShinySiteWidget(mockView, mockAuthenticationController, mockSynapseJSNIUtils);
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		when(mockSynapseJSNIUtils.getHostname(anyString())).thenReturn("test.sagebase.org");
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.SHINYSITE_SITE_KEY, validSiteUrl);
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).configure(eq(validSiteUrl), anyInt());
		
		descriptor.put(WidgetConstants.SHINYSITE_SITE_KEY, validSiteUrl2);
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).configure(eq(validSiteUrl2), anyInt());
	}
	
	@Test
	public void testConfigureInvalid() {
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.SHINYSITE_SITE_KEY, invalidSiteUrl);
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).showError(invalidSiteUrl + DisplayConstants.INVALID_SHINY_SITE);
	}
	
	@Test
	public void testGetHeightFromDescriptor() {
		Map<String, String> descriptor = new HashMap<String, String>();
		assertEquals(WidgetConstants.SHINYSITE_DEFAULT_HEIGHT_PX,ShinySiteWidget.getHeightFromDescriptor(descriptor));
				
		descriptor.put(WidgetConstants.HEIGHT_KEY, "500");
		assertEquals(500, ShinySiteWidget.getHeightFromDescriptor(descriptor));
	}
	
	@Test 
	public void testIsValidShinySite() {
		assertTrue(ShinySiteWidget.isValidShinySite(validSiteUrl, mockSynapseJSNIUtils));
		assertTrue(ShinySiteWidget.isValidShinySite(validSiteUrl.toUpperCase(), mockSynapseJSNIUtils));
		assertFalse(ShinySiteWidget.isValidShinySite(invalidSiteUrl, mockSynapseJSNIUtils));
		assertTrue(ShinySiteWidget.isValidShinySite("https://docs.google.com/a/sagebase.org/forms/d/1JmVWhcCAx26Jd94nFY8HhtVBgJSReaBbphZid16T6V4/viewform", mockSynapseJSNIUtils));
		assertFalse(ShinySiteWidget.isValidShinySite(null, mockSynapseJSNIUtils));
		
		//hostname test
		when(mockSynapseJSNIUtils.getHostname(anyString())).thenReturn("www.jayhodgson.com");
		assertFalse(ShinySiteWidget.isValidShinySite(invalidSiteUrl, mockSynapseJSNIUtils));
		when(mockSynapseJSNIUtils.getHostname(anyString())).thenReturn("anything.synapse.org");
		assertTrue(ShinySiteWidget.isValidShinySite(invalidSiteUrl, mockSynapseJSNIUtils));
		when(mockSynapseJSNIUtils.getHostname(anyString())).thenReturn("anything.sagebase.org");
		assertTrue(ShinySiteWidget.isValidShinySite(invalidSiteUrl, mockSynapseJSNIUtils));
	}
	
	@Test
	public void testIsIncludePrincipalId() {
		Map<String, String> descriptor = new HashMap<String, String>();
		//default false
		assertFalse(ShinySiteWidget.isIncludePrincipalId(descriptor));
		//explicitly false
		descriptor.put(WidgetConstants.INCLUDE_PRINCIPAL_ID_KEY, "false");
		assertFalse(ShinySiteWidget.isIncludePrincipalId(descriptor));
		//parse true
		descriptor.put(WidgetConstants.INCLUDE_PRINCIPAL_ID_KEY, "tRuE");
		assertTrue(ShinySiteWidget.isIncludePrincipalId(descriptor));
		//invalid param should default to false
		descriptor.put(WidgetConstants.INCLUDE_PRINCIPAL_ID_KEY, "invalid");
		assertFalse(ShinySiteWidget.isIncludePrincipalId(descriptor));
	}
	
	
}
