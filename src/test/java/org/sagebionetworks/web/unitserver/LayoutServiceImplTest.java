package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Code;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.web.server.servlet.LayoutServiceImpl;
import org.sagebionetworks.web.shared.provenance.ActivityTreeNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.EntityTreeNode;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;


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
		ProvTreeNode root = new EntityTreeNode("rand123", "syn123", "root", "2", (long)2, Data.class.getName());
		ProvTreeNode activity = new ActivityTreeNode("rand456", "123", ActivityType.CODE_EXECUTION, "Normaliztion", "1", (long)1, Code.class.getName());
		root.addChild(activity);
		ProvTreeNode oldVersion = new EntityTreeNode("rand789", "syn123", "oldVersion", "1", (long)1, Data.class.getName());
		activity.addChild(oldVersion);
		
		ProvTreeNode layoutRoot = layoutService.layoutProvTree(root);
		assertTrue(layoutRoot.getxPos() > 0 || layoutRoot.getyPos() > 0);
		
		ProvTreeNode layoutAct = layoutRoot.iterator().next();
		assertTrue(layoutAct.getxPos() > 0 || layoutAct.getyPos() > 0);
		
		ProvTreeNode layoutOldVer = layoutAct.iterator().next();
		assertTrue(layoutOldVer.getxPos() > 0 || layoutOldVer.getyPos() > 0);
	}
	
}
