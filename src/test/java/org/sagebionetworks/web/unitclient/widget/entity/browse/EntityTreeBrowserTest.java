package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityTreeBrowserTest {	
	EntityTreeBrowserView mockView;
	SearchServiceAsync mockSearchService; 
	AuthenticationController mockAuthenticationController;
	EntityTypeProvider mockEntityTypeProvider;
	GlobalApplicationState mockGlobalApplicationState;
	IconsImageBundle mockIconsImageBundle;
	AdapterFactory adapterFactory;
	
	EntityTreeBrowser entityTreeBrowser;
	List<String> searchResults;
	
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
		searchResults = new ArrayList<String>();
	}
	
	@Test
	public void testGetFolderChildren() {
		AsyncMockStubber.callSuccessWith(searchResults).when(mockSearchService).searchEntities(anyString(), anyList(), anyInt(), anyInt(), anyString(), anyBoolean(), any(AsyncCallback.class));
		AsyncCallback<List<EntityHeader>> mockCallback = mock(AsyncCallback.class);
		entityTreeBrowser.getFolderChildren("123", mockCallback);
		verify(mockCallback).onSuccess(anyList());
	}
	
	@Test
	public void testGetFolderChildrenRaceCondition() {
		AsyncCallback<List<EntityHeader>> mockCallback = mock(AsyncCallback.class);
		entityTreeBrowser.getFolderChildren("123", mockCallback);
		//capture the servlet call
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		verify(mockSearchService).searchEntities(anyString(), anyList(), anyInt(), anyInt(), anyString(), anyBoolean(), captor.capture());
		//before invoking asynccallback.success, set the current entity id to something else (simulating that the user has selected a different folder while this was still processing)
		entityTreeBrowser.setCurrentFolderChildrenEntityId("456");
		captor.getValue().onSuccess(searchResults);
		verify(mockCallback, never()).onSuccess(anyList());
	}
}
