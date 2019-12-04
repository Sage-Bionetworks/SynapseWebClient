package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.WikiTab;
import org.sagebionetworks.web.shared.WikiPageKey;

public class WikiTabTest {
	@Mock
	Tab mockTab;
	@Mock
	WikiPageWidget mockWikiPageWidget;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	CallbackP<String> mockWikiReloadHandler;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	@Mock
	SynapseJavascriptClient mockJsClient;

	WikiTab tab;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new WikiTab(mockTab, mockPortalGinInjector);
		when(mockPortalGinInjector.getWikiPageWidget()).thenReturn(mockWikiPageWidget);
		when(mockPortalGinInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);
		tab.lazyInject();
	}

	@Test
	public void testSetTabClickedCallback() {
		tab.setTabClickedCallback(mockOnClickCallback);
		verify(mockTab).addTabClickedCallback(mockOnClickCallback);
	}

	@Test
	public void testSetWikiReloadHandler() {
		tab.setWikiReloadHandler(mockWikiReloadHandler);
		verify(mockWikiPageWidget).setWikiReloadHandler(mockWikiReloadHandler);
	}

	@Test
	public void testConfigure() {
		String entityId = "syn1";
		String entityName = "mr. bean";
		String wikiPageId = "9";
		Boolean canEdit = true;
		WikiPageWidget.Callback callback = mock(WikiPageWidget.Callback.class);
		tab.configure(entityId, entityName, wikiPageId, canEdit, callback, mockActionMenuWidget);

		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(canEdit), eq(callback));
		verify(mockWikiPageWidget).showSubpages(mockActionMenuWidget);
		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.WIKI, place.getArea());
		assertEquals(wikiPageId, place.getAreaToken());
	}

	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}

}
