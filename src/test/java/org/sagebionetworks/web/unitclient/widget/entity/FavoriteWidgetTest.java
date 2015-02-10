package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FavoriteWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	FavoriteWidgetView mockView;
	String entityId = "syn123";
	FavoriteWidget favoriteWidget;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(FavoriteWidgetView.class);
		List<EntityHeader> favs = new ArrayList<EntityHeader>();
		EntityHeader fav = new EntityHeader();
		fav.setId("syn456");
		favs.add(fav);
		when(mockGlobalApplicationState.getFavorites()).thenReturn(favs);
		favoriteWidget = new FavoriteWidget(mockView, mockSynapseClient, mockGlobalApplicationState);
		favoriteWidget.configure(entityId);
		reset(mockView);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSetIsFavorite() throws Exception {
		PaginatedResults<EntityHeader> favorites = new PaginatedResults<EntityHeader>();
		List<EntityHeader> results = new ArrayList<EntityHeader>();
		favorites.setResults(results);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).getFavorites(anyInt(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).addFavorite(anyString(), any(AsyncCallback.class));
				
		favoriteWidget.setIsFavorite(true);
		verify(mockView).showLoading();
		verify(mockSynapseClient).addFavorite(eq(entityId), any(AsyncCallback.class));
		verify(mockSynapseClient).getFavorites(anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockGlobalApplicationState).setFavorites(results);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetIsFavoriteUnset() throws Exception {
		List<EntityHeader> results = new ArrayList<EntityHeader>();
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).getFavorites(anyInt(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).removeFavorite(anyString(), any(AsyncCallback.class));
				
		favoriteWidget.setIsFavorite(false);
		verify(mockView).showLoading();
		verify(mockSynapseClient).removeFavorite(eq(entityId), any(AsyncCallback.class));
		verify(mockSynapseClient).getFavorites(anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockGlobalApplicationState).setFavorites(results);
	}
	
	@Test
	public void testUpdateIsFavoriteViewNotAFavorite() {
		//test when current entity is not a favorite
		when(mockGlobalApplicationState.getFavorites()).thenReturn(new ArrayList<EntityHeader>());
		favoriteWidget.updateIsFavoriteView();
		verify(mockView).hideLoading();
		verify(mockView).showIsNotFavorite();
	}
	
	@Test
	public void testUpdateIsFavoriteViewIsFavorite() {
		//test when current entity is a favorite
		ArrayList<EntityHeader> favorites = new ArrayList<EntityHeader>();
		EntityHeader fav = new EntityHeader();
		fav.setId(entityId);
		favorites.add(fav);
		when(mockGlobalApplicationState.getFavorites()).thenReturn(favorites);
		favoriteWidget.updateIsFavoriteView();
		verify(mockView).hideLoading();
		verify(mockView).showIsFavorite();
	}

	
}
