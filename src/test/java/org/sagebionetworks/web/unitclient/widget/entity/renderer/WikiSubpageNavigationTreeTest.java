package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTree;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTree.SubpageNavTreeNode;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTreeView;
import org.sagebionetworks.web.shared.WikiPageKey;

public class WikiSubpageNavigationTreeTest {
	WikiSubpageNavigationTree tree;
	
	WikiSubpageNavigationTreeView mockView;
	
	List<JSONEntity> wikiHeaders;
	String ownerObjectName;
	WikiPageKey curWikiKey;
	
	
	
	@Before
	public void before() {
		mockView = Mockito.mock(WikiSubpageNavigationTreeView.class);
		
		tree = new WikiSubpageNavigationTree(mockView);
		
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
		
		curWikiKey = new WikiPageKey();
		curWikiKey.setWikiPageId("3");	// On page D
		curWikiKey.setOwnerObjectId("3");
		curWikiKey.setVersion(new Long(1));
		curWikiKey.setOwnerObjectType(ObjectType.ENTITY.name());
		
	}
	
	@Test
	public void testConfigureIsEmbedded() {
		tree.configure(wikiHeaders, ownerObjectName, new Synapse(""), curWikiKey, true);
		
		SubpageNavTreeNode aNode = tree.getOverallRoot();
		SubpageNavTreeNode bNode = aNode.getChildren().get(0);
		SubpageNavTreeNode cNode = bNode.getChildren().get(0);
		SubpageNavTreeNode dNode = aNode.getChildren().get(1);
		SubpageNavTreeNode eNode = aNode.getChildren().get(2);
		
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
		
		assertTrue(aNode.getTargetPlace() instanceof Synapse);
		assertTrue(bNode.getTargetPlace() instanceof Synapse);
		assertTrue(cNode.getTargetPlace() instanceof Synapse);
		assertTrue(dNode.getTargetPlace() instanceof Synapse);
		assertTrue(eNode.getTargetPlace() instanceof Synapse);
	}
	
	@Test
	public void testConfigureNotaEmbedded() {
		tree.configure(wikiHeaders, ownerObjectName, new Wiki(""), curWikiKey, false);
		
		SubpageNavTreeNode aNode = tree.getOverallRoot();
		SubpageNavTreeNode bNode = aNode.getChildren().get(0);
		SubpageNavTreeNode cNode = bNode.getChildren().get(0);
		SubpageNavTreeNode dNode = aNode.getChildren().get(1);
		SubpageNavTreeNode eNode = aNode.getChildren().get(2);
		
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
		
		assertTrue(aNode.getTargetPlace() instanceof Wiki);
		assertTrue(bNode.getTargetPlace() instanceof Wiki);
		assertTrue(cNode.getTargetPlace() instanceof Wiki);
		assertTrue(dNode.getTargetPlace() instanceof Wiki);
		assertTrue(eNode.getTargetPlace() instanceof Wiki);
	}
}
