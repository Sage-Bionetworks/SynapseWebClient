package org.sagebionetworks.web.client.widget.provenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceWidgetViewImpl extends LayoutContainer implements ProvenanceWidgetView {
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;		
	private ProvGraph graph;
	private List<Connection> connections;
	private LayoutContainer debug;
	private SynapseJSNIUtils synapseJSNIUtils;
	private HashMap<String,String> filledPopoverIds;
		
	private int height = WidgetConstants.PROV_WIDGET_HEIGHT_DEFAULT;
	
	@Inject
	public ProvenanceWidgetViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}
		
	
	@Override
	protected void onRender(Element parent, int index) {
		// TODO Auto-generated method stub
		super.onRender(parent, index);
		initJsPlumb();
		createGraph();
	}

	@Override
	public void setGraph(ProvGraph graph) {
		this.graph = graph;
		if(this.isRendered()) {
			createGraph();
		}
	}

	@Override
	public Widget asWidget() {		
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
	}

	
	/*
	 * Private Methods
	 */	
	private void createGraph() {
		this.removeAll(true);
		this.filledPopoverIds = new HashMap<String,String>();
		LayoutContainer prov = new LayoutContainer();
		prov.setStyleAttribute("position", "relative");		
		prov.setHeight(height);		
		connections = new ArrayList<ProvenanceWidgetViewImpl.Connection>();		
		
		if(graph != null) {
			// add nodes to graph
			Set<ProvGraphNode> nodes = graph.getNodes();
			for(ProvGraphNode node : nodes) {			
				prov.add(getNodeContainer(node));
			}			
		}
		
		this.add(prov, new MarginData(5));
		this.addStyleName("scroll-auto");
		this.layout(true);
		
		// make connections (assure DOM elements are in before asking jsPlumb to connect them)
		if(graph != null) {			
			Set<ProvGraphEdge> edges = graph.getEdges();
			for(ProvGraphEdge edge : edges) {
				connect(edge.getSink().getId(), edge.getSource().getId());
			}
		}

	}
	
	/**
	 * Create an actual LayoutContainer rendering of the node
	 * @param node
	 * @return
	 */
	private LayoutContainer getNodeContainer(final ProvGraphNode node) {
		if(node instanceof EntityGraphNode) {
			LayoutContainer container = ProvViewUtil.createEntityContainer((EntityGraphNode)node, iconsImageBundle);
			addToolTipToContainer(node, container, DisplayConstants.ENTITY);			
			return container;
		} else if(node instanceof ActivityGraphNode) {
			LayoutContainer container = ProvViewUtil.createActivityContainer((ActivityGraphNode)node, iconsImageBundle);
			// create tool tip for defined activities only
			if(((ActivityGraphNode) node).getType() == ActivityType.UNDEFINED) {
				addUndefinedToolTip(container);
			} else {
				addToolTipToContainer(node, container, DisplayConstants.ACTIVITY);				
			}
			return container;
		} else if(node instanceof ExpandGraphNode) {
			return ProvViewUtil.createExpandContainer((ExpandGraphNode)node, sageImageBundle, presenter);
		}
		return null;
	}

	private void addToolTipToContainer(final ProvGraphNode node, final LayoutContainer container, final String title) {		
		container.setToolTip(ProvViewUtil.createTooltipConfig(title, DisplayUtils.getLoadingHtml(sageImageBundle)));			
		container.addListener(Events.OnMouseOver, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {	
				// load the tooltip contents only once
				if(filledPopoverIds.containsKey(node.getId())) {															
					container.setToolTip(ProvViewUtil.createTooltipConfig(title, filledPopoverIds.get(node.getId())));
					return;
				}															
				// retrieve info
				presenter.getInfo(node.getId(), new AsyncCallback<KeyValueDisplay<String>>() {						
					@Override
					public void onSuccess(KeyValueDisplay<String> result) {
						String rendered = ProvViewUtil.createEntityPopoverHtml(result).asString();
						filledPopoverIds.put(container.getId(), rendered);
					    container.setToolTip(ProvViewUtil.createTooltipConfig(title, rendered));										
					}
					
					@Override
					public void onFailure(Throwable caught) {
						container.setToolTip(ProvViewUtil.createTooltipConfig(title, DisplayConstants.ERROR_GENERIC_RELOAD));
					}
				});
			}
		});
	}

	private void addUndefinedToolTip(LayoutContainer container) {
		Anchor a = new Anchor();
		a.setHref(WebConstants.PROVENANCE_API_URL);
		a.setText(DisplayConstants.HOW_TO_DEFINE_ACTIVITY);
		a.setTarget("_blank");		
		a.setStyleName("link");
		container.setToolTip(ProvViewUtil.createMessageConfig(DisplayConstants.DEFINE_ACTIVITY, a.toString()));		
	}

	
	private static native void connect(String parentId, String childId) /*-{
		var jsPlumb = $wnd.jsPlumb;		
		jsPlumb.connect({source:parentId, target:childId, overlays:jsP_overlays	});
	}-*/;
	
	private static native void initJsPlumb() /*-{
		;(function() {
					
			$wnd.jsPlumbDemo = {					
				init : function() {					
					var color = "gray";
					var jsPlumb = $wnd.jsPlumb;		
					jsPlumb.importDefaults({
						// notice the 'curviness' argument to this Bezier curve.  the curves on this page are far smoother
						// than the curves on the first demo, which use the default curviness value.			
						Connector : [ "Straight" ],
						DragOptions : { cursor: "pointer", zIndex:2000 },
						PaintStyle : { strokeStyle:color, lineWidth:1 },
						EndpointStyle : { radius:0.01, fillStyle:color },
						HoverPaintStyle : {strokeStyle:"#ec9f2e" },
						EndpointHoverStyle : {fillStyle:"#ec9f2e" },			
						Anchors :  [ "BottomCenter", "TopCenter" ]
					});				
						
					// declare some common values:
					jsP_arrowCommon = { foldback:0.7, fillStyle:color, length:7, width:7 };
						// use three-arg spec to create two different arrows with the common values:
					jsP_overlays = [
							[ "Arrow", { location:0.7, direction:1 }, jsP_arrowCommon ]
						];					
				}
			};
			
		})();

		//  This file contains the JS that handles the first init of each jQuery demonstration, and also switching
		//  between render modes.
		$wnd.jsPlumb.bind("ready", function() {
			// chrome fix.
			document.onselectstart = function () { return false; };
			$wnd.jsPlumb.setRenderMode($wnd.jsPlumb.SVG);
			$wnd.jsPlumbDemo.init();
		});

	
	}-*/;

	private class Connection {
		private String parentId;
		private String childId;
		public Connection(String parentId, String childId) {
			super();
			this.parentId = parentId;
			this.childId = childId;
		}
		public String getParentId() {
			return parentId;
		}
		public String getChildId() {
			return childId;
		}		
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}
	
}
