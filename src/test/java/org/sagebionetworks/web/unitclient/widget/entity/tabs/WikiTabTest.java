package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.WikiTab;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.place.shared.Place;

public class WikiTabTest {
	@Mock
	Tab mockTab;
	@Mock
	WikiPageWidget mockWikiPageWidget;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	CallbackP<String> mockWikiReloadHandler;
	WikiTab tab;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new WikiTab(mockTab, mockWikiPageWidget);
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
		String wikiPageId = "9";
		Boolean canEdit = true;
		WikiPageWidget.Callback callback = mock(WikiPageWidget.Callback.class);
		tab.configure(entityId, wikiPageId, canEdit, callback);
		
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(canEdit), eq(callback), eq(true), anyString());
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockTab).setPlace(captor.capture());
		Synapse place = (Synapse)captor.getValue();
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
