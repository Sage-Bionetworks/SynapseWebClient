package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTree;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTree.SubpageNavTreeNode;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTreeView;
import org.sagebionetworks.web.shared.WikiPageKey;

public class WikiSubpageNavigationTreeTest {
	WikiSubpageNavigationTree tree;
	@Mock
	WikiSubpageNavigationTreeView mockView;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	CallbackP<WikiPageKey> mockReloadWikiPageCallback;
	@Captor
	ArgumentCaptor<WikiPageKey> pageKeyCaptor;
	List<V2WikiHeader> wikiHeaders;
	String ownerObjectName;
	WikiPageKey currentWikiKey;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		tree = new WikiSubpageNavigationTree(mockView, mockGlobalApplicationState);

		wikiHeaders = new ArrayList<V2WikiHeader>();

		// Set up headers for tree:
		// A
		// B
		// C
		// D
		// E

		V2WikiHeader a = new V2WikiHeader();
		V2WikiHeader b = new V2WikiHeader();
		V2WikiHeader c = new V2WikiHeader();
		V2WikiHeader d = new V2WikiHeader();
		V2WikiHeader e = new V2WikiHeader();
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
		d.setTitle(null);
		d.setParentId("0");

		e.setId("4");
		e.setTitle("");
		e.setParentId("0");

