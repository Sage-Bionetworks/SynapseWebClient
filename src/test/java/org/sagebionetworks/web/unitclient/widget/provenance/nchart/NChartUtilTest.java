package org.sagebionetworks.web.unitclient.widget.provenance.nchart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayer;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayerNode;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartUtil;
import org.sagebionetworks.web.client.widget.provenance.nchart.XYPoint;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

public class NChartUtilTest {

	// JsoProvider jsoProvider = new JsoProviderJavaImpl();
	JsoProvider jsoProvider;

	EntityGraphNode d1;
	EntityGraphNode d2;
	EntityGraphNode d3;
	EntityGraphNode d4;
	EntityGraphNode d5;
	EntityGraphNode d6;
	ActivityGraphNode a;
	ActivityGraphNode b;
	ActivityGraphNode c;
	ActivityGraphNode d;
	final static int DEFAULT_DURATION = 10;

	@Before
	public void setup() {
		jsoProvider = mock(JsoProvider.class);
		d1 = new EntityGraphNode("d1", null, null, null, null, null, false, false);
		d2 = new EntityGraphNode("d2", null, null, null, null, null, false, false);
		d3 = new EntityGraphNode("d3", null, null, null, null, null, false, false);
		d4 = new EntityGraphNode("d4", null, null, null, null, null, false, false);
		d5 = new EntityGraphNode("d5", null, null, null, null, null, false, false);
		d6 = new EntityGraphNode("d6", null, null, null, null, null, false, false);
		a = new ActivityGraphNode("A", "1", "Step A", ActivityType.MANUAL, "1", new Date(), false);
		b = new ActivityGraphNode("B", "2", "Step B", ActivityType.MANUAL, "1", new Date(), false);
		c = new ActivityGraphNode("C", "2", "Step C", ActivityType.MANUAL, "1", new Date(), false);
		d = new ActivityGraphNode("D", "4", "Step D", ActivityType.MANUAL, "1", new Date(), false);
	}

