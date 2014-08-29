package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserView;

public class EntityTreeBrowserTest {	
	EntityTreeBrowserView mockView;
	SearchServiceAsync mockSearchService; 
	AuthenticationController mockAuthenticationController;
	EntityTypeProvider mockEntityTypeProvider;
	GlobalApplicationState mockGlobalApplicationState;
	IconsImageBundle mockIconsImageBundle;
	AdapterFactory adapterFactory;
	
	EntityTreeBrowser entityTreeBrowser;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(EntityTreeBrowserView.class);
		mockSearchService = mock(SearchServiceAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);
		adapterFactory = new AdapterFactoryImpl();
		
		entityTreeBrowser = new EntityTreeBrowser(mockView, mockSearchService, mockAuthenticationController, mockEntityTypeProvider, mockGlobalApplicationState, mockIconsImageBundle, adapterFactory);
		verify(mockView).setPresenter(entityTreeBrowser);
		reset(mockView);
	}
		
	@Test
	public void testSortEntityHeadersByName() {
		EntityHeader header1 = new EntityHeader();
		EntityHeader header2 = new EntityHeader();
		EntityHeader header3 = new EntityHeader();
		header1.setName("Abra");
		header2.setName("marill");
		header3.setName("Zubat");
		
		List<EntityHeader> list = new ArrayList<EntityHeader>();
		list.add(header3);
		list.add(header2);
		list.add(header1);
		
		// [Zubat, marill, Abra]
		entityTreeBrowser.sortEntityHeadersByName(list);
		assertEquals(list.get(0), header1);
		assertEquals(list.get(1), header2);
		assertEquals(list.get(2), header3);
	}
	
}
