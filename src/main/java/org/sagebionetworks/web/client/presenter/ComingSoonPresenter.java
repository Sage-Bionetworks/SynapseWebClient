package org.sagebionetworks.web.client.presenter;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.ComingSoon;
import org.sagebionetworks.web.client.services.LayoutServiceAsync;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.ComingSoonView;
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

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ComingSoonPresenter extends AbstractActivity implements ComingSoonView.Presenter {
		
	private ComingSoon place;
	private ComingSoonView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	LayoutServiceAsync layoutService;
	JsoProvider jsoProvider;
	SynapseJSNIUtils jsniUtils;
	
	@Inject
	public ComingSoonPresenter(ComingSoonView view,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			LayoutServiceAsync layoutService,
			JsoProvider jsoProvider,
			SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.layoutService = layoutService;
		this.jsoProvider = jsoProvider;
		this.jsniUtils = jsniUtils;
		
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	public void setPlace(ComingSoon place) {
		this.place = place;
		this.view.setPresenter(this);
		final String token = place.toToken();

		view.setEntity(null);

//		ProvGraph graph = new ProvGraph();		
//		EntityGraphNode d1 = new EntityGraphNode("d1",null,null,null,null,null,false,true);		
//		EntityGraphNode d2 = new EntityGraphNode("d2",null,null,null,null,null,false,false);
//		EntityGraphNode d3 = new EntityGraphNode("d3",null,null,null,null,null,false,false);
//		EntityGraphNode d4 = new EntityGraphNode("d4",null,null,null,null,null,false,false);
//		ActivityGraphNode a = new ActivityGraphNode("A","1","Step A", ActivityType.MANUAL,false);
//		graph.addNode(d1);
//		graph.addNode(d2);
//		graph.addNode(d3);
//		graph.addNode(d4);
//		graph.addEdge(new ProvGraphEdge(d1, a));
//		graph.addEdge(new ProvGraphEdge(d2, a));
//		graph.addEdge(new ProvGraphEdge(a, d3));
//		graph.addEdge(new ProvGraphEdge(a, d4));

		ProvGraph graph = new ProvGraph();		
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

		
		// create characters
		Set<ProvGraphNode> graphNodes = new HashSet<ProvGraphNode>();
		graphNodes.add(d1);
		graphNodes.add(d2);
		graphNodes.add(d3);
		graphNodes.add(d4);
		graphNodes.add(d5);
		graphNodes.add(d6);
		graphNodes.add(a);
		graphNodes.add(b);
		graphNodes.add(c);
		graphNodes.add(d);
		NChartCharacters characters = NChartUtil.createNChartCharacters(jsoProvider, graphNodes);
		
		//NChartLayersArray ncLayersArray = createFakeLayersArray(d1, d2, d3, d4, a);		
		NChartLayersArray ncLayersArray = NChartUtil.createLayers(jsoProvider, graph);
		
		LayoutResult layoutResult = jsniUtils.nChartlayout(ncLayersArray, characters);
				
		// TODO : translate LayoutResult back into the ProvGraph 
		String s = "";
		for(ProvGraphNode n : graphNodes) {
			String id = n.getId();
			List<XYPoint> points = layoutResult.getPointsForId(id);
			s+= id + ": ";
			for(XYPoint pt : points) {
				s+= pt.getX() + "," + pt.getY() + "   ";
			}
			s+= "<br>\n";
		}
		view.showErrorMessage(s);
		
		NChartUtil.fillPositions(layoutResult, graph);
	}

	private NChartLayersArray createFakeLayersArray(EntityGraphNode d1,
			EntityGraphNode d2, EntityGraphNode d3, EntityGraphNode d4,
			ActivityGraphNode a) {
		// create NChart nodes for each entity individually
		NChartLayerNode lnD1 = NChartUtil.createEntityLayerNode(jsoProvider, d1);
		NChartLayerNode lnD2 = NChartUtil.createEntityLayerNode(jsoProvider, d2);
		NChartLayerNode lnD3 = NChartUtil.createEntityLayerNode(jsoProvider, d3);
		NChartLayerNode lnD4 = NChartUtil.createEntityLayerNode(jsoProvider, d4);
		
		// create NChart nodes for each activity with their connected nodes
		List<ProvGraphNode> aConnectedNodes = new ArrayList<ProvGraphNode>();
		aConnectedNodes.add(d1);
		aConnectedNodes.add(d2);
		aConnectedNodes.add(d3);
		aConnectedNodes.add(d4);
		NChartLayerNode lnA = NChartUtil.createActivityLayerNode(jsoProvider, a, aConnectedNodes);		
				
		// define NChart layers
		List<NChartLayer> layers = new ArrayList<NChartLayer>();
		List<NChartLayerNode> layerNodes;
		NChartLayer layer;
		// 0 - inputs
		layerNodes = new ArrayList<NChartLayerNode>();
		layerNodes.add(lnD1);
		layerNodes.add(lnD2);
		layer = jsoProvider.newNChartLayer();
		layer.setDuration(10);
		layer.setNodes(layerNodes);
		layers.add(layer);
		// 1 - activity
		layerNodes = new ArrayList<NChartLayerNode>();
		layerNodes.add(lnA);
		layer = jsoProvider.newNChartLayer();
		layer.setDuration(10);
		layer.setNodes(layerNodes);
		layers.add(layer);
		// 2 outputs
		layerNodes = new ArrayList<NChartLayerNode>();
		layerNodes.add(lnD3);
		layerNodes.add(lnD4);
		layer = jsoProvider.newNChartLayer();
		layer.setDuration(10);
		layer.setNodes(layerNodes);
		layers.add(layer);	
		
		// form array
		NChartLayersArray ncLayersArray = jsoProvider.newNChartLayersArray();
		ncLayersArray.setLayers(layers);
		return ncLayersArray;
	}

	
	@Override
    public String mayStop() {
        view.clear();
        return null;
        
    }
	
}
