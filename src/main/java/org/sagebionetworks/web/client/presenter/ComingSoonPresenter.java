package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.List;

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

		ProvGraph provGraph = new ProvGraph();		
		EntityGraphNode d1 = new EntityGraphNode("d1",null,null,null,null,null,false);		
		EntityGraphNode d2 = new EntityGraphNode("d2",null,null,null,null,null,false);
		EntityGraphNode d3 = new EntityGraphNode("d3",null,null,null,null,null,false);
		EntityGraphNode d4 = new EntityGraphNode("d4",null,null,null,null,null,false);
		ActivityGraphNode a = new ActivityGraphNode("A","1","Step A", ActivityType.MANUAL);
		ProvGraphEdge edge = new ProvGraphEdge(d1, d2);
		provGraph.addNode(d1);
		provGraph.addEdge(edge);
		
		// create characters
		List<ProvGraphNode> graphNodes = new ArrayList<ProvGraphNode>();
		graphNodes.add(d1);
		graphNodes.add(d2);
		graphNodes.add(d3);
		graphNodes.add(d4);
		graphNodes.add(a);
		NChartCharacters characters = NChartUtil.createNChartCharacters(jsoProvider, graphNodes);
		
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
		LayoutResult result = jsniUtils.nChartlayout(ncLayersArray, characters);
				
		// TODO : translate LayoutResult back into the ProvGraph 
		String s = "";
		for(ProvGraphNode n : graphNodes) {
			String id = n.getId();
			List<XYPoint> points = result.getPointsForId(id);
			s+= id + ": ";
			for(XYPoint pt : points) {
				s+= pt.getX() + "," + pt.getY() + "   ";
			}
			s+= "<br>\n";
		}
		view.showErrorMessage(s);
	}

	
	@Override
    public String mayStop() {
        view.clear();
        return null;
        
    }
	
}
