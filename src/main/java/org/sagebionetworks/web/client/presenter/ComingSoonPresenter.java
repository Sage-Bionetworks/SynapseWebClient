package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
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
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

		synapseClient.getEntity(token, new AsyncCallback<EntityWrapper>() {			
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					Entity entity = nodeModelCreator.createEntity(result);
					view.setEntity(entity);
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}
			}			
			@Override
			public void onFailure(Throwable caught) {
				view.showInfo("Error", "error getting: " + token);
			}
		});
		
		ProvGraph graph = new ProvGraph();		
		EntityGraphNode d1;
		EntityGraphNode d2;
		EntityGraphNode d3;
		EntityGraphNode d4;
		EntityGraphNode d5;
		EntityGraphNode d6;
		EntityGraphNode d7;
		EntityGraphNode d8;
		EntityGraphNode d9;
		EntityGraphNode d10;
		EntityGraphNode d11;
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
		d7 = new EntityGraphNode("d7",null,null,null,null,null,false,false);
		d8 = new EntityGraphNode("d8",null,null,null,null,null,false,false);
		d9 = new EntityGraphNode("d9",null,null,null,null,null,false,false);
		d10 = new EntityGraphNode("d10",null,null,null,null,null,false,false);
		d11 = new EntityGraphNode("d11",null,null,null,null,null,false,false);
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
		graph.addNode(d7);
		graph.addNode(d8);
		graph.addNode(d9);
		graph.addNode(d10);
		graph.addNode(d11);
		graph.addEdge(new ProvGraphEdge(d1, a));
		graph.addEdge(new ProvGraphEdge(d2, a));
		graph.addEdge(new ProvGraphEdge(a, d3));
		graph.addEdge(new ProvGraphEdge(a, d4));
		graph.addEdge(new ProvGraphEdge(a, d5));
		graph.addEdge(new ProvGraphEdge(a, d6));
		graph.addEdge(new ProvGraphEdge(d4, b));		
		graph.addEdge(new ProvGraphEdge(d7, b));		
		graph.addEdge(new ProvGraphEdge(d6, d));		
		graph.addEdge(new ProvGraphEdge(b, d8));		
		graph.addEdge(new ProvGraphEdge(d5, c));		
		graph.addEdge(new ProvGraphEdge(d8, c));		
		graph.addEdge(new ProvGraphEdge(c, d9));		
		graph.addEdge(new ProvGraphEdge(c, d10));		
		graph.addEdge(new ProvGraphEdge(d, d11));		
		d10.setStartingNode(true);
		d11.setStartingNode(true);

		
		// create characters
		Set<ProvGraphNode> graphNodes = new HashSet<ProvGraphNode>();
		graphNodes.add(d1);
		graphNodes.add(d2);
		graphNodes.add(d3);
		graphNodes.add(d4);
		graphNodes.add(d5);
		graphNodes.add(d6);
		graphNodes.add(d7);
		graphNodes.add(d8);
		graphNodes.add(d9);
		graphNodes.add(d10);
		graphNodes.add(d11);
		graphNodes.add(a);
		graphNodes.add(b);
		graphNodes.add(c);
		graphNodes.add(d);
		NChartCharacters characters = NChartUtil.createNChartCharacters(jsoProvider, graphNodes);
		
		//NChartLayersArray ncLayersArray = createFakeLayersArray(d1, d2, d3, d4, a);		
		NChartLayersArray ncLayersArray = NChartUtil.createLayers(jsoProvider, graph);
		
		LayoutResult layoutResult = jsniUtils.nChartlayout(ncLayersArray, characters);
						
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
