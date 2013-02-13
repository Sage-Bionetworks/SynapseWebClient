package org.sagebionetworks.web.unitclient.widget.provenance.nchart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayer;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayerNode;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartUtil;
import org.sagebionetworks.web.client.widget.provenance.nchart.XYPoint;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

public class NChartUtilTest {

	//JsoProvider jsoProvider = new JsoProviderJavaImpl();
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
		d1 = new EntityGraphNode("d1",null,null,null,null,null,false,false);		
		d2 = new EntityGraphNode("d2",null,null,null,null,null,false,false);
		d3 = new EntityGraphNode("d3",null,null,null,null,null,false,false);
		d4 = new EntityGraphNode("d4",null,null,null,null,null,false,false);
		d5 = new EntityGraphNode("d5",null,null,null,null,null,false,false);
		d6 = new EntityGraphNode("d6",null,null,null,null,null,false,false);
		a = new ActivityGraphNode("A","1","Step A", ActivityType.MANUAL,false);
		b = new ActivityGraphNode("B","2","Step B", ActivityType.MANUAL,false);
		c = new ActivityGraphNode("C","2","Step C", ActivityType.MANUAL,false);
		d = new ActivityGraphNode("D","4","Step D", ActivityType.MANUAL,false);
	}
	
	/**
	 * d1 and d2 starting 
	 *    0   1 2 
	 *    d1'   d3 
	 *       \ /
	 *        A
	 *       / \
	 *    d2'   d4
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
		graph.addEdge(new ProvGraphEdge(d1, a));
		graph.addEdge(new ProvGraphEdge(d2, a));
		graph.addEdge(new ProvGraphEdge(a, d3));
		graph.addEdge(new ProvGraphEdge(a, d4));		
		d1.setStartingNode(true);
		d2.setStartingNode(true);
		
		// execute
		NChartLayersArrayImpl ncLayersArray = (NChartLayersArrayImpl) NChartUtil.createLayers(jsoProvider, graph);
		
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
		NChartLayerNodeImpl lnA = (NChartLayerNodeImpl) NChartUtil.createActivityLayerNode(jsoProvider, a, aConnectedNodes);		

		int layerIdx;
		List<NChartLayerNode> layerNodes;		
		
		// layer 0
		layerIdx = 0;
		layerNodes = ((NChartLayerImpl) layers.get(layerIdx)).getNodes();		
		assertTrue(layerNodes.contains(lnD1));
		assertTrue(layerNodes.contains(lnD2));
		assertEquals(DEFAULT_DURATION, ((NChartLayerImpl) layers.get(layerIdx)).getDuration());
		// layer 1
		layerIdx = 1;
		layerNodes = ((NChartLayerImpl) layers.get(layerIdx)).getNodes();		
		assertChartLayerNodeEqual(lnA, (NChartLayerNodeImpl) layerNodes.get(0));		
		assertEquals(DEFAULT_DURATION, ((NChartLayerImpl) layers.get(layerIdx)).getDuration());
		// layer 2
		layerIdx = 2;
		layerNodes = ((NChartLayerImpl) layers.get(layerIdx)).getNodes();
		assertTrue(layerNodes.contains(lnD3));
		assertTrue(layerNodes.contains(lnD4));
		assertEquals(DEFAULT_DURATION, ((NChartLayerImpl) layers.get(layerIdx)).getDuration());
		
	}

	/**
	 * d1 and d5 starting 
	 * 
	 *    0      1    2     3    4  5    6 
	 *    d1' -> A -> d2 -> B -> d3 
	 *                             \
	 *                              C -> d6
	 *    d5' -> D -----------> d4 /
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
		NChartLayersArrayImpl ncLayersArray = (NChartLayersArrayImpl) NChartUtil.createLayers(jsoProvider, graph);
		
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
		NChartLayerNodeImpl lnA = (NChartLayerNodeImpl) NChartUtil.createActivityLayerNode(jsoProvider, a, aConnectedNodes);		
		
		List<ProvGraphNode> bConnectedNodes = new ArrayList<ProvGraphNode>();
		aConnectedNodes.add(d2);
		aConnectedNodes.add(d3);
		NChartLayerNodeImpl lnB = (NChartLayerNodeImpl) NChartUtil.createActivityLayerNode(jsoProvider, b, bConnectedNodes);
		
		List<ProvGraphNode> cConnectedNodes = new ArrayList<ProvGraphNode>();
		aConnectedNodes.add(d3);
		aConnectedNodes.add(d4);
		aConnectedNodes.add(d6);
		NChartLayerNodeImpl lnC = (NChartLayerNodeImpl) NChartUtil.createActivityLayerNode(jsoProvider, c, cConnectedNodes);		
		
		List<ProvGraphNode> dConnectedNodes = new ArrayList<ProvGraphNode>();
		aConnectedNodes.add(d5);
		aConnectedNodes.add(d4);
		NChartLayerNodeImpl lnD = (NChartLayerNodeImpl) NChartUtil.createActivityLayerNode(jsoProvider, d, dConnectedNodes);		

		int layerIdx;
		List<NChartLayerNode> layerNodes;
		
		// layer 0
		layerIdx = 0;
		layerNodes = ((NChartLayerImpl) layers.get(layerIdx)).getNodes();		
		assertEquals(2, layerNodes.size());
		assertTrue(layerNodes.contains(lnD1));
		assertTrue(layerNodes.contains(lnD5));
		assertEquals(DEFAULT_DURATION, ((NChartLayerImpl) layers.get(layerIdx)).getDuration());
		// layer 1
		layerIdx = 1;
		layerNodes = ((NChartLayerImpl) layers.get(layerIdx)).getNodes(); 
		assertEquals(2, layerNodes.size());
		Integer lnAidx = ((NChartLayerNodeImpl) layerNodes.get(0)).getEvent().equals(lnA.getEvent()) ? 0 : 1;
		Integer lnDidx = lnAidx == 0 ? 1 : 0;
		assertChartLayerNodeEqual(lnA, (NChartLayerNodeImpl) layerNodes.get(lnAidx));		
		assertChartLayerNodeEqual(lnD, (NChartLayerNodeImpl) layerNodes.get(lnDidx));		
		assertEquals(DEFAULT_DURATION, ((NChartLayerImpl) layers.get(layerIdx)).getDuration());
		// layer 2
		layerIdx = 2;
		layerNodes = ((NChartLayerImpl) layers.get(layerIdx)).getNodes();
		assertEquals(1, layerNodes.size());
		assertTrue(layerNodes.contains(lnD2));
		assertEquals(DEFAULT_DURATION, ((NChartLayerImpl) layers.get(layerIdx)).getDuration());
		// layer 3
		layerIdx = 3;
		layerNodes = ((NChartLayerImpl) layers.get(layerIdx)).getNodes();		
		assertEquals(1, layerNodes.size());
		assertChartLayerNodeEqual(lnB, (NChartLayerNodeImpl) layerNodes.get(0));
		assertEquals(DEFAULT_DURATION, ((NChartLayerImpl) layers.get(layerIdx)).getDuration());
		// layer 4
		layerIdx = 4;
		layerNodes = ((NChartLayerImpl) layers.get(layerIdx)).getNodes();
		assertEquals(2, layerNodes.size());
		assertTrue(layerNodes.contains(lnD3));
		assertTrue(layerNodes.contains(lnD4));
		assertEquals(DEFAULT_DURATION, ((NChartLayerImpl) layers.get(layerIdx)).getDuration());
		// layer 5
		layerIdx = 5;
		layerNodes = ((NChartLayerImpl) layers.get(layerIdx)).getNodes(); 
		assertEquals(1, layerNodes.size());
		assertChartLayerNodeEqual(lnC, (NChartLayerNodeImpl) layerNodes.get(0));		
		assertEquals(DEFAULT_DURATION, ((NChartLayerImpl) layers.get(layerIdx)).getDuration());
		// layer 6
		layerIdx = 6;
		layerNodes = ((NChartLayerImpl) layers.get(layerIdx)).getNodes();
		assertEquals(1, layerNodes.size());
		assertTrue(layerNodes.contains(lnD6));
		assertEquals(DEFAULT_DURATION, ((NChartLayerImpl) layers.get(layerIdx)).getDuration());
		
	}

	@Test
	public void testCreateNChartCharacters() {
		EntityGraphNode d1 = new EntityGraphNode("d1",null,null,null,null,null,false,false);		
		EntityGraphNode d2 = new EntityGraphNode("d2",null,null,null,null,null,false,false);
		ActivityGraphNode a = new ActivityGraphNode("A","1","Step A", ActivityType.MANUAL,false);
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
		EntityGraphNode d1 = new EntityGraphNode("d1",null,null,null,null,null,false,false);		
		EntityGraphNode d2 = new EntityGraphNode("d2",null,null,null,null,null,false,false);
		ActivityGraphNode a = new ActivityGraphNode("A","1","Step A", ActivityType.MANUAL,false);
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
		assertEquals(subnodes,subnodeArg.getValue());
		verify(mockNode).setEvent(a.getId());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateEntityLayerNode() {
		EntityGraphNode d1 = new EntityGraphNode("d1",null,null,null,null,null,false,false);		

		// when
		NChartLayerNode mockNode = mock(NChartLayerNode.class);
		when(jsoProvider.newNChartLayerNode()).thenReturn(mockNode);
		ArgumentCaptor<List> subnodeArg = ArgumentCaptor.forClass(List.class);
		
		NChartLayerNode entNode = NChartUtil.createEntityLayerNode(jsoProvider, d1);
				
		
		List<String> subnodes = Arrays.asList(new String[] {d1.getId()});
		assertTrue(mockNode == entNode);
		verify(mockNode).setSubnodes(subnodeArg.capture());
		assertEquals(subnodes,subnodeArg.getValue());
	}

	
	
	/*
	 * Private Methods
	 */
	private void assertChartLayerNodeEqual(NChartLayerNodeImpl expected,
			NChartLayerNodeImpl actual) {
		for(String id : expected.getSubnodes()) {
			assertTrue(actual.getSubnodes().contains(id));
		}
		assertEquals(expected.getEvent(), actual.getEvent());
	}

	
	/*
	 * Test implementations
	 */
	private class JsoProviderTestImpl implements JsoProvider {

		@Override
		public LayoutResult newLayerResult() {
			return new LayoutResultImpl();
		}

		@Override
		public NChartCharacters newNChartCharacters() {
			return new NChartCharactersImpl();
		}

		@Override
		public NChartLayer newNChartLayer() {
			return new NChartLayerImpl();
		}

		@Override
		public NChartLayerNode newNChartLayerNode() {
			return new NChartLayerNodeImpl();
		}

		@Override
		public NChartLayersArray newNChartLayersArray() {
			return new NChartLayersArrayImpl();
		}
		
	}
	
	private class NChartLayerNodeImpl implements NChartLayerNode {
		List<String> subnodes;
		String event;
		
		@Override
		public void setSubnodes(List<String> subnodes) {
			this.subnodes = subnodes;
		}

		@Override
		public void setEvent(String event) {
			this.event = event;
		}

		public List<String> getSubnodes() {
			return subnodes;
		}

		public String getEvent() {
			return event;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((event == null) ? 0 : event.hashCode());
			result = prime * result
					+ ((subnodes == null) ? 0 : subnodes.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NChartLayerNodeImpl other = (NChartLayerNodeImpl) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (event == null) {
				if (other.event != null)
					return false;
			} else if (!event.equals(other.event))
				return false;
			if (subnodes == null) {
				if (other.subnodes != null)
					return false;
			} else if (!subnodes.equals(other.subnodes))
				return false;
			return true;
		}

		private NChartUtilTest getOuterType() {
			return NChartUtilTest.this;
		}
		
	}
	
	private class NChartLayerImpl implements NChartLayer {
		List<NChartLayerNode> nodes;
		int duration;		
		
		@Override
		public void setNodes(List<NChartLayerNode> nodes) {
			this.nodes = nodes;
		}

		@Override
		public void setDuration(int duration) {
			this.duration = duration;
		}

		public List<NChartLayerNode> getNodes() {
			return nodes;
		}

		public int getDuration() {
			return duration;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + duration;
			result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NChartLayerImpl other = (NChartLayerImpl) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (duration != other.duration)
				return false;
			if (nodes == null) {
				if (other.nodes != null)
					return false;
			} else if (!nodes.equals(other.nodes))
				return false;
			return true;
		}

		private NChartUtilTest getOuterType() {
			return NChartUtilTest.this;
		}		
		
	}
	
	private class NChartLayersArrayImpl implements NChartLayersArray {
		List<NChartLayer> layers;
		@Override
		public void setLayers(List<NChartLayer> layers) {
			this.layers = layers;
		}
		public List<NChartLayer> getLayers() {
			return layers;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((layers == null) ? 0 : layers.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NChartLayersArrayImpl other = (NChartLayersArrayImpl) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (layers == null) {
				if (other.layers != null)
					return false;
			} else if (!layers.equals(other.layers))
				return false;
			return true;
		}
		private NChartUtilTest getOuterType() {
			return NChartUtilTest.this;
		}
		
	}
	
	private class NChartCharactersImpl implements NChartCharacters {
		List<String> characters = new ArrayList<String>();
		
		@Override
		public void addCharacter(String characterId) {
			characters.add(characterId);
		}
		
	}

	private class LayoutResultImpl implements LayoutResult {
		Map<String,List<XYPoint>> nodeToPoints;
		
		@Override
		public List<XYPoint> getPointsForId(String provGraphNodeId) {
			return new ArrayList<XYPoint>();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((nodeToPoints == null) ? 0 : nodeToPoints.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LayoutResultImpl other = (LayoutResultImpl) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (nodeToPoints == null) {
				if (other.nodeToPoints != null)
					return false;
			} else if (!nodeToPoints.equals(other.nodeToPoints))
				return false;
			return true;
		}

		private NChartUtilTest getOuterType() {
			return NChartUtilTest.this;
		}		
	
	}
	
	
	
}
