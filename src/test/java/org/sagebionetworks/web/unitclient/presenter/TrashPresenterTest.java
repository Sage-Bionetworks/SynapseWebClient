package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.presenter.TrashPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.TrashView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TrashPresenterTest {
	
	public static final int ARBITRARY_OFFSET = 17;
	
	TrashPresenter presenter;
	TrashView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserAccountServiceAsync;
	GlobalApplicationState mockGlobalApplicationState;
	NodeModelCreator mockNodeModelCreator;
	SynapseClientAsync mockSynapse;
	PaginatedResults<TrashedEntity> trashList;
	
	@Before
	public void setup() throws JSONObjectAdapterException{
		mockView = mock(TrashView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserAccountServiceAsync = mock(UserAccountServiceAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapse = mock(SynapseClientAsync.class);
		presenter = new TrashPresenter(mockView, mockSynapse, mockGlobalApplicationState, mockAuthenticationController, mockNodeModelCreator);
		trashList = getTestTrash();
		
		when(mockNodeModelCreator.createPaginatedResults(anyString(), any(Class.class))).thenReturn(trashList);
		verify(mockView).setPresenter(presenter);
	}
	
	@Test
	public void testSetPlace() {
		Trash place = Mockito.mock(Trash.class);
		presenter.setPlace(place);
	}
	
	@Test
	public void testGetTrash() {
		AsyncMockStubber.callSuccessWith("").when(mockSynapse).viewTrashForUser(
				anyInt(), anyInt(), any(AsyncCallback.class));
		presenter.getTrash(ARBITRARY_OFFSET);
		verify(mockView).configure(anyList());
	}
	
	@Test
	public void testGetTrashFailure() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapse).viewTrashForUser(
				anyLong(), anyLong(), any(AsyncCallback.class));
		presenter.getTrash(ARBITRARY_OFFSET);
		verify(mockView).displayFailureMessage(anyString(), anyString());
	}
	
	@Test
	public void testPurgeAll() {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapse).purgeTrashForUser(
				any(AsyncCallback.class));
		presenter.purgeAll();
		verify(mockView).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testPurgeAllFailure() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapse).purgeTrashForUser(
				any(AsyncCallback.class));
		presenter.purgeAll();
		verify(mockView).displayFailureMessage(anyString(), anyString());	}

	@Test
	public void testPurgeEntities() {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapse).purgeMultipleTrashedEntitiesForUser(
				anySet(), any(AsyncCallback.class));
		presenter.purgeEntities(new HashSet<TrashedEntity>(trashList.getResults()));
		verify(mockView).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testPurgeEntitiesFailure() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapse).purgeMultipleTrashedEntitiesForUser(
				anySet(), any(AsyncCallback.class));
		presenter.purgeEntities(new HashSet<TrashedEntity>(trashList.getResults()));
		verify(mockView).displayFailureMessage(anyString(), anyString());	}
	
	@Test
	public void testRestore() {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapse).restoreFromTrash(
				anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapse).getEntity(
				anyString(), any(AsyncCallback.class));
		presenter.restoreEntity(trashList.getResults().get(0));
		verify(mockView).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testRestoreFailure() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapse).restoreFromTrash(
				anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapse).getEntity(
				anyString(), any(AsyncCallback.class));
		presenter.restoreEntity(trashList.getResults().get(0));
		verify(mockView).displayFailureMessage(anyString(), anyString());
	}
	
	@Test
	public void testRestoreParentNotFoundFailureRestoreCall() {
		ForbiddenException ex = mock(ForbiddenException.class);
		String forbiddenMessage = "Forbidden Exception error message";
		when(ex.getMessage()).thenReturn(forbiddenMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapse).restoreFromTrash(
				anyString(), anyString(), any(AsyncCallback.class));
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		presenter.restoreEntity(trashList.getResults().get(0));
		ArgumentCaptor<String> errorMessageCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockView).showErrorMessage(errorMessageCaptor.capture());
		assertTrue(errorMessageCaptor.getValue().contains(forbiddenMessage));
	}
	
	/* 
	 * Private Methods
	  */

	private static PaginatedResults<TrashedEntity> getTestTrash() {
		PaginatedResults<TrashedEntity> trashedEntities = new PaginatedResults<TrashedEntity>();
		
		List<TrashedEntity> trashList = new ArrayList<TrashedEntity>();
		TrashedEntity trashedEntity = new TrashedEntity();
		trashedEntity.setDeletedOn(new Date((long) 2112112112));
		trashedEntity.setDeletedByPrincipalId("Admin");
		trashedEntity.setOriginalParentId("syn333");
		trashedEntity.setEntityId("syn222");
		trashedEntity.setEntityName("Pokemon");
		trashList.add(trashedEntity);
		trashedEntity = new TrashedEntity();
		trashedEntity.setDeletedOn(new Date((long) 2112112112));
		trashedEntity.setDeletedByPrincipalId("Admin");
		trashedEntity.setOriginalParentId("syn222");
		trashedEntity.setEntityId("syn111");
		trashedEntity.setEntityName("Squirtle");
		trashList.add(trashedEntity);
		trashedEntities.setResults(trashList);
		trashedEntities.setTotalNumberOfResults(trashList.size());
		return trashedEntities;
	}
}
