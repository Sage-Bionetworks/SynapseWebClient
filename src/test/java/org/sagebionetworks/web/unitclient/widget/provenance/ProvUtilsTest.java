package org.sagebionetworks.web.unitclient.widget.provenance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.Used;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.provenance.ProvUtils;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

public class ProvUtilsTest {

	ProvenanceWidget provenanceWidget;
	ProvenanceWidgetView mockView;
	AuthenticationController mockAuthController;
	AdapterFactory adapterFactory;
	SynapseClientAsync mockSynapseClient;

	@Before
	public void setup() {
		mockView = mock(ProvenanceWidgetView.class);
		mockAuthController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		adapterFactory = new AdapterFactoryImpl();
	}

	/*
	 * Test Graph:
	 * 
	 * expand | ent2 ent3 \ / Activity | ent1
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildProvGraph() throws Exception {
		FileEntity entity1 = new FileEntity();
		entity1.setId("syn123");
		entity1.setVersionNumber(1L);
		FileEntity entity2 = new FileEntity();
		entity2.setId("syn456");
		entity2.setVersionNumber(1L);
		FileEntity entity3 = new FileEntity();
		entity3.setId("syn456654");
		entity3.setVersionNumber(1L);
		Activity act = new Activity();
		act.setId("789");

		Reference ref1 = new Reference();
		ref1.setTargetId(entity1.getId());
		ref1.setTargetVersionNumber(entity1.getVersionNumber());
		EntityHeader header1 = new EntityHeader();
		header1.setId(ref1.getTargetId());
		header1.setVersionNumber(ref1.getTargetVersionNumber());

		Reference ref2 = new Reference();
		ref2.setTargetId(entity2.getId());
		ref2.setTargetVersionNumber(entity2.getVersionNumber());
		EntityHeader header2 = new EntityHeader();
		header2.setId(ref2.getTargetId());
		header2.setVersionNumber(ref2.getTargetVersionNumber());

		Reference ref3 = new Reference();
		ref3.setTargetId(entity3.getId());
		ref3.setTargetVersionNumber(entity3.getVersionNumber());
		EntityHeader header3 = new EntityHeader();
		header3.setId(ref3.getTargetId());
		header3.setName("Some Name");
		header3.setType(FileEntity.class.getName());
		header3.setVersionNumber(ref3.getTargetVersionNumber());

		Set<Used> used = new HashSet<Used>();
		UsedEntity ue = new UsedEntity();
		ue.setReference(ref2);
		used.add(ue);
		ue = new UsedEntity();
		ue.setReference(ref3);
		used.add(ue);
		act.setUsed(used);

		List<Activity> activities = new ArrayList<Activity>();
		activities.add(act);
		Map<String, ProvGraphNode> idToNode = new HashMap<String, ProvGraphNode>();
		Map<Reference, EntityHeader> refToHeader = new HashMap<Reference, EntityHeader>();
		refToHeader.put(ref1, header1);
		refToHeader.put(ref2, header2);
		refToHeader.put(ref3, header3);

		Map<Reference, String> generatedByActivityId = new HashMap<Reference, String>();
		generatedByActivityId.put(ref1, act.getId());

		Map<String, Activity> processedActivities = new HashMap<String, Activity>();
		processedActivities.put(act.getId(), act);

		Set<Reference> startRefs = new HashSet<Reference>();
		startRefs.add(ref1);

		Set<Reference> noExpandNodes = new HashSet<Reference>();

		ProvGraph graph = ProvUtils.buildProvGraph(generatedByActivityId, processedActivities, idToNode, refToHeader, true, startRefs, noExpandNodes);

		assertNotNull(graph.getNodes());
		assertNotNull(graph.getEdges());
		Set<ProvGraphNode> nodes = graph.getNodes();
		Set<ProvGraphEdge> edges = graph.getEdges();

		// verify all nodes created
		EntityGraphNode entity1Node = null;
		EntityGraphNode entity2Node = null;
		EntityGraphNode entity3Node = null;
		ExpandGraphNode entity3ExpandNode = null;
		ActivityGraphNode actNode = null;
		for (ProvGraphNode node : nodes) {
			if (node instanceof EntityGraphNode) {
				if (((EntityGraphNode) node).getEntityId().equals(entity1.getId()))
					entity1Node = (EntityGraphNode) node;
				if (((EntityGraphNode) node).getEntityId().equals(entity2.getId()))
					entity2Node = (EntityGraphNode) node;
				if (((EntityGraphNode) node).getEntityId().equals(entity3.getId()))
					entity3Node = (EntityGraphNode) node;
			} else if (node instanceof ActivityGraphNode) {
				if (((ActivityGraphNode) node).getActivityId().equals(act.getId()))
					actNode = (ActivityGraphNode) node;
			} else if (node instanceof ExpandGraphNode) {
				if (((ExpandGraphNode) node).getEntityId().equals(entity3.getId()))
					entity3ExpandNode = (ExpandGraphNode) node;
			}
		}
		assertNotNull(entity1Node);
		assertNotNull(entity2Node);
		assertNotNull(entity3Node);
		assertNotNull(actNode);
		assertNotNull(entity3ExpandNode);

		// verify all edges created
		ProvGraphEdge generatedByEdge = new ProvGraphEdge(entity1Node, actNode);
		assertTrue(edges.contains(generatedByEdge));
		ProvGraphEdge usedEdge = new ProvGraphEdge(actNode, entity2Node);
		assertTrue(edges.contains(usedEdge));

		// SWC-1070 regression test
		// find and verify expand nodes are not created for entities with no name (forbidden or not found
		// entities)
		assertNull(header2.getName()); // precondition
		boolean foundExpand2 = false;
		for (ProvGraphEdge edge : edges) {
			if (edge.getSource().equals(entity2Node) && edge.getSink() instanceof ExpandGraphNode) {
				foundExpand2 = true;
			}
		}
		assertFalse(foundExpand2);
	}

	@Test
	public void testCreateUniqueNodeId() throws Exception {
		Integer sequence = 0;
		Map<String, ProvGraphNode> idToNode = new HashMap<String, ProvGraphNode>();
		for (int i = 0; i < 10; i++) {
			String next = ProvUtils.createUniqueNodeId();
			assertFalse(idToNode.containsKey(next));
			idToNode.put(next, null);
		}
	}

	@Test
	public void testExtractReferences() {
		String entId1 = "syn123";
		String entId2 = "syn456";
		Activity act = new Activity();
		act.setId("789");
		Reference ref = new Reference();
		ref.setTargetId(entId1);
		Reference ref2 = new Reference();
		ref2.setTargetId(entId2);

		Set<Used> used = new HashSet<Used>();
		UsedEntity ue;
		ue = new UsedEntity();
		ue.setReference(ref);
		used.add(ue);
		ue = new UsedEntity();
		ue.setReference(ref2);
		used.add(ue);

		act.setUsed(used);

		List<Activity> activities = new ArrayList<Activity>();
		activities.add(act);

		List<Reference> refs = ProvUtils.extractReferences(activities);

		assertTrue(refs.contains(ref));
		assertTrue(refs.contains(ref2));
	}

	@Test
	public void testMapReferencesToHeaders() throws Exception {
		Reference ref = new Reference();
		ref.setTargetId("syn456");
		ref.setTargetVersionNumber(1L);
		EntityHeader header = new EntityHeader();
		header.setId(ref.getTargetId());
		header.setVersionNumber(ref.getTargetVersionNumber());
		UsedEntity ue = new UsedEntity();
		ue.setReference(ref);
		ArrayList<EntityHeader> referenceHeaders = new ArrayList<EntityHeader>(Arrays.asList(new EntityHeader[] {header}));

		Map<Reference, EntityHeader> refToHeader = ProvUtils.mapReferencesToHeaders(referenceHeaders);
		assertTrue(refToHeader.containsKey(ref));
		assertEquals(header, refToHeader.get(ref));
	}

}
