package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree.MOVE_DOWN_ERROR;
import static org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree.MOVE_LEFT_ERROR;
import static org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree.MOVE_RIGHT_ERROR;
import static org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree.MOVE_UP_ERROR;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree.SubpageOrderEditorTreeNode;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTreeView;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class WikiSubpageOrderEditorTreeTest {

	WikiSubpageOrderEditorTree tree;

	@Mock
	WikiSubpageOrderEditorTreeView mockView;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	WikiPageKey mockWikiPageKey;
	@Mock
	V2WikiOrderHint mockV2WikiOrderHint;
	@Mock
	CallbackP mockCallback;
	@Mock
	V2WikiPage mockV2WikiPage;
	List<V2WikiHeader> wikiHeaders;
	String ownerObjectName;
	V2WikiHeader a, b, c, d, e;
	@Captor
	ArgumentCaptor<WikiPageKey> wikiPageKeyCaptor;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);

		tree = new WikiSubpageOrderEditorTree(mockView, mockSynAlert, mockSynapseJavascriptClient);

		wikiHeaders = new ArrayList<V2WikiHeader>();

		// Set up headers for tree:
		// A
		// B
		// C
		// D
		// E

		a = new V2WikiHeader();
		b = new V2WikiHeader();
		c = new V2WikiHeader();
		d = new V2WikiHeader();
		e = new V2WikiHeader();
		wikiHeaders.add(a);
		wikiHeaders.add(b);
		wikiHeaders.add(c);
		wikiHeaders.add(d);
		wikiHeaders.add(e);

		a.setId("0");
		a.setTitle("Root");
		ownerObjectName = "A";

		b.setId("1");
		b.setTitle("B");
		b.setParentId("0");

		c.setId("2");
		c.setTitle("C");
		c.setParentId("1");

		d.setId("3");
		d.setTitle("D");
		d.setParentId("0");

		e.setId("4");
		e.setTitle("E");
		e.setParentId("0");
		String selectWikiPageId = d.getId();
		tree.configure(selectWikiPageId, mockWikiPageKey, wikiHeaders, ownerObjectName, mockV2WikiOrderHint, mockCallback);

		AsyncMockStubber.callSuccessWith(mockV2WikiOrderHint).when(mockSynapseJavascriptClient).updateV2WikiOrderHint(any(WikiPageKey.class), any(V2WikiOrderHint.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockV2WikiPage).when(mockSynapseJavascriptClient).updateV2WikiPage(any(WikiPageKey.class), any(V2WikiPage.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockV2WikiPage).when(mockSynapseJavascriptClient).getV2WikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
	}

	@Test
	public void testConfigure() {
		// verify tree structure
		SubpageOrderEditorTreeNode aNode = tree.getOverallRoot();
		SubpageOrderEditorTreeNode bNode = aNode.getChildren().get(0);
		SubpageOrderEditorTreeNode cNode = bNode.getChildren().get(0);
		SubpageOrderEditorTreeNode dNode = aNode.getChildren().get(1);
		SubpageOrderEditorTreeNode eNode = aNode.getChildren().get(2);

		assertTrue(aNode.getHeader().equals(wikiHeaders.get(0))); // A
		assertTrue(aNode.getChildren().size() == 3);
		assertTrue(aNode.getText().equals(ownerObjectName));

		assertTrue(bNode.getHeader().equals(wikiHeaders.get(1))); // B
		assertTrue(bNode.getChildren().size() == 1);
		assertTrue(bNode.getText().equals("B"));

		assertTrue(cNode.getHeader().equals(wikiHeaders.get(2))); // C
		assertTrue(cNode.getChildren().isEmpty());
		assertTrue(cNode.getText().equals("C"));

		assertTrue(dNode.getHeader().equals(wikiHeaders.get(3))); // D
		assertTrue(dNode.getChildren().isEmpty());
		assertTrue(dNode.getText().equals("D"));

		assertTrue(eNode.getHeader().equals(wikiHeaders.get(4))); // E
		assertTrue(eNode.getChildren().isEmpty());
		assertTrue(eNode.getText().equals("E"));

		// verify selection
		assertEquals(dNode, tree.getSelectedTreeItem());
		verify(mockView).selectTreeItem(dNode);
	}

	@Test
	public void testGetOrderHint() {
		List<String> orderHint = tree.getIdListOrderHint();

		assertTrue(orderHint.indexOf("1") < orderHint.indexOf("3")); // B < D
		assertTrue(orderHint.indexOf("3") < orderHint.indexOf("4")); // D < E
	}

	@Test
	public void testSelectTreeItem() {
		// Select some tree item (C).
		SubpageOrderEditorTreeNode c = tree.getOverallRoot().getChildren().get(0).getChildren().get(0);
		assertTrue(c.getText().equals("C"));
		tree.selectTreeItem(c);

		verify(mockView).selectTreeItem(c);
	}

	@Test
	public void testMoveSelectedItemUp() {
		SubpageOrderEditorTreeNode e = tree.getOverallRoot().getChildren().get(2);
		assertTrue(e.getText().equals("E"));
		tree.selectTreeItem(e);

		// Swap E up with D.
		tree.moveSelectedItem(true);

		List<String> orderHint = tree.getIdListOrderHint();
		// verify hint id list has been updated to the new list
		verify(mockV2WikiOrderHint).setIdList(orderHint);
		verify(mockSynapseJavascriptClient).updateV2WikiOrderHint(any(WikiPageKey.class), any(V2WikiOrderHint.class), any(AsyncCallback.class));

		assertTrue(orderHint.indexOf("4") < orderHint.indexOf("3")); // E < D

		// Swap E up with B.
		tree.moveSelectedItem(true);

		orderHint = tree.getIdListOrderHint();

		assertTrue(orderHint.indexOf("4") < orderHint.indexOf("1")); // E < B

		verify(mockView, times(2)).moveTreeItem(e, true);

	}

	@Test
	public void testMoveSelectedItemDown() {
		SubpageOrderEditorTreeNode b = tree.getOverallRoot().getChildren().get(0);
		assertTrue(b.getText().equals("B"));
		tree.selectTreeItem(b);

		// Swap B down with D.
		tree.moveSelectedItem(false);

		List<String> orderHint = tree.getIdListOrderHint();
		// verify hint id list has been updated to the new list
		verify(mockV2WikiOrderHint).setIdList(orderHint);
		verify(mockSynapseJavascriptClient).updateV2WikiOrderHint(any(WikiPageKey.class), any(V2WikiOrderHint.class), any(AsyncCallback.class));

		assertTrue(orderHint.indexOf("1") > orderHint.indexOf("3")); // B > D

		// Swap B down with E.
		tree.moveSelectedItem(false);

		orderHint = tree.getIdListOrderHint();

		assertTrue(orderHint.indexOf("1") > orderHint.indexOf("4")); // B > E

		verify(mockView, times(2)).moveTreeItem(b, false);
	}

	@Test
	public void testMoveSelectedItemLeft() {
		// move C node left (change parent from B to A)
		SubpageOrderEditorTreeNode aNode = tree.getOverallRoot();
		SubpageOrderEditorTreeNode bNode = aNode.getChildren().get(0);
		SubpageOrderEditorTreeNode cNode = bNode.getChildren().get(0);
		tree.selectTreeItem(cNode);
		when(mockV2WikiPage.getId()).thenReturn(c.getId());

		tree.moveLeft();

		// verify we get C node wiki page, update it's parent to A, and attempt to update.
		verify(mockSynapseJavascriptClient).getV2WikiPage(wikiPageKeyCaptor.capture(), any(AsyncCallback.class));
		assertEquals(c.getId(), wikiPageKeyCaptor.getValue().getWikiPageId());
		verify(mockV2WikiPage).setParentWikiId(a.getId());
		verify(mockSynapseJavascriptClient).updateV2WikiPage(any(WikiPageKey.class), any(V2WikiPage.class), any(AsyncCallback.class));
		// On success, invoke the refresh callback with the selected wiki page ID
		verify(mockCallback).invoke(c.getId());
	}

	@Test
	public void testMoveSelectedItemRight() {
		// move D node right (change parent from A to B)
		SubpageOrderEditorTreeNode aNode = tree.getOverallRoot();
		SubpageOrderEditorTreeNode dNode = aNode.getChildren().get(1);
		tree.selectTreeItem(dNode);
		when(mockV2WikiPage.getId()).thenReturn(d.getId());

		tree.moveRight();

		// verify we get D node wiki page, update it's parent to B, and attempt to update.
		verify(mockSynapseJavascriptClient).getV2WikiPage(wikiPageKeyCaptor.capture(), any(AsyncCallback.class));
		assertEquals(d.getId(), wikiPageKeyCaptor.getValue().getWikiPageId());
		verify(mockV2WikiPage).setParentWikiId(b.getId());
		verify(mockSynapseJavascriptClient).updateV2WikiPage(any(WikiPageKey.class), any(V2WikiPage.class), any(AsyncCallback.class));
		// On success, invoke the refresh callback with the selected wiki page ID
		verify(mockCallback).invoke(d.getId());
	}


	@Test
	public void testMoveSelectedItemUpIllegalState() {
		SubpageOrderEditorTreeNode b = tree.getOverallRoot().getChildren().get(0);
		assertTrue(b.getText().equals("B"));
		tree.selectTreeItem(b);

		// Try to move B up.
		tree.moveUp();
		verify(mockSynAlert).showError(MOVE_UP_ERROR);
	}

	@Test
	public void testMoveSelectedItemRightIllegalState() {
		SubpageOrderEditorTreeNode b = tree.getOverallRoot().getChildren().get(0);
		assertTrue(b.getText().equals("B"));
		tree.selectTreeItem(b);

		// Try to move B right.
		tree.moveRight();
		verify(mockSynAlert).showError(MOVE_RIGHT_ERROR);
	}

	@Test
	public void testMoveSelectedItemLeftIllegalState() {
		SubpageOrderEditorTreeNode e = tree.getOverallRoot().getChildren().get(2);
		tree.selectTreeItem(e);

		// Try to move E left (parent is root, so this should not be possible).
		tree.moveLeft();
		verify(mockSynAlert).showError(MOVE_LEFT_ERROR);
	}

	@Test
	public void testMoveSelectedItemDownIllegalState() {
		SubpageOrderEditorTreeNode e = tree.getOverallRoot().getChildren().get(2);
		tree.selectTreeItem(e);

		// Try to move E down.
		tree.moveDown();
		verify(mockSynAlert).showError(MOVE_DOWN_ERROR);
	}

	@Test
	public void testGetParent() {
		// Overall root has no parent.
		assertNull(tree.getParent(tree.getOverallRoot()));

		SubpageOrderEditorTreeNode c = tree.getOverallRoot().getChildren().get(0).getChildren().get(0);
		assertTrue(c.getText().equals("C"));

		SubpageOrderEditorTreeNode b = tree.getOverallRoot().getChildren().get(0);
		assertTrue(b.getText().equals("B"));

		SubpageOrderEditorTreeNode cParent = tree.getParent(c);

		assertTrue(cParent.equals(b));
	}

}