	/**
	 * d1 and d2 starting 0 1 2 d3' --> d1 \ / --> A / \ d4' --> d2
	 * 
	 */
	@Test
	public void testCreateLayersSimple() {
		jsoProvider = new JsoProviderTestImpl(); // use real classes
		ProvGraph graph = new ProvGraph();
		graph.addNode(d1);
		graph.addNode(d2);
		graph.addNode(d3);
		graph.addNode(d4);
		graph.addEdge(new ProvGraphEdge(d3, a));
		graph.addEdge(new ProvGraphEdge(d4, a));
		graph.addEdge(new ProvGraphEdge(a, d1));
		graph.addEdge(new ProvGraphEdge(a, d2));
		d3.setStartingNode(true);
		d4.setStartingNode(true);

		// execute
		NChartLayersArrayTestImpl ncLayersArray = (NChartLayersArrayTestImpl) NChartUtil.createLayers(jsoProvider, graph);

		// verify
		List<NChartLayer> layers = ncLayersArray.getLayers();
		NChartLayerNode lnD1 = NChartUtil.createEntityLayerNode(jsoProvider, d1);
		NChartLayerNode lnD2 = NChartUtil.createEntityLayerNode(jsoProvider, d2);
		NChartLayerNode lnD3 = NChartUtil.createEntityLayerNode(jsoProvider, d3);
		NChartLayerNode lnD4 = NChartUtil.createEntityLayerNode(jsoProvider, d4);
		List<ProvGraphNode> aConnectedNodes = new ArrayList<ProvGraphNode>();
		aConnectedNodes.add(d1);
		aConnectedNodes.add(d2);
		aConnectedNodes.add(d3);
		aConnectedNodes.add(d4);
		NChartLayerNodeTestImpl lnA = (NChartLayerNodeTestImpl) NChartUtil.createActivityLayerNode(jsoProvider, a, aConnectedNodes);

		int layerIdx;
		List<NChartLayerNode> layerNodes;

		// layer 0
		layerIdx = 0;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertTrue(layerNodes.contains(lnD3));
		assertTrue(layerNodes.contains(lnD4));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());
		// layer 1
		layerIdx = 1;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertChartLayerNodeEqual(lnA, (NChartLayerNodeTestImpl) layerNodes.get(0));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());
		// layer 2
		layerIdx = 2;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertTrue(layerNodes.contains(lnD1));
		assertTrue(layerNodes.contains(lnD2));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());

	}

	/**
	 * d1 and d5 starting
	 * 
	 * 0 1 2 3 4 5 6 d1' -> A -> d2 -> B -> d3 \ C -> d6 d5' -> D -----------> d4 /
	 */
	@Test
	public void testCreateLayersDifferentStarts() {
		jsoProvider = new JsoProviderTestImpl(); // use real classes
		ProvGraph graph = new ProvGraph();
		graph.addNode(d1);
		graph.addNode(d2);
		graph.addNode(d3);
		graph.addNode(d4);
		graph.addNode(d5);
		graph.addNode(d6);
		graph.addEdge(new ProvGraphEdge(d1, a));
		graph.addEdge(new ProvGraphEdge(a, d2));
		graph.addEdge(new ProvGraphEdge(d2, b));
		graph.addEdge(new ProvGraphEdge(b, d3));
		graph.addEdge(new ProvGraphEdge(d3, c));
		graph.addEdge(new ProvGraphEdge(d4, c));
		graph.addEdge(new ProvGraphEdge(c, d6));
		graph.addEdge(new ProvGraphEdge(d5, d));
		graph.addEdge(new ProvGraphEdge(d, d4));
		d1.setStartingNode(true);
		d5.setStartingNode(true);

		// execute
		NChartLayersArrayTestImpl ncLayersArray = (NChartLayersArrayTestImpl) NChartUtil.createLayers(jsoProvider, graph);

		// verify
		List<NChartLayer> layers = ncLayersArray.getLayers();
		NChartLayerNode lnD1 = NChartUtil.createEntityLayerNode(jsoProvider, d1);
		NChartLayerNode lnD2 = NChartUtil.createEntityLayerNode(jsoProvider, d2);
		NChartLayerNode lnD3 = NChartUtil.createEntityLayerNode(jsoProvider, d3);
		NChartLayerNode lnD4 = NChartUtil.createEntityLayerNode(jsoProvider, d4);
		NChartLayerNode lnD5 = NChartUtil.createEntityLayerNode(jsoProvider, d5);
		NChartLayerNode lnD6 = NChartUtil.createEntityLayerNode(jsoProvider, d6);
		List<ProvGraphNode> aConnectedNodes = new ArrayList<ProvGraphNode>();
		aConnectedNodes.add(d1);
		aConnectedNodes.add(d2);
		NChartLayerNodeTestImpl lnA = (NChartLayerNodeTestImpl) NChartUtil.createActivityLayerNode(jsoProvider, a, aConnectedNodes);

		List<ProvGraphNode> bConnectedNodes = new ArrayList<ProvGraphNode>();
		aConnectedNodes.add(d2);
		aConnectedNodes.add(d3);
		NChartLayerNodeTestImpl lnB = (NChartLayerNodeTestImpl) NChartUtil.createActivityLayerNode(jsoProvider, b, bConnectedNodes);

		List<ProvGraphNode> cConnectedNodes = new ArrayList<ProvGraphNode>();
		aConnectedNodes.add(d3);
		aConnectedNodes.add(d4);
		aConnectedNodes.add(d6);
		NChartLayerNodeTestImpl lnC = (NChartLayerNodeTestImpl) NChartUtil.createActivityLayerNode(jsoProvider, c, cConnectedNodes);

		List<ProvGraphNode> dConnectedNodes = new ArrayList<ProvGraphNode>();
		aConnectedNodes.add(d5);
		aConnectedNodes.add(d4);
		NChartLayerNodeTestImpl lnD = (NChartLayerNodeTestImpl) NChartUtil.createActivityLayerNode(jsoProvider, d, dConnectedNodes);

		int layerIdx;
		List<NChartLayerNode> layerNodes;

		// layer 0
		layerIdx = 0;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertEquals(2, layerNodes.size());
		assertTrue(layerNodes.contains(lnD1));
		assertTrue(layerNodes.contains(lnD5));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());
		// layer 1
		layerIdx = 1;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertEquals(2, layerNodes.size());
		Integer lnAidx = ((NChartLayerNodeTestImpl) layerNodes.get(0)).getEvent().equals(lnA.getEvent()) ? 0 : 1;
		Integer lnDidx = lnAidx == 0 ? 1 : 0;
		assertChartLayerNodeEqual(lnA, (NChartLayerNodeTestImpl) layerNodes.get(lnAidx));
		assertChartLayerNodeEqual(lnD, (NChartLayerNodeTestImpl) layerNodes.get(lnDidx));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());
		// layer 2
		layerIdx = 2;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertEquals(1, layerNodes.size());
		assertTrue(layerNodes.contains(lnD2));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());
		// layer 3
		layerIdx = 3;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertEquals(1, layerNodes.size());
		assertChartLayerNodeEqual(lnB, (NChartLayerNodeTestImpl) layerNodes.get(0));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());
		// layer 4
		layerIdx = 4;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertEquals(2, layerNodes.size());
		assertTrue(layerNodes.contains(lnD3));
		assertTrue(layerNodes.contains(lnD4));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());
		// layer 5
		layerIdx = 5;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertEquals(1, layerNodes.size());
		assertChartLayerNodeEqual(lnC, (NChartLayerNodeTestImpl) layerNodes.get(0));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());
		// layer 6
		layerIdx = 6;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertEquals(1, layerNodes.size());
		assertTrue(layerNodes.contains(lnD6));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());

	}

	/**
	 * d4 start, d1 used multiple times, ( -> denotes generatedBy or used) newer older 0 1 2 3 4
	 * ------------> d1 / / d4 -> B -> d3 -> A -> d2
	 * 
	 * Regression Test from SWC-580
	 */
	@Test
	public void testCreateLayersUsedMultipleTimes() {
		jsoProvider = new JsoProviderTestImpl(); // use real classes
		ProvGraph graph = new ProvGraph();
		graph.addNode(d1);
		graph.addNode(d2);
		graph.addNode(d3);
		graph.addNode(d4);
		graph.addEdge(new ProvGraphEdge(d4, b));
		graph.addEdge(new ProvGraphEdge(b, d1));
		graph.addEdge(new ProvGraphEdge(b, d3));
		graph.addEdge(new ProvGraphEdge(d3, a));
		graph.addEdge(new ProvGraphEdge(a, d1));
		graph.addEdge(new ProvGraphEdge(a, d2));
		d4.setStartingNode(true);

		// execute
		NChartLayersArrayTestImpl ncLayersArray = (NChartLayersArrayTestImpl) NChartUtil.createLayers(jsoProvider, graph);

		// verify
		List<NChartLayer> layers = ncLayersArray.getLayers();
		NChartLayerNode lnD1 = NChartUtil.createEntityLayerNode(jsoProvider, d1);
		NChartLayerNode lnD2 = NChartUtil.createEntityLayerNode(jsoProvider, d2);
		NChartLayerNode lnD3 = NChartUtil.createEntityLayerNode(jsoProvider, d3);
		NChartLayerNode lnD4 = NChartUtil.createEntityLayerNode(jsoProvider, d4);
		List<ProvGraphNode> aConnectedNodes = new ArrayList<ProvGraphNode>();
		aConnectedNodes.add(d1);
		aConnectedNodes.add(d2);
		aConnectedNodes.add(d3);
		NChartLayerNodeTestImpl lnA = (NChartLayerNodeTestImpl) NChartUtil.createActivityLayerNode(jsoProvider, a, aConnectedNodes);
		List<ProvGraphNode> bConnectedNodes = new ArrayList<ProvGraphNode>();
		bConnectedNodes.add(d1);
		bConnectedNodes.add(d3);
		bConnectedNodes.add(d4);
		NChartLayerNodeTestImpl lnB = (NChartLayerNodeTestImpl) NChartUtil.createActivityLayerNode(jsoProvider, b, bConnectedNodes);

		int layerIdx;
		List<NChartLayerNode> layerNodes;

		// layer 0
		layerIdx = 0;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertTrue(layerNodes.contains(lnD4));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());
		// layer 1
		layerIdx = 1;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertChartLayerNodeEqual(lnB, (NChartLayerNodeTestImpl) layerNodes.get(0));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());
		// layer 2
		layerIdx = 2;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertTrue(layerNodes.contains(lnD3));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());
		// layer 3
		layerIdx = 3;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertChartLayerNodeEqual(lnA, (NChartLayerNodeTestImpl) layerNodes.get(0));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());
		// layer 4
		layerIdx = 4;
		layerNodes = ((NChartLayerTestImpl) layers.get(layerIdx)).getNodes();
		assertTrue(layerNodes.contains(lnD1));
		assertTrue(layerNodes.contains(lnD2));
		assertEquals(DEFAULT_DURATION, ((NChartLayerTestImpl) layers.get(layerIdx)).getDuration());

	}


	@Test
	public void testCreateNChartCharacters() {
		EntityGraphNode d1 = new EntityGraphNode("d1", null, null, null, null, null, false, false);
		EntityGraphNode d2 = new EntityGraphNode("d2", null, null, null, null, null, false, false);
		ActivityGraphNode a = new ActivityGraphNode("A", "1", "Step A", ActivityType.MANUAL, "1", new Date(), false);
		Set<ProvGraphNode> graphNodes = new HashSet<ProvGraphNode>();
		graphNodes.add(d1);
		graphNodes.add(d2);
		graphNodes.add(a);

		// when
		NChartCharacters mockChars = mock(NChartCharacters.class);
		when(jsoProvider.newNChartCharacters()).thenReturn(mockChars);

		NChartCharacters characters = NChartUtil.createNChartCharacters(jsoProvider, graphNodes);

		assertTrue(mockChars == characters); // object equality
		verify(mockChars).addCharacter(d1.getId());
		verify(mockChars).addCharacter(d2.getId());
		verify(mockChars).addCharacter(a.getId());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateActivityLayerNode() {
		EntityGraphNode d1 = new EntityGraphNode("d1", null, null, null, null, null, false, false);
		EntityGraphNode d2 = new EntityGraphNode("d2", null, null, null, null, null, false, false);
		ActivityGraphNode a = new ActivityGraphNode("A", "1", "Step A", ActivityType.MANUAL, "1", new Date(), false);
		List<ProvGraphNode> graphNodes = new ArrayList<ProvGraphNode>();
		graphNodes.add(d1);
		graphNodes.add(d2);

		// when
		NChartLayerNode mockNode = mock(NChartLayerNode.class);
		when(jsoProvider.newNChartLayerNode()).thenReturn(mockNode);
		ArgumentCaptor<List> subnodeArg = ArgumentCaptor.forClass(List.class);

		NChartLayerNode actNode = NChartUtil.createActivityLayerNode(jsoProvider, a, graphNodes);


		List<String> subnodes = Arrays.asList(new String[] {d1.getId(), d2.getId()});
		assertTrue(mockNode == actNode);
		verify(mockNode).setSubnodes(subnodeArg.capture());
		assertEquals(subnodes, subnodeArg.getValue());
		verify(mockNode).setEvent(a.getId());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateEntityLayerNode() {
		EntityGraphNode d1 = new EntityGraphNode("d1", null, null, null, null, null, false, false);

		// when
		NChartLayerNode mockNode = mock(NChartLayerNode.class);
		when(jsoProvider.newNChartLayerNode()).thenReturn(mockNode);
		ArgumentCaptor<List> subnodeArg = ArgumentCaptor.forClass(List.class);

		NChartLayerNode entNode = NChartUtil.createEntityLayerNode(jsoProvider, d1);


		List<String> subnodes = Arrays.asList(new String[] {d1.getId()});
		assertTrue(mockNode == entNode);
		verify(mockNode).setSubnodes(subnodeArg.capture());
		assertEquals(subnodes, subnodeArg.getValue());
	}

	@Test
	public void testFillPositions() {
		jsoProvider = new JsoProviderTestImpl(); // use real classes
		ProvGraph graph = new ProvGraph();
		graph.addNode(d1);
		graph.addNode(d2);
		graph.addEdge(new ProvGraphEdge(d1, a));
		graph.addEdge(new ProvGraphEdge(a, d2));
		d1.setStartingNode(true);

		Map<String, List<XYPoint>> idToPoints = new HashMap<String, List<XYPoint>>();
		idToPoints.put(d2.getId(), Arrays.asList(new XYPoint[] {new XYPointTestImpl(200, 1)}));
		idToPoints.put(a.getId(), Arrays.asList(new XYPoint[] {new XYPointTestImpl(100, 1)}));
		idToPoints.put(d1.getId(), Arrays.asList(new XYPoint[] {new XYPointTestImpl(0, 1)}));

		LayoutResultTestImpl layoutResult = new LayoutResultTestImpl(idToPoints);

		NChartUtil.fillPositions(layoutResult, graph);

		// verify transforms and position changes in graph
		assertTrue((int) (1 * NChartUtil.SCALE_X) == d2.getxPos());
		assertTrue((int) (1 * NChartUtil.SCALE_X) == a.getxPos());
		assertTrue((int) (1 * NChartUtil.SCALE_X) == d1.getxPos());

		assertTrue(0 == d2.getyPos());
		assertTrue((int) (100 * NChartUtil.SCALE_Y) == a.getyPos());
		assertTrue((int) (200 * NChartUtil.SCALE_Y) == d1.getyPos());
	}

	/*
	 * Private Methods
	 */
	private void assertChartLayerNodeEqual(NChartLayerNodeTestImpl expected, NChartLayerNodeTestImpl actual) {
		for (String id : expected.getSubnodes()) {
			assertTrue(actual.getSubnodes().contains(id));
		}
		assertEquals(expected.getEvent(), actual.getEvent());
	}

}
