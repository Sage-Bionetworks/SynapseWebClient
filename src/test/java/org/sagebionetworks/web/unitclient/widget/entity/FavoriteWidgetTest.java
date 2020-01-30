package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FavoriteWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	FavoriteWidgetView mockView;
	String entityId = "syn123";
	FavoriteWidget favoriteWidget;
	EntityHeader fav;
	List<EntityHeader> favs;

	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;

	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(FavoriteWidgetView.class);
		favs = new ArrayList<EntityHeader>();
		fav = new EntityHeader();
		fav.setId("syn456");
		favs.add(fav);
		when(mockGlobalApplicationState.getFavorites()).thenReturn(favs);
		favoriteWidget = new FavoriteWidget(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController, mockSynapseJavascriptClient);
		favoriteWidget.configure(entityId);
		reset(mockView);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetIsFavorite() throws Exception {
		EntityHeader newFav = new EntityHeader();
		newFav.setId("syn123");
		favs.add(newFav);
		PaginatedResults<EntityHeader> favorites = new PaginatedResults<EntityHeader>();
		List<EntityHeader> results = new ArrayList<EntityHeader>();
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(new EntityHeader()).when(mockSynapseClient).addFavorite(anyString(), any(AsyncCallback.class));

		favoriteWidget.setIsFavorite(true);
		verify(mockView).setLoadingVisible(true);
		verify(mockView, Mockito.times(2)).setNotFavoriteVisible(false);
		verify(mockView).setFavoriteVisible(false);
		verify(mockView).setFavoriteVisible(true);
		verify(mockSynapseClient).addFavorite(eq(entityId), any(AsyncCallback.class));
		verify(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		verify(mockGlobalApplicationState).setFavorites(results);
		verify(mockView).setLoadingVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetIsFavoriteUnset() throws Exception {
		List<EntityHeader> results = new ArrayList<EntityHeader>();
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).removeFavorite(anyString(), any(AsyncCallback.class));

		favoriteWidget.setIsFavorite(false);
		verify(mockView).setLoadingVisible(true);
		verify(mockView, Mockito.times(2)).setFavoriteVisible(false);
		verify(mockView).setNotFavoriteVisible(false);
		verify(mockView).setNotFavoriteVisible(true);
		verify(mockSynapseClient).removeFavorite(eq(entityId), any(AsyncCallback.class));
		verify(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		verify(mockGlobalApplicationState).setFavorites(results);
		verify(mockView).setLoadingVisible(false);
	}

	@Test
	public void testUpdateIsFavoriteViewNotAFavorite() {
		// test when current entity is not a favorite
		when(mockGlobalApplicationState.getFavorites()).thenReturn(new ArrayList<EntityHeader>());
		favoriteWidget.updateIsFavoriteView();
		verify(mockView).setNotFavoriteVisible(true);
		verify(mockView).setFavoriteVisible(false);
	}

	@Test
	public void testUpdateIsFavoriteViewIsFavorite() {
		// test when current entity is a favorite
		ArrayList<EntityHeader> favorites = new ArrayList<EntityHeader>();
		EntityHeader fav = new EntityHeader();
		fav.setId(entityId);
		favorites.add(fav);
		when(mockGlobalApplicationState.getFavorites()).thenReturn(favorites);
		favoriteWidget.updateIsFavoriteView();
		verify(mockView).setNotFavoriteVisible(false);
		verify(mockView).setFavoriteVisible(true);
	}

	@Test
	public void testFavoriteAnynomous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		favoriteWidget.configure(entityId);
		verify(mockView).setLoadingVisible(false);
		verify(mockView, Mockito.never()).setNotFavoriteVisible(Mockito.anyBoolean());
		verify(mockView, Mockito.never()).setFavoriteVisible(Mockito.anyBoolean());
	}
}
