package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.DELETED;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.THE;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.WAS_SUCCESSFULLY_DELETED;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.WIKI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.WikiPageDeleteConfirmationDialog;
import org.sagebionetworks.web.client.widget.entity.WikiPageDeleteConfirmationDialogView;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class WikiPageDeleteConfirmationDialogTest {
	private static final String WIKI_TREE_PAGE_A2_TITLE = "title(A2)";
	private static final String WIKI_TREE_PAGE_A2_ID = "A2";
	private static final String WIKI_TREE_PAGE_A1_TITLE = "title(A1)";
	private static final String WIKI_TREE_PAGE_A1_ID = "A1";
	private static final String WIKI_TREE_PAGE_A_TITLE = "title(A)";
	private static final String WIKI_TREE_PAGE_A_ID = "A";
	private static final String WIKI_TREE_ROOT_ID = "root";

	String wikiPageTitle="To delete, or not to delete.";
	
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	WikiPageDeleteConfirmationDialogView mockView;
	@Mock
	WikiPageKey mockWikiPageKey;
	@Mock
	CallbackP<String> mockAfterDeleteCallback;
	WikiPageDeleteConfirmationDialog dialog;
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		AsyncMockStubber.callSuccessWith(getWikiHeaderTree()).when(mockSynapseClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		when(mockWikiPageKey.getWikiPageId()).thenReturn(WIKI_TREE_PAGE_A2_ID);
		dialog = new WikiPageDeleteConfirmationDialog(mockView, mockSynapseClient);
	}
	
	/**
	 * Sets up the wiki header tree to have a root page (id=root) that has one child page (id=A), which has 2 children (id=A1 and id=A2). 
	 * @return
	 */
	private List<V2WikiHeader> getWikiHeaderTree() {
		List<V2WikiHeader> headers = new ArrayList<>();
		V2WikiHeader page = new V2WikiHeader();
		page.setId(WIKI_TREE_ROOT_ID);
		headers.add(page);
		page = new V2WikiHeader();
		page.setId(WIKI_TREE_PAGE_A_ID);
		page.setTitle(WIKI_TREE_PAGE_A_TITLE);
		page.setParentId(WIKI_TREE_ROOT_ID);
		headers.add(page);
		page = new V2WikiHeader();
		page.setId(WIKI_TREE_PAGE_A1_ID);
		page.setTitle(WIKI_TREE_PAGE_A1_TITLE);
		page.setParentId(WIKI_TREE_PAGE_A_ID);
		headers.add(page);
		page = new V2WikiHeader();
		page.setId(WIKI_TREE_PAGE_A2_ID);
		page.setTitle(WIKI_TREE_PAGE_A2_TITLE);
		page.setParentId(WIKI_TREE_PAGE_A_ID);
		headers.add(page);
		return headers;
	}
	
	@Test
	public void testGetWikiHeaderMap() {
		// set up the entity name to verify that the root wiki header title has been updated
		Map<String, V2WikiHeader> headerMap = dialog.getWikiHeaderMap(getWikiHeaderTree());
		
		V2WikiHeader page = headerMap.get(WIKI_TREE_ROOT_ID);
		assertNull(page.getParentId());
		assertEquals(WikiPageDeleteConfirmationDialog.ROOT_WIKI_PAGE_NAME, page.getTitle());
		
		page = headerMap.get(WIKI_TREE_PAGE_A_ID);
		assertEquals(WIKI_TREE_ROOT_ID, page.getParentId());
		assertEquals(WIKI_TREE_PAGE_A_TITLE, page.getTitle());
		
		page = headerMap.get(WIKI_TREE_PAGE_A1_ID);
		assertEquals(WIKI_TREE_PAGE_A_ID, page.getParentId());
		assertEquals(WIKI_TREE_PAGE_A1_TITLE, page.getTitle());
		
		page = headerMap.get(WIKI_TREE_PAGE_A2_ID);
		assertEquals(WIKI_TREE_PAGE_A_ID, page.getParentId());
		assertEquals(WIKI_TREE_PAGE_A2_TITLE, page.getTitle());
	}
	

	@Test
	public void testGetWikiChildrenMap() {
		Map<String, List<V2WikiHeader>> childrenMap = dialog.getWikiChildrenMap(getWikiHeaderTree());
		
		List<V2WikiHeader> children = childrenMap.get(WIKI_TREE_ROOT_ID);
		assertTrue(children.size() == 1 && children.get(0).getId().equals(WIKI_TREE_PAGE_A_ID));
		
		children = childrenMap.get(WIKI_TREE_PAGE_A_ID);
		assertTrue(children.size() == 2);
		assertTrue(children.get(0).getId().equals(WIKI_TREE_PAGE_A1_ID) || children.get(0).getId().equals(WIKI_TREE_PAGE_A2_ID));
		
		assertFalse(childrenMap.containsKey(WIKI_TREE_PAGE_A1_ID));
		assertFalse(childrenMap.containsKey(WIKI_TREE_PAGE_A2_ID));
	}
	
	@Test
	public void testOnDeleteWiki(){
		// let's simulate that this is the root wiki.
		when(mockWikiPageKey.getWikiPageId()).thenReturn(WIKI_TREE_ROOT_ID);
		// the call under tests
		dialog.show(mockWikiPageKey, mockAfterDeleteCallback);
		
		verify(mockSynapseClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showModal(eq(WIKI_TREE_ROOT_ID), any(Map.class), any(Map.class));

		// should not make it to the delete wiki page call
		verify(mockSynapseClient, never()).deleteV2WikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testOnDeleteWikiPageConfirmedDeleteFailed(){
		String error = "some error";
		AsyncMockStubber.callFailureWith(new Throwable(error)).when(mockSynapseClient).deleteV2WikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
		dialog.show(mockWikiPageKey, mockAfterDeleteCallback);
		
		// did not make it to the delete wiki page call
		verify(mockSynapseClient, never()).deleteV2WikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
		
		dialog.onDeleteWiki();
		
		verify(mockSynapseClient).deleteV2WikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(error);
	}
	
	@Test
	public void testOnDeleteWikiPageConfirmedDeleteSuccess(){
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteV2WikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
		dialog.show(mockWikiPageKey, mockAfterDeleteCallback);
		// did not make it to the delete wiki page call
		verify(mockSynapseClient, never()).deleteV2WikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
		
		dialog.onDeleteWiki();
		
		verify(mockSynapseClient).deleteV2WikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockView).showInfo(THE + WIKI + WAS_SUCCESSFULLY_DELETED);
		verify(mockAfterDeleteCallback).invoke(WIKI_TREE_PAGE_A_ID);
	}
}
