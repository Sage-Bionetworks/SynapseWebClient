package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree.SubpageOrderEditorTreeNode;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTreeView;

public class WikiSubpageOrderEditorTreeTest {
	
	WikiSubpageOrderEditorTree tree;
	
	WikiSubpageOrderEditorTreeView mockView;
	
	List<JSONEntity> wikiHeaders;
	String ownerObjectName;
	
	@Before
	public void before() {
		mockView = Mockito.mock(WikiSubpageOrderEditorTreeView.class);
		
		tree = new WikiSubpageOrderEditorTree(mockView);
		
		wikiHeaders = new ArrayList<JSONEntity>();
		
		// Set up headers for tree:
		//	A
		//		B
		//			C
		//		D
		//		E
		
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
		d.setTitle("D");
		d.setParentId("0");
		
		e.setId("4");
		e.setTitle("E");
		e.setParentId("0");
		
	}
	
	@Test
	public void testConfigure() {
		tree.configure(wikiHeaders, ownerObjectName);
		
		SubpageOrderEditorTreeNode aNode = tree.getOverallRoot();
		SubpageOrderEditorTreeNode bNode = aNode.getChildren().get(0);
		SubpageOrderEditorTreeNode cNode = bNode.getChildren().get(0);
		SubpageOrderEditorTreeNode dNode = aNode.getChildren().get(1);
		SubpageOrderEditorTreeNode eNode = aNode.getChildren().get(2);
		
		assertTrue(aNode.getHeader().equals(wikiHeaders.get(0)));	// A
		assertTrue(aNode.getChildren().size() == 3);
		assertTrue(aNode.getText().equals(ownerObjectName));
		
		assertTrue(bNode.getHeader().equals(wikiHeaders.get(1)));	// B
		assertTrue(bNode.getChildren().size() == 1);
		assertTrue(bNode.getText().equals("B"));
		
		assertTrue(cNode.getHeader().equals(wikiHeaders.get(2)));	// C
		assertTrue(cNode.getChildren().isEmpty());
		assertTrue(cNode.getText().equals("C"));
		
		assertTrue(dNode.getHeader().equals(wikiHeaders.get(3)));	// D
		assertTrue(dNode.getChildren().isEmpty());
		assertTrue(dNode.getText().equals("D"));
		
		assertTrue(eNode.getHeader().equals(wikiHeaders.get(4)));	// E
		assertTrue(eNode.getChildren().isEmpty());
		assertTrue(eNode.getText().equals("E"));
	}
	
	@Test
	public void testGetOrderHint() {
		tree.configure(wikiHeaders, ownerObjectName);
		
		List<String> orderHint = tree.getIdListOrderHint();
		
		assertTrue(orderHint.indexOf("1") < orderHint.indexOf("3"));	// B < D
		assertTrue(orderHint.indexOf("3") < orderHint.indexOf("4"));	// D < E
	}
	
	@Test
	public void testSelectTreeItem() {
		tree.configure(wikiHeaders, ownerObjectName);
		
		// Select some tree item (C).
		SubpageOrderEditorTreeNode c = tree.getOverallRoot().getChildren().get(0).getChildren().get(0);
		assertTrue(c.getText().equals("C"));
		tree.selectTreeItem(c);
		
		verify(mockView).selectTreeItem(c);
	}
	
	@Test
	public void testMoveSelectedItemUp() {
		tree.configure(wikiHeaders, ownerObjectName);
		
		SubpageOrderEditorTreeNode e = tree.getOverallRoot().getChildren().get(2);
		assertTrue(e.getText().equals("E"));
		tree.selectTreeItem(e);
		
		// Swap E up with D.
		tree.moveSelectedItem(true);
		
		List<String> orderHint = tree.getIdListOrderHint();
		
		assertTrue(orderHint.indexOf("4") < orderHint.indexOf("3"));	// E < D
		
		// Swap E up with B.
		tree.moveSelectedItem(true);
		
		orderHint = tree.getIdListOrderHint();
		
		assertTrue(orderHint.indexOf("4") < orderHint.indexOf("1"));	// E < B
		
		verify(mockView, times(2)).moveTreeItem(e, true);
	}
	
	@Test
	public void testMoveSelectedItemDown() {
		tree.configure(wikiHeaders, ownerObjectName);
		
		SubpageOrderEditorTreeNode b = tree.getOverallRoot().getChildren().get(0);
		assertTrue(b.getText().equals("B"));
		tree.selectTreeItem(b);
		
		// Swap B down with D.
		tree.moveSelectedItem(false);
		
		List<String> orderHint = tree.getIdListOrderHint();
		
		assertTrue(orderHint.indexOf("1") > orderHint.indexOf("3"));	// B > D
		
		// Swap B down with E.
		tree.moveSelectedItem(false);
		
		orderHint = tree.getIdListOrderHint();
		
		assertTrue(orderHint.indexOf("1") > orderHint.indexOf("4"));	// B > E
		
		verify(mockView, times(2)).moveTreeItem(b, false);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testMoveSelectedItemUpIllegalState() {
		tree.configure(wikiHeaders, ownerObjectName);
		
		SubpageOrderEditorTreeNode b = tree.getOverallRoot().getChildren().get(0);
		assertTrue(b.getText().equals("B"));
		tree.selectTreeItem(b);
		
		// Try to move B up.
		tree.moveSelectedItem(true);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testMoveSelectedItemDownIllegalState() {
		tree.configure(wikiHeaders, ownerObjectName);
		
		SubpageOrderEditorTreeNode e = tree.getOverallRoot().getChildren().get(2);
		tree.selectTreeItem(e);
		
		// Try to move E down.
		tree.moveSelectedItem(false);
	}
	
	@Test
	public void testGetParent() {
		tree.configure(wikiHeaders, ownerObjectName);
		
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
