package org.sagebionetworks.web.unitserver;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.servlet.LayoutServiceImpl;


/**
 * Test for the SynapseClientImpl
 * @author John
 *
 */
public class LayoutServiceImplTest {

	private LayoutServiceImpl layoutService;
	
	@Before
	public void setup() {	
		layoutService = new LayoutServiceImpl();
	}
	
	@Test
	public void testLayoutProvTree() {
//		ProvTreeNode root = new EntityGraphNode("rand123", "syn123", "root", "2", (long)2, Data.class.getName());
//		ProvTreeNode activity = new ActivityGraphNode("rand456", "123", "act name", ActivityType.CODE_EXECUTION, "syn1234", "Normaliztion", "1", (long)1, Code.class.getName());
//		root.addChild(activity);
//		ProvTreeNode oldVersion = new EntityGraphNode("rand789", "syn123", "oldVersion", "1", (long)1, Data.class.getName());
//		activity.addChild(oldVersion);
//		
//		ProvTreeNode layoutRoot = layoutService.layoutProvTree(root);
//		assertTrue(layoutRoot.getxPos() > 0 || layoutRoot.getyPos() > 0);
//		
//		ProvTreeNode layoutAct = layoutRoot.iterator().next();
//		assertTrue(layoutAct.getxPos() > 0 || layoutAct.getyPos() > 0);
//		
//		ProvTreeNode layoutOldVer = layoutAct.iterator().next();
//		assertTrue(layoutOldVer.getxPos() > 0 || layoutOldVer.getyPos() > 0);
	}
	
}
