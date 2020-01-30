package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class WikiSubpagesWidgetTest {
	@Mock
	WikiSubpagesView mockView;
	AdapterFactory adapterFactory;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	V2WikiOrderHint mockV2WikiOrderHint;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	ActionMenuWidget mockActionMenuWidget;

	WikiSubpagesWidget widget;
	List<V2WikiHeader> wikiHeadersList;
	V2WikiHeader testRootHeader;
	String entityId = "syn123", projectName = "a test project";
	Map<String, String> descriptor = new HashMap<String, String>();
	UserEntityPermissions permissions;
	@Mock
	Project mockWikiProject;
	@Mock
	V2WikiHeader mockWikiHeader;

	@Before
	public void before() throws JSONObjectAdapterException {
		adapterFactory = new AdapterFactoryImpl();
		widget = new WikiSubpagesWidget(mockView, mockAuthenticationController, mockSynapseJavascriptClient);

		EntityBundle bundle = new EntityBundle();
		permissions = new UserEntityPermissions();
		permissions.setCanEdit(true);
		bundle.setPermissions(permissions);
		when(mockWikiProject.getId()).thenReturn(entityId);
		when(mockWikiProject.getName()).thenReturn(projectName);
		bundle.setEntity(mockWikiProject);
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));

		AsyncMockStubber.callSuccessWith("").when(mockSynapseJavascriptClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		wikiHeadersList = new ArrayList<V2WikiHeader>();
		testRootHeader = new V2WikiHeader();
		testRootHeader.setId("123");
		testRootHeader.setParentId(null);
		testRootHeader.setTitle("my test root wiki header (page)");
		wikiHeadersList.add(testRootHeader);

		mockV2WikiOrderHint = mock(V2WikiOrderHint.class);
		when(mockV2WikiOrderHint.getIdList()).thenReturn(null);
		AsyncMockStubber.callSuccessWith(wikiHeadersList).when(mockSynapseJavascriptClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockV2WikiOrderHint).when(mockSynapseJavascriptClient).getV2WikiOrderHint(any(WikiPageKey.class), any(AsyncCallback.class));
		reset(mockView);
	}

	@Test
	public void testConfigureEntityBundleFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), false, null, mockActionMenuWidget);
		verify(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testConfigureFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseJavascriptClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), false, null, mockActionMenuWidget);
		verify(mockSynapseJavascriptClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testConfigureProjectRootNotFound() throws Exception {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseJavascriptClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), false, null, mockActionMenuWidget);
		verify(mockSynapseJavascriptClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView, times(2)).clear();
	}

	@Test
	public void testGetLinkPlaceSynapse() throws Exception {
		boolean embeddedInOwnerPage = true;
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), embeddedInOwnerPage, null, mockActionMenuWidget);
		String targetEntityId = "syn938";
		Long targetEntityVersion = 4L;
		String targetWikiId = "888";
		Place targetPlace = WikiSubpagesWidget.getLinkPlace(targetEntityId, targetEntityVersion, targetWikiId, embeddedInOwnerPage);
		assertTrue(targetPlace instanceof Synapse);
		Synapse targetSynapsePlace = (Synapse) targetPlace;
		assertEquals(targetEntityId, targetSynapsePlace.getEntityId());
		assertEquals(targetEntityVersion, targetSynapsePlace.getVersionNumber());
		assertEquals(Synapse.EntityArea.WIKI, targetSynapsePlace.getArea());
		assertEquals(targetWikiId, targetSynapsePlace.getAreaToken());
	}

	@Test
	public void testGetLinkPlaceWiki() throws Exception {
		boolean embeddedInOwnerPage = false;
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), embeddedInOwnerPage, null, mockActionMenuWidget);
		String targetEntityId = "syn938";
		Long targetEntityVersion = 4L;
		String targetWikiId = "888";
		Place targetPlace = WikiSubpagesWidget.getLinkPlace(targetEntityId, targetEntityVersion, targetWikiId, embeddedInOwnerPage);
		assertTrue(targetPlace instanceof Wiki);
		Wiki targetWikiPlace = (Wiki) targetPlace;
		assertEquals(targetEntityId, targetWikiPlace.getOwnerId());
		assertEquals(ObjectType.ENTITY.toString(), targetWikiPlace.getOwnerType());
		assertEquals(targetWikiId, targetWikiPlace.getWikiId());
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testEditOrderButtonVisibilityForCannotEdit() {
		permissions.setCanEdit(false);
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), false, null, mockActionMenuWidget);
		verify(mockView).setEditOrderButtonVisible(false);
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), true, null, mockActionMenuWidget);
		verify(mockView, Mockito.times(2)).setEditOrderButtonVisible(false);
		AsyncMockStubber.callFailureWith(new Throwable()).when(mockSynapseJavascriptClient).getV2WikiOrderHint(any(WikiPageKey.class), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), true, null, mockActionMenuWidget);
		verify(mockView, Mockito.times(3)).setEditOrderButtonVisible(false);
	}

	@Test
	public void testEditOrderButtonVisibilityForLogin() {
		permissions.setCanEdit(true);
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), false, null, mockActionMenuWidget);
		verify(mockView).setEditOrderButtonVisible(true);
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), true, null, mockActionMenuWidget);
		verify(mockView, Mockito.times(2)).setEditOrderButtonVisible(true);
		AsyncMockStubber.callFailureWith(new Throwable()).when(mockSynapseJavascriptClient).getV2WikiOrderHint(any(WikiPageKey.class), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), false, null, mockActionMenuWidget);
		verify(mockView, Mockito.times(3)).setEditOrderButtonVisible(true);
	}

	@Test
	public void testConfigureDifferentEntity() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), false, null, mockActionMenuWidget);
		verify(mockSynapseJavascriptClient).getEntityBundle(eq(entityId), any(EntityBundleRequest.class), any(AsyncCallback.class));

		String entityId2 = "syn9834";
		widget.configure(new WikiPageKey(entityId2, ObjectType.ENTITY.toString(), null), false, null, mockActionMenuWidget);
		verify(mockSynapseJavascriptClient).getEntityBundle(eq(entityId2), any(EntityBundleRequest.class), any(AsyncCallback.class));
	}

	@Test
	public void testConfigureSameEntityViewContainsWikiPageId() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), false, null, mockActionMenuWidget);
		verify(mockView, times(2)).clear();
		verify(mockSynapseJavascriptClient).getEntityBundle(eq(entityId), any(EntityBundleRequest.class), any(AsyncCallback.class));

		// widget has been configured for this entity id, let's reconfigure with a different page (same
		// entity)
		String wikiPageId = "123";
		when(mockView.contains(eq(wikiPageId))).thenReturn(true);
		verify(mockView).configure(anyList(), eq(projectName), any(Place.class), any(), anyBoolean(), any(CallbackP.class), any(ActionMenuWidget.class));
		// hidden initially in configure, and called again because only a single WikiPage is in the header
		// tree.
		verify(mockView, times(2)).hideSubpages();

		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), wikiPageId), false, null, mockActionMenuWidget);

		// verify using existing tree (since wiki page is in the subpages nav tree)
		verify(mockView).setPage(wikiPageId);
		// no additional calls to clear or configure the view
		verify(mockView, times(2)).clear();
		verify(mockView).configure(anyList(), eq(projectName), any(Place.class), any(), anyBoolean(), any(CallbackP.class), any(ActionMenuWidget.class));
	}

	@Test
	public void testConfigureViewContainsWikiPageIdChangeEntityName() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), false, null, mockActionMenuWidget);
		verify(mockView, times(2)).clear();
		verify(mockSynapseJavascriptClient).getEntityBundle(eq(entityId), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockView).configure(anyList(), eq(projectName), any(Place.class), any(), anyBoolean(), any(CallbackP.class), any(ActionMenuWidget.class));

		// widget has been configured for this entity id, let's reconfigure with a different page. same
		// entity, but the entity name has changed.
		String wikiPageId = "123";
		when(mockView.contains(eq(wikiPageId))).thenReturn(true);
		String newProjectName = "project name has changed";
		when(mockWikiProject.getName()).thenReturn(newProjectName);
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), wikiPageId), false, null, mockActionMenuWidget);

		// verify using existing tree (since wiki page is in the subpages nav tree)
		verify(mockView).setPage(wikiPageId);
		// additional call to clear and reconfigure the view (with the new project name)
		verify(mockView, times(3)).clear();
		verify(mockView).configure(anyList(), eq(newProjectName), any(Place.class), any(), anyBoolean(), any(CallbackP.class), any(ActionMenuWidget.class));
	}

	@Test
	public void testConfigureViewContainsWikiPageAndTreeChanged() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), false, null, mockActionMenuWidget);

		verify(mockView, times(2)).clear();
		verify(mockSynapseJavascriptClient).getEntityBundle(eq(entityId), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockView).configure(anyList(), eq(projectName), any(Place.class), any(), anyBoolean(), any(CallbackP.class), any(ActionMenuWidget.class));

		// widget has been configured for this entity id, let's reconfigure with a different page that's in
		// the view.
		// tree has changed though (different wiki headers list), so it should reconfigure.
		String wikiPageId = "123";
		when(mockView.contains(eq(wikiPageId))).thenReturn(true);
		AsyncMockStubber.callSuccessWith(Collections.singletonList(mockWikiHeader)).when(mockSynapseJavascriptClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));

		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), wikiPageId), false, null, mockActionMenuWidget);

		// verify initially using existing tree (since wiki page is in the subpages nav tree)
		verify(mockView).setPage(wikiPageId);
		// additional call to clear and reconfigure the view (with the updated wiki header tree)
		verify(mockView, times(3)).clear();
		verify(mockView, times(2)).configure(anyList(), eq(projectName), any(Place.class), any(), anyBoolean(), any(CallbackP.class), any(ActionMenuWidget.class));
	}
}