		currentWikiKey = new WikiPageKey();
		currentWikiKey.setWikiPageId("3"); // On page D
		currentWikiKey.setOwnerObjectId("3");
		currentWikiKey.setVersion(new Long(1));
		currentWikiKey.setOwnerObjectType(ObjectType.ENTITY.name());
	}

	@Test
	public void testConfigure() {
		Synapse rootPlace = new Synapse("");
		tree.configure(wikiHeaders, ownerObjectName, rootPlace, currentWikiKey, true, mockReloadWikiPageCallback);
		SubpageNavTreeNode aNode = tree.getOverallRoot();
		SubpageNavTreeNode bNode = aNode.getChildren().get(0);
		SubpageNavTreeNode cNode = bNode.getChildren().get(0);
		SubpageNavTreeNode dNode = aNode.getChildren().get(1);
		SubpageNavTreeNode eNode = aNode.getChildren().get(2);

		testTreeStructure(aNode, bNode, cNode, dNode, eNode);

		WikiPageKey aWikiPageKey = new WikiPageKey(currentWikiKey.getOwnerObjectId(), currentWikiKey.getOwnerObjectType(), "0", currentWikiKey.getVersion());
		WikiPageKey bWikiPageKey = new WikiPageKey(currentWikiKey.getOwnerObjectId(), currentWikiKey.getOwnerObjectType(), "1", currentWikiKey.getVersion());
		WikiPageKey cWikiPageKey = new WikiPageKey(currentWikiKey.getOwnerObjectId(), currentWikiKey.getOwnerObjectType(), "2", currentWikiKey.getVersion());
		WikiPageKey dWikiPageKey = new WikiPageKey(currentWikiKey.getOwnerObjectId(), currentWikiKey.getOwnerObjectType(), "3", currentWikiKey.getVersion());
		WikiPageKey eWikiPageKey = new WikiPageKey(currentWikiKey.getOwnerObjectId(), currentWikiKey.getOwnerObjectType(), "4", currentWikiKey.getVersion());

		assertEquals(Arrays.asList(aWikiPageKey, bWikiPageKey, cWikiPageKey, dWikiPageKey, eWikiPageKey), Arrays.asList(aNode.getWikiPageKey(), bNode.getWikiPageKey(), cNode.getWikiPageKey(), dNode.getWikiPageKey(), eNode.getWikiPageKey()));

		// on page D by default, should expand all
		assertFalse(aNode.isCollapsed());
		assertFalse(bNode.isCollapsed());
	}

	@Test
	public void testConfigureIsEmbedded() {
		tree.configure(wikiHeaders, ownerObjectName, new Synapse(""), currentWikiKey, true, mockReloadWikiPageCallback);

		SubpageNavTreeNode aNode = tree.getOverallRoot();
		SubpageNavTreeNode bNode = aNode.getChildren().get(0);
		SubpageNavTreeNode cNode = bNode.getChildren().get(0);
		SubpageNavTreeNode dNode = aNode.getChildren().get(1);
		SubpageNavTreeNode eNode = aNode.getChildren().get(2);

		testInstanceOfSynapse(Arrays.asList(aNode, bNode, cNode, dNode, eNode));
	}

	@Test
	public void testConfigureNotaEmbedded() {
		tree.configure(wikiHeaders, ownerObjectName, new Wiki(""), currentWikiKey, false, mockReloadWikiPageCallback);

		SubpageNavTreeNode aNode = tree.getOverallRoot();
		SubpageNavTreeNode bNode = aNode.getChildren().get(0);
		SubpageNavTreeNode cNode = bNode.getChildren().get(0);
		SubpageNavTreeNode dNode = aNode.getChildren().get(1);
		SubpageNavTreeNode eNode = aNode.getChildren().get(2);

		testInstanceOfWiki(Arrays.asList(aNode, bNode, cNode, dNode, eNode));
	}

	@Test
	public void testIsCurrentPage() {
		tree.configure(wikiHeaders, ownerObjectName, new Wiki(""), currentWikiKey, false, mockReloadWikiPageCallback);

		SubpageNavTreeNode aNode = tree.getOverallRoot();
		SubpageNavTreeNode bNode = aNode.getChildren().get(0);
		SubpageNavTreeNode cNode = bNode.getChildren().get(0);
		SubpageNavTreeNode dNode = aNode.getChildren().get(1);
		SubpageNavTreeNode eNode = aNode.getChildren().get(2);

		assertTrue(tree.isCurrentPage(dNode));
		assertFalse(tree.isCurrentPage(aNode));
		assertFalse(tree.isCurrentPage(bNode));
		assertFalse(tree.isCurrentPage(cNode));
		assertFalse(tree.isCurrentPage(eNode));
	}

	@Test
	public void testRootCurrentPage() {
		// set root to be the currently selected node
		currentWikiKey.setWikiPageId("0");
		tree.configure(wikiHeaders, ownerObjectName, new Wiki(""), currentWikiKey, false, mockReloadWikiPageCallback);

		SubpageNavTreeNode aNode = tree.getOverallRoot();
		SubpageNavTreeNode bNode = aNode.getChildren().get(0);

		// verify other parent nodes are collapsed, but root is expanded
		assertFalse(aNode.isCollapsed());
		assertTrue(bNode.isCollapsed());
	}

	@Test
	public void testReloadWiki() {
		tree.configure(wikiHeaders, ownerObjectName, new Wiki(""), currentWikiKey, false, mockReloadWikiPageCallback);
		reset(mockGlobalApplicationState);
		SubpageNavTreeNode aNode = tree.getOverallRoot();
		SubpageNavTreeNode bNode = aNode.getChildren().get(0);
		SubpageNavTreeNode cNode = bNode.getChildren().get(0);
		SubpageNavTreeNode dNode = aNode.getChildren().get(1);
		SubpageNavTreeNode eNode = aNode.getChildren().get(2);

		verify(mockView, times(1)).configure(aNode);

		testReloadWikiForNode(aNode, aNode, 1);
		testReloadWikiForNode(bNode, aNode, 2);
		testReloadWikiForNode(cNode, aNode, 3);
		testReloadWikiForNode(dNode, aNode, 4);
		testReloadWikiForNode(eNode, aNode, 5);
	}

	private void testReloadWikiForNode(SubpageNavTreeNode aNode, SubpageNavTreeNode root, int time) {
		tree.reloadWiki(aNode);
		verify(mockReloadWikiPageCallback).invoke(aNode.getWikiPageKey());
		// pushCurrentPlace called once from configure, and once from reload
		verify(mockGlobalApplicationState).pushCurrentPlace(aNode.getTargetPlace());
		verify(mockView, Mockito.times(time)).resetNavTree(root);
	}

	private void testInstanceOfSynapse(List<SubpageNavTreeNode> nodes) {
		for (SubpageNavTreeNode node : nodes) {
			assertTrue(node.getTargetPlace() instanceof Synapse);
		}
	}

	private void testInstanceOfWiki(List<SubpageNavTreeNode> nodes) {
		for (SubpageNavTreeNode node : nodes) {
			assertTrue(node.getTargetPlace() instanceof Wiki);
		}
	}

	private void testTreeStructure(SubpageNavTreeNode aNode, SubpageNavTreeNode bNode, SubpageNavTreeNode cNode, SubpageNavTreeNode dNode, SubpageNavTreeNode eNode) {
		assertTrue(aNode.getChildren().size() == 3);
		assertTrue(aNode.getPageTitle().equals(ownerObjectName));

		assertTrue(bNode.getChildren().size() == 1);
		assertTrue(bNode.getPageTitle().equals("B"));

		assertTrue(cNode.getChildren().isEmpty());
		assertTrue(cNode.getPageTitle().equals("C"));

		assertTrue(dNode.getChildren().isEmpty());
		// D has a null title, should use it's ID
		assertTrue(dNode.getPageTitle().equals("3"));

		assertTrue(eNode.getChildren().isEmpty());
		// E has an empty string for a title, should use it's ID
		assertTrue(eNode.getPageTitle().equals("4"));
	}

	@Test
	public void testContains() {
		tree.configure(wikiHeaders, ownerObjectName, new Wiki(""), currentWikiKey, false, mockReloadWikiPageCallback);

		assertTrue(tree.contains(wikiHeaders.get(0).getId()));
		assertTrue(tree.contains(wikiHeaders.get(1).getId()));
		assertFalse(tree.contains("777"));
	}

	@Test
	public void testSetPage() {
		// tree contains no pages
		String pageId = wikiHeaders.get(0).getId();
		tree.setPage(pageId);
		verify(mockReloadWikiPageCallback, never()).invoke(any(WikiPageKey.class));

		// tree contains wiki headers
		tree.configure(wikiHeaders, ownerObjectName, new Wiki(""), currentWikiKey, false, mockReloadWikiPageCallback);
		tree.setPage(pageId);
		verify(mockReloadWikiPageCallback).invoke(pageKeyCaptor.capture());
		assertEquals(pageId, pageKeyCaptor.getValue().getWikiPageId());
	}
}
