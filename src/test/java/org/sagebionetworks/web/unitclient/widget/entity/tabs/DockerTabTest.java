package org.sagebionetworks.web.unitclient.widget.entity.tabs;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;

public class DockerTabTest {
	@Mock
	Tab mockTab;
	@Mock
	DockerTabView mockView;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	CookieProvider mockCookies;
	DockerTab tab;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new DockerTab(mockView, mockTab, mockCookies);
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn("fake cookie");
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(tab);
	}

	@Test
	public void testSetTabClickedCallback() {
		tab.setTabClickedCallback(mockOnClickCallback);
		verify(mockTab).addTabClickedCallback(mockOnClickCallback);
	}

	@Test
	public void testConfigure() {
		String entityId = "syn1"; 
		String entityName = "Docker project test";
		String areaToken = "a=b&c=d";
		tab.configure(entityId, entityName, areaToken);
		verify(mockTab).setTabListItemVisible(true);
	}

	@Test
	public void testConfigureNotInAlpha() {
		String entityId = "syn1"; 
		String entityName = "Docker project test";
		String areaToken = "a=b&c=d";
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn(null);
		tab.configure(entityId, entityName, areaToken);
		verify(mockTab).setTabListItemVisible(false);
	}

	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}
}
