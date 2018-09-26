package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.renderer.ButtonLinkWidget.SYNAPSE_USER_ID_QUERY_PARAM;

import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.renderer.ButtonLinkWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ButtonLinkWidgetView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

@RunWith(MockitoJUnitRunner.class)
public class ButtonLinkWidgetTest {
	ButtonLinkWidget widget;
	@Mock
	ButtonLinkWidgetView mockView;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	AuthenticationController mockAuthController;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	
	String baseUrl = "http://my.synapse.org";
	String subpage1 = "#!Synapse:1234";
	String subpage1Full = baseUrl + "/" + subpage1;
	String validExternalUrl = "http://www.jayhodgson.com";
	String buttonText = "Click here";
	String currentUserId = "1289834";
	
	@Before
	public void setup(){
		when(mockGwt.getHostPrefix()).thenReturn(baseUrl);
		widget = new ButtonLinkWidget(mockView, mockGwt, mockAuthController);
		when(mockAuthController.isLoggedIn()).thenReturn(false);
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

	@Test
	public void testConfigureWidth() {
		String width = "20px";
		Map<String, String> descriptor = getDefaultDescriptor();
		descriptor.put(ButtonLinkWidget.WIDTH, width);
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).configure(eq(wikiKey), eq(buttonText), eq(validExternalUrl), eq(false), eq(true));
		verify(mockView).setWidth(width);
	}
	
	@Test
	public void testConfigureNoWidth() {
		Map<String, String> descriptor = getDefaultDescriptor();
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).configure(eq(wikiKey), eq(buttonText), eq(validExternalUrl), eq(false), eq(true));
		verify(mockView).setSize(ButtonSize.LARGE);
		verify(mockView, times(0)).setWidth(anyString());
	}
	
	@Test
	public void testConfigureLoggedInIncludePrincipalIdMissing() {
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		Map<String, String> descriptor = getDefaultDescriptor();
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).configure(eq(wikiKey), eq(buttonText), eq(validExternalUrl), eq(false), eq(true));
	}
	
	@Test
	public void testConfigureLoggedInIncludePrincipalIdFalse() {
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		Map<String, String> descriptor = getDefaultDescriptor();
		descriptor.put(WidgetConstants.INCLUDE_PRINCIPAL_ID_KEY, Boolean.toString(false));
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).configure(eq(wikiKey), eq(buttonText), eq(validExternalUrl), eq(false), eq(true));
	}
	
	@Test
	public void testConfigureLoggedInIncludePrincipalIdTrueWithoutOtherParams() {
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(currentUserId);
		Map<String, String> descriptor = getDefaultDescriptor();
		descriptor.put(WidgetConstants.INCLUDE_PRINCIPAL_ID_KEY, Boolean.toString(true));
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).configure(eq(wikiKey), eq(buttonText), eq(validExternalUrl + "?" + SYNAPSE_USER_ID_QUERY_PARAM + currentUserId), eq(false), eq(true));
	}
	@Test
	public void testConfigureLoggedInIncludePrincipalIdTrueWithOtherParams() {
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(currentUserId);
		Map<String, String> descriptor = getDefaultDescriptor();
		descriptor.put(WidgetConstants.INCLUDE_PRINCIPAL_ID_KEY, Boolean.toString(true));
		String originalUrl = validExternalUrl + "?anotherparam=1";
		descriptor.put(WidgetConstants.LINK_URL_KEY, originalUrl);
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).configure(eq(wikiKey), eq(buttonText), eq(originalUrl + "&" + SYNAPSE_USER_ID_QUERY_PARAM + currentUserId), eq(false), eq(true));
	}
}
