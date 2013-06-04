package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FavoriteWidgetTest {

	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	GlobalApplicationState mockGlobalApplicationState;
	FavoriteWidgetView mockView;
	JSONObjectAdapter jsonObjectAdapter;
	String entityId = "syn123";
	FavoriteWidget favoriteWidget;

	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);		
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(FavoriteWidgetView.class);
		jsonObjectAdapter = new JSONObjectAdapterImpl();

		favoriteWidget = new FavoriteWidget(mockView, mockSynapseClient, mockNodeModelCreator, jsonObjectAdapter, mockGlobalApplicationState);
		favoriteWidget.configure(entityId);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSetIsFavorite() throws Exception {
		PaginatedResults<EntityHeader> favorites = new PaginatedResults<EntityHeader>();
		List<EntityHeader> results = new ArrayList<EntityHeader>();
		favorites.setResults(results);
		EntityHeader added = new EntityHeader();
		String getFavoritesJson = favorites.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();
		String addedJson = added.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();
		AsyncMockStubber.callSuccessWith(getFavoritesJson).when(mockSynapseClient).getFavorites(anyInt(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(addedJson).when(mockSynapseClient).addFavorite(anyString(), any(AsyncCallback.class));
		Mockito.<PaginatedResults<?>>when(mockNodeModelCreator.createPaginatedResults(anyString(), eq(EntityHeader.class))).thenReturn(favorites);
				
		favoriteWidget.setIsFavorite(true);
				
		verify(mockSynapseClient).addFavorite(eq(entityId), any(AsyncCallback.class));
		verify(mockSynapseClient).getFavorites(anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockGlobalApplicationState).setFavorites(results);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetIsFavoriteUnset() throws Exception {
		PaginatedResults<EntityHeader> favorites = new PaginatedResults<EntityHeader>();
		List<EntityHeader> results = new ArrayList<EntityHeader>();
		favorites.setResults(results);
		EntityHeader added = new EntityHeader();
		String getFavoritesJson = favorites.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();
		String addedJson = added.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();
		AsyncMockStubber.callSuccessWith(getFavoritesJson).when(mockSynapseClient).getFavorites(anyInt(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).removeFavorite(anyString(), any(AsyncCallback.class));
		Mockito.<PaginatedResults<?>>when(mockNodeModelCreator.createPaginatedResults(anyString(), eq(EntityHeader.class))).thenReturn(favorites);
				
		favoriteWidget.setIsFavorite(false);
				
		verify(mockSynapseClient).removeFavorite(eq(entityId), any(AsyncCallback.class));
		verify(mockSynapseClient).getFavorites(anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockGlobalApplicationState).setFavorites(results);
	}
	
}
