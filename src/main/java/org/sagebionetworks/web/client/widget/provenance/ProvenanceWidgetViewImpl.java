package org.sagebionetworks.web.client.widget.provenance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ExternalGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceWidgetViewImpl extends FlowPanel implements ProvenanceWidgetView {
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private PortalGinInjector ginInjector;
	private ProvGraph graph;
	private FlowPanel debug;
	private SynapseJSNIUtils synapseJSNIUtils;
	private HashMap<String, String> filledPopoverIds;
	private Integer height = null;
	private static final String TOP3_LEFT3 = "margin-left-3 margin-top-3";
	private Div synAlertContainer = new Div();
	private FlowPanel container;
	private FlowPanel thisLayoutContainer;
	private FlowPanel prov;
	private IsWidget loadingContainer;
	private Map<String, ProvNodeContainer> nodeToContainer;

	@Inject
	public ProvenanceWidgetViewImpl(SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils, PortalGinInjector ginInjector) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.ginInjector = ginInjector;

		container = new FlowPanel();
		this.thisLayoutContainer = this;
		this.add(container);
		this.add(synAlertContainer);
		loadingContainer = DisplayUtils.getLoadingWidget("Loading provenance");
	}

	@Override
	public void setGraph(ProvGraph graph) {
		this.graph = graph;
		createGraph();
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
		container.add(loadingContainer);
		container.addStyleName(TOP3_LEFT3);
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
		container.clear();
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	/*
	 * Private Methods
	 */
	private void createGraph() {
		nodeToContainer = new HashMap<String, ProvNodeContainer>();
		this.filledPopoverIds = new HashMap<String, String>();
		prov = new FlowPanel();
		if (height != null) {
			prov.setHeight(height + "px");
		}
		prov.addStyleName("position-relative margin-5");
		ProvNodeContainer startingNodeContainer = null;
		if (graph != null) {
			container.clear();
			// add nodes to graph
			Set<ProvGraphNode> nodes = graph.getNodes();
			for (ProvGraphNode node : nodes) {
				ProvNodeContainer container = getNodeContainer(node);
				nodeToContainer.put(node.getId(), container);
				prov.add(container);
				if (node.isStartingNode()) {
					startingNodeContainer = container;
				}
			}
		}

		container.add(prov);
		this.addStyleName("scroll-auto");

		if (graph != null) {
			// make connections (assure DOM elements are in before asking jsPlumb to connect
			// them)
			beforeJSPlumbLoad(prov.getElement().getId());
			Set<ProvGraphEdge> edges = graph.getEdges();
			for (ProvGraphEdge edge : edges) {
				connect(edge.getSink().getId(), edge.getSource().getId());
			}

			// look for old versions
			presenter.findOldVersions();
			afterJSPlumbLoad();
		}

		if (startingNodeContainer != null && graph.getNodes().size() > 3 && DisplayUtils.isInViewport(startingNodeContainer)) {
			startingNodeContainer.getElement().scrollIntoView();
		}
	}

	/**
	 * Create an actual LayoutContainer rendering of the node
	 * 
	 * @param node
	 * @return
	 */
	private ProvNodeContainer getNodeContainer(final ProvGraphNode node) {
		if (node instanceof EntityGraphNode) {
			ProvNodeContainer container = ProvViewUtil.createEntityContainer((EntityGraphNode) node, iconsImageBundle);
			return container;
		} else if (node instanceof ActivityGraphNode) {
			ProvNodeContainer container = ProvViewUtil.createActivityContainer((ActivityGraphNode) node, iconsImageBundle, ginInjector);
			// create tool tip for defined activities only
			return container;
		} else if (node instanceof ExpandGraphNode) {
			return ProvViewUtil.createExpandContainer((ExpandGraphNode) node, presenter, this);
		} else if (node instanceof ExternalGraphNode) {
			ProvNodeContainer container = ProvViewUtil.createExternalUrlContainer((ExternalGraphNode) node, iconsImageBundle);
			return container;
		}
		return null;
	}

	private static native void connect(String parentId, String childId) /*-{
		try {
			jsPlumbInstance.connect({
				source : parentId,
				target : childId,
				overlays : jsP_overlays
			});
		} catch (err) {
			console.log(err);
		}
	}-*/;

	/**
	 * Call before connecting divs. Suspends drawing graph until bulk operation is complete (call
	 * afterJSPlumbLoad)
	 * 
	 * @param parentContainerId
	 */
	private static native void beforeJSPlumbLoad(String containerId) /*-{
		;
		(function() {
			$wnd.jsPlumbDemo = {
				init : function() {
					try {
						var color = "gray";
						jsPlumbInstance = $wnd.jsPlumb.getInstance();
						jsPlumbInstance.importDefaults({
							// notice the 'curviness' argument to this Bezier curve.  the curves on this page are far smoother
							// than the curves on the first demo, which use the default curviness value.			
							Connector : [ "Straight" ],
							DragOptions : {
								cursor : "pointer",
								zIndex : 2000
							},
							EndpointStyle : {
								radius : 0.01,
								fillStyle : color
							},
							HoverPaintStyle : {
								strokeStyle : "#ec9f2e"
							},
							EndpointHoverStyle : {
								fillStyle : "#ec9f2e"
							},
							Anchors : [ "BottomCenter", "TopCenter" ]
						});

						// declare some common values:
						jsP_arrowCommon = {
							foldback : 0.7,
							fillStyle : color,
							length : 7,
							width : 7
						};
						// use three-arg spec to create two different arrows with the common values:
						jsP_overlays = [ [ "Arrow", {
							location : 0.7,
							direction : 1
						}, jsP_arrowCommon ] ];
					} catch (err) {
						console.error(err);
					}
				}
			};

		})();

		try {
			//  This file contains the JS that handles the first init of each jQuery demonstration, and also switching
			//  between render modes.
			$wnd.jsPlumb.bind("ready", function() {
				// chrome fix.
				document.onselectstart = function() {
					return false;
				};
				$wnd.jsPlumbDemo.init();
				try {
					jsPlumbInstance.setSuspendDrawing(true);
					jsPlumbInstance.setContainer(containerId);
				} catch (err) {
					console.log(err);
				}
			});

		} catch (err) {
			console.error(err);
		}
	}-*/;

	/**
	 * Call after connecting divs.
	 */
	private static native void afterJSPlumbLoad() /*-{
		try {
			jsPlumbInstance.setSuspendDrawing(false, true);
		} catch (err) {
			console.log(err);
		}
	}-*/;

	@Override
	public void markOldVersions(List<String> notCurrentNodeIds) {
		for (String nodeId : notCurrentNodeIds) {
			ProvNodeContainer container = nodeToContainer.get(nodeId);
			if (container != null) {
				container.showMessage("<span class=\"small moveup-5\">(" + DisplayConstants.OLD_VERSION + ")</span>");
			}
		}
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
}
