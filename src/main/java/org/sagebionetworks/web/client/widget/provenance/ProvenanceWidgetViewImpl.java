package org.sagebionetworks.web.client.widget.provenance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ExternalGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
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
	private HashMap<String,String> filledPopoverIds;
		
	private int height = WidgetConstants.PROV_WIDGET_HEIGHT_DEFAULT;
	private static final String TOP3_LEFT3 = "margin-left-3 margin-top-3";
	
	private FlowPanel container;
	private FlowPanel thisLayoutContainer;
	private Anchor fullScreenAnchor;
	private FlowPanel prov;
	private HTML loadingContainer;
	private boolean blockCloseFullscreen = false;
	private boolean inFullScreen = false;
	private Map<String,ProvNodeContainer> nodeToContainer;
	
	@Inject
	public ProvenanceWidgetViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils, PortalGinInjector ginInjector) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.ginInjector = ginInjector;
		
		container = new FlowPanel();
		this.thisLayoutContainer = this;
		this.add(container);
		createFullScreenButton(iconsImageBundle);	
		loadingContainer = new HTML(DisplayUtils.getLoadingHtml(sageImageBundle, "Loading provenance"));
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
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
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
		this.filledPopoverIds = new HashMap<String,String>();
		blockCloseFullscreen = false;
		prov = new FlowPanel();
		prov.addStyleName("position-relative margin-5");
		refreshProvHeight();
						
		if(graph != null) {
			container.clear();			
			if(!inFullScreen) addFullScreenAnchor();			
			// add nodes to graph
			Set<ProvGraphNode> nodes = graph.getNodes();
			for(ProvGraphNode node : nodes) {							
				ProvNodeContainer container = getNodeContainer(node); 
				nodeToContainer.put(node.getId(), container);
				prov.add(container);
			}						
		}
		
		container.add(prov);
		this.addStyleName("scroll-auto");
		
		if(graph != null) {			
			// make connections (assure DOM elements are in before asking jsPlumb to connect them)
			beforeJSPlumbLoad(prov.getElement().getId());
			Set<ProvGraphEdge> edges = graph.getEdges();
			for(ProvGraphEdge edge : edges) {
				connect(edge.getSink().getId(), edge.getSource().getId());
			}

			// look for old versions
			presenter.findOldVersions();
			afterJSPlumbLoad();
		}
	}
	
	/**
	 * Create an actual LayoutContainer rendering of the node
	 * @param node
	 * @return
	 */
	private ProvNodeContainer getNodeContainer(final ProvGraphNode node) {
		if(node instanceof EntityGraphNode) {
			ProvNodeContainer container = ProvViewUtil.createEntityContainer((EntityGraphNode)node, iconsImageBundle);
			addToolTipToContainer(node, container, DisplayConstants.ENTITY);			
			return container;
		} else if(node instanceof ActivityGraphNode) {
			ProvNodeContainer container = ProvViewUtil.createActivityContainer((ActivityGraphNode)node, iconsImageBundle, ginInjector);
			// create tool tip for defined activities only
			if(((ActivityGraphNode) node).getType() != ActivityType.UNDEFINED) {
				addToolTipToContainer(node, container, DisplayConstants.ACTIVITY);				
			}
			return container;
		} else if(node instanceof ExpandGraphNode) {
			return ProvViewUtil.createExpandContainer((ExpandGraphNode)node, sageImageBundle, presenter, this);
		} else if(node instanceof ExternalGraphNode) {			
			ProvNodeContainer container = ProvViewUtil.createExternalUrlContainer((ExternalGraphNode) node, iconsImageBundle);
			addToolTipToContainer(node, container, DisplayConstants.EXTERNAL_URL);
			return container;
		}
		return null;
	}

	private void addToolTipToContainer(final ProvGraphNode node, final ProvNodeContainer nodeContainer, final String title) {
		nodeContainer.setupTooltip(DisplayUtils.getLoadingHtml(sageImageBundle));
		final HandlerRegistration mouseOverRegistration = nodeContainer.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				node.setShowingTooltip(true);
				if(filledPopoverIds.containsKey(node.getId())) {
					return;
				}  
				// retrieve info
				presenter.getInfo(node.getId(), new AsyncCallback<KeyValueDisplay<String>>() {						
					@Override
					public void onSuccess(KeyValueDisplay<String> result) {
						String popoverHtml = ProvViewUtil.createEntityPopoverHtml(result).asString();
						if (!DisplayUtils.isDefined(popoverHtml))
							popoverHtml = DisplayConstants.DETAILS_UNAVAILABLE;
						renderPopover(popoverHtml);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						renderPopover(DisplayConstants.DETAILS_UNAVAILABLE);						
					}
					
					private void renderPopover(final String rendered) {
						filledPopoverIds.put(container.getElement().getId(), rendered);
						Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
							 @Override
							public void execute() {
								boolean isShowingTooltip = node.isShowingTooltip();
								nodeContainer.setupTooltip(rendered);
								if (isShowingTooltip)
									nodeContainer.showTooltip();
							}
						 });
					}
				});
			}
		});
		
		nodeContainer.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				node.setShowingTooltip(false);
				mouseOverRegistration.removeHandler();
			}
		});
	}
	
	private static native void connect(String parentId, String childId) /*-{
		jsPlumbInstance.connect({source:parentId, target:childId, overlays:jsP_overlays	});
	}-*/;
	
	/**
	 * Call before connecting divs. Suspends drawing graph until bulk operation is complete (call afterJSPlumbLoad)
	 * @param parentContainerId
	 */
	private static native void beforeJSPlumbLoad(String containerId) /*-{
		;(function() {
			$wnd.jsPlumbDemo = {					
				init : function() {					
					var color = "gray";
					jsPlumbInstance = $wnd.jsPlumb.getInstance();		
					jsPlumbInstance.importDefaults({
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
			$wnd.jsPlumbDemo.init();
			jsPlumbInstance.setRenderMode($wnd.jsPlumb.SVG);
			jsPlumbInstance.setSuspendDrawing(true);
			jsPlumbInstance.setContainer(containerId);
		});
		
		
	}-*/;
	
	/**
	 * Call after connecting divs.
	 */
	private static native void afterJSPlumbLoad() /*-{
		jsPlumbInstance.setSuspendDrawing(false, true);
	}-*/;
	
		
	private void createFullScreenButton(IconsImageBundle iconsImageBundle) {
		fullScreenAnchor = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.fullScreen16())));
		//fullSizeButton.setStyleName("z-index-10");
		fullScreenAnchor.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				// remove graph from on page container
				thisLayoutContainer.remove(container);
				container.remove(fullScreenAnchor);
				
				// add it to window
				container.addStyleName("scroll-auto");				
				final Modal window = new Modal();
				window.addStyleName("modal-fullscreen");
				window.setTitle(DisplayConstants.PROVENANCE);
				final ModalBody body = new ModalBody();
				body.add(container);
				ClickHandler closeHandler = new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						addFullScreenAnchor();
			        	container.removeStyleName("scroll-auto");
			        	body.remove(container);
			        	thisLayoutContainer.add(container);
			        	inFullScreen = false;
			        	refreshProvHeight();
			        	window.hide();
					}
				};
				window.addCloseHandler(closeHandler);
				window.add(body);
				ModalFooter footer = new ModalFooter();
				Button closeButton = new Button(DisplayConstants.CLOSE,closeHandler);
				footer.add(closeButton);
				
				window.add(footer);
				inFullScreen = true;
				refreshProvHeight();
				window.show();
			}
		});
	}
	public void refreshProvHeight() {
		if (prov != null) {
			if (inFullScreen) {
				prov.setHeight(new Double(com.google.gwt.user.client.Window.getClientHeight() * .97).intValue() + "px");
			} else {
				prov.setHeight(height + "px");
			}
		}
	}
	private void addFullScreenAnchor() {
		fullScreenAnchor.addStyleName("margin-top-1 margin-left-1");
		container.insert(fullScreenAnchor, 0);
	}

	@Override
	public void setBlockCloseFullscreen(boolean blockClose) {
		blockCloseFullscreen = blockClose;
	}

	@Override
	public void markOldVersions(List<String> notCurrentNodeIds) {
		for(String nodeId : notCurrentNodeIds) {
			ProvNodeContainer container = nodeToContainer.get(nodeId);
			if(container != null) {
				container.showMessage("<span class=\"small moveup-5\">(" + DisplayConstants.OLD_VERSION + ")</span>");				
			}
		}
	}
	
	
}
