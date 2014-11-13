package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.DELETED;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.DELETE_PREFIX;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.THE;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.WAS_SUCCESSFULLY_DELETED;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerView;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityActionControllerImplTest {

	EntityActionControllerView mockView;
	PreflightController mockPreflightController;
	EntityTypeProvider mockEntityTypeProvider;
	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	AuthenticationController mockAuthenticationController;
	AccessControlListModalWidget mockAccessControlListModalWidget;
	
	ActionMenuWidget mockActionMenu;
	EntityUpdatedHandler mockEntityUpdatedHandler;
	
	EntityBundle entityBundle;

	EntityActionControllerImpl controller;
	String parentId;
	String entityId;
	String entityDispalyType;

	@Before
	public void before() {
		mockView = Mockito.mock(EntityActionControllerView.class);
		mockPreflightController = Mockito.mock(PreflightController.class);
		mockEntityTypeProvider = Mockito.mock(EntityTypeProvider.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockGlobalApplicationState = Mockito.mock(GlobalApplicationState.class);
		mockPlaceChanger = Mockito.mock(PlaceChanger.class);
		mockAuthenticationController = Mockito
				.mock(AuthenticationController.class);
		mockAccessControlListModalWidget = Mockito
				.mock(AccessControlListModalWidget.class);
		
		mockActionMenu = Mockito.mock(ActionMenuWidget.class);
		mockEntityUpdatedHandler = Mockito.mock(EntityUpdatedHandler.class);
		
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		entityDispalyType = "Sometype";
		when(mockEntityTypeProvider.getEntityDispalyName(any(Entity.class))).thenReturn(entityDispalyType);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		
		// The controller under test.
		controller = new EntityActionControllerImpl(mockView,
				mockPreflightController, mockEntityTypeProvider,
				mockSynapseClient, mockGlobalApplicationState,
				mockAuthenticationController, mockAccessControlListModalWidget);
		
		parentId = "syn456";
		entityId = "syn123";
		Entity table = new TableEntity();
		table.setId(entityId);
		table.setParentId(parentId);
		UserEntityPermissions permissions = new UserEntityPermissions();
		permissions.setCanChangePermissions(true);
		permissions.setCanDelete(true);
		permissions.setCanPublicRead(true);
		entityBundle = new EntityBundle(table, null, permissions, null, null, null, null, null);
		
	}

	@Test
	public void testConfigure(){
		controller.configure(mockActionMenu, entityBundle, mockEntityUpdatedHandler);
		verify(mockAccessControlListModalWidget).configure(any(Entity.class), anyBoolean());
		//delete
		verify(mockActionMenu).setActionEnabled(Action.DELETE_ENTITY, true);
		verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, true);
		verify(mockActionMenu).setActionText(Action.DELETE_ENTITY, DELETE_PREFIX+entityDispalyType);
		verify(mockActionMenu).addActionListener(Action.DELETE_ENTITY, controller);
		// share
		verify(mockActionMenu).setActionEnabled(Action.SHARE, true);
		verify(mockActionMenu).setActionVisible(Action.SHARE, true);
		verify(mockActionMenu).addActionListener(Action.SHARE, controller);
	}
	
	@Test
	public void testConfigurePublicRead(){
		entityBundle.getPermissions().setCanPublicRead(true);
		controller.configure(mockActionMenu, entityBundle, mockEntityUpdatedHandler);
		verify(mockActionMenu).setActionIcon(Action.SHARE, IconType.GLOBE);
	}
	
	@Test
	public void testConfigureNotPublic(){
		entityBundle.getPermissions().setCanPublicRead(false);
		controller.configure(mockActionMenu, entityBundle, mockEntityUpdatedHandler);
		verify(mockActionMenu).setActionIcon(Action.SHARE, IconType.LOCK);
	}
	
	@Test
	public void testOnDeleteConfirmCancel(){
		/*
		 *  The user must be shown a confirm dialog before a delete.  Confirm is signaled via the Callback.invoke()
		 *  in this case we do not want to confirm.
		 */
		AsyncMockStubber.callNoInvovke().when(mockView).showConfirmDialog(anyString(), anyString(), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, mockEntityUpdatedHandler);
		// the call under tests
		controller.onAction(Action.DELETE_ENTITY);
		verify(mockView).showConfirmDialog(anyString(), anyString(), any(Callback.class));
		// should not make it to the pre-flight check
		verify(mockPreflightController, never()).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
	}
	
	@Test
	public void testOnDeleteConfirmedPreFlightFailed(){
		// confirm the delete
		AsyncMockStubber.callWithInvoke().when(mockView).showConfirmDialog(anyString(), anyString(), any(Callback.class));
		/*
		 * The preflight check is confirmed by calling Callback.invoke(), in this case it must not be invoked.
		 */
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, mockEntityUpdatedHandler);
		// the call under test
		controller.onAction(Action.DELETE_ENTITY);
		verify(mockView).showConfirmDialog(anyString(), anyString(), any(Callback.class));
		verify(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		// Must not make it to the actual delete since preflight failed.
		verify(mockSynapseClient, never()).deleteEntityById(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testOnDeleteConfirmedPreFlightPassedDeleteFailed(){
		// confirm the delete
		AsyncMockStubber.callWithInvoke().when(mockView).showConfirmDialog(anyString(), anyString(), any(Callback.class));
		// confirm pre-flight
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		String error = "some error";
		AsyncMockStubber.callFailureWith(new Throwable(error)).when(mockSynapseClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, mockEntityUpdatedHandler);
		// the call under test
		controller.onAction(Action.DELETE_ENTITY);
		verify(mockView).showConfirmDialog(anyString(), anyString(), any(Callback.class));
		verify(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		// an attempt to delete should be made
		verify(mockSynapseClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE);
	}
	
	@Test
	public void testOnDeleteConfirmedPreFlightPassedDeleteSuccess(){
		// confirm the delete
		AsyncMockStubber.callWithInvoke().when(mockView).showConfirmDialog(anyString(), anyString(), any(Callback.class));
		// confirm pre-flight
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, mockEntityUpdatedHandler);
		// the call under test
		controller.onAction(Action.DELETE_ENTITY);
		verify(mockView).showConfirmDialog(anyString(), anyString(), any(Callback.class));
		verify(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		// an attempt to delete should be made
		verify(mockSynapseClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(DELETED, THE + entityDispalyType + WAS_SUCCESSFULLY_DELETED);
		verify(mockPlaceChanger).goTo(new Synapse(parentId, null, EntityArea.TABLES, null) );
	}
	
	@Test
	public void testCreateDeletePlaceNullParentId(){
		entityBundle.getEntity().setParentId(null);
		controller.configure(mockActionMenu, entityBundle, mockEntityUpdatedHandler);
		// call under test
		Place result =controller.createDeletePlace();
		Place expected =  new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
		assertEquals(expected, result);
	}
	
	@Test
	public void testCreateDeletePlaceProject(){
		// setup a project
		Entity project = new Project();
		project.setId(entityId);
		project.setParentId(parentId);
		entityBundle = new EntityBundle(project, null, entityBundle.getPermissions(), null, null, null, null, null);
		controller.configure(mockActionMenu, entityBundle, mockEntityUpdatedHandler);
		// call under test
		Place result = controller.createDeletePlace();
		Place expected = new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
		assertEquals(expected, result);
	}
	
	@Test
	public void testCreateDeletePlaceFile(){
		// setup a project
		Entity file = new FileEntity();
		file.setId(entityId);
		file.setParentId(parentId);
		entityBundle = new EntityBundle(file, null, entityBundle.getPermissions(), null, null, null, null, null);
		controller.configure(mockActionMenu, entityBundle, mockEntityUpdatedHandler);
		// call under test
		Place result = controller.createDeletePlace();
		Place expected = new Synapse(parentId);
		assertEquals(expected, result);
	}
	
	@Test
	public void testOnShareNoChange(){
		/*
		 * Share change is confirmed by calling Callback.invoke(), in this case it must not be invoked.
		 */
		AsyncMockStubber.callNoInvovke().when(mockAccessControlListModalWidget).showSharing(any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, mockEntityUpdatedHandler);
		// method under test
		controller.onAction(Action.SHARE);
		verify(mockAccessControlListModalWidget).showSharing(any(Callback.class));
		verify(mockEntityUpdatedHandler, never()).onPersistSuccess(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testOnShareWithChange(){
		// invoke this time
		AsyncMockStubber.callWithInvoke().when(mockAccessControlListModalWidget).showSharing(any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, mockEntityUpdatedHandler);
		// method under test
		controller.onAction(Action.SHARE);
		verify(mockAccessControlListModalWidget).showSharing(any(Callback.class));
		verify(mockEntityUpdatedHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
	}
}
