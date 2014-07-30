package org.sagebionetworks.web.client.widget.provenance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ExternalGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceWidgetViewImpl extends LayoutContainer implements ProvenanceWidgetView {
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private PortalGinInjector ginInjector;
	private ProvGraph graph;
	private LayoutContainer debug;
	private SynapseJSNIUtils synapseJSNIUtils;
	private HashMap<String,String> filledPopoverIds;
		
	private int height = WidgetConstants.PROV_WIDGET_HEIGHT_DEFAULT;
	private static final LayoutData TOP3_LEFT3 = new MarginData(3, 0, 0, 3);
	
	private LayoutContainer container;
	private LayoutContainer thisLayoutContainer;
	private Anchor fullScreenAnchor;
	private LayoutContainer prov;
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
		
		container = new LayoutContainer();
		this.thisLayoutContainer = this;
		
		createFullScreenButton(iconsImageBundle);	
		loadingContainer = new HTML(DisplayUtils.getLoadingHtml(sageImageBundle, "Loading provenance"));
	}

	@Override
	protected void onRender(Element parent, int index) {
		// TODO Auto-generated method stub
		super.onRender(parent, index);
		this.add(container);
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
		container.add(loadingContainer, TOP3_LEFT3);
		container.layout(true);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		container.removeAll();
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
		prov = new LayoutContainer();
		prov.setStyleAttribute("position", "relative");		
		prov.setHeight(height);				
		if(graph != null) {
			container.removeAll();			
			if(!inFullScreen) addFullScreenAnchor();			
			// add nodes to graph
			Set<ProvGraphNode> nodes = graph.getNodes();
			for(ProvGraphNode node : nodes) {							
				ProvNodeContainer container = getNodeContainer(node); 
				nodeToContainer.put(node.getId(), container);
				prov.add(container);
			}						
		}
		
		container.add(prov, new MarginData(5));
		this.addStyleName("scroll-auto");
		
		container.layout(true);
		if(graph != null) {			
			// make connections (assure DOM elements are in before asking jsPlumb to connect them)
			beforeJSPlumbLoad(prov.getId());
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
			addToolTipToContainer(node, container.getContent(), DisplayConstants.ENTITY);			
			return container;
		} else if(node instanceof ActivityGraphNode) {
			ProvNodeContainer container = ProvViewUtil.createActivityContainer((ActivityGraphNode)node, iconsImageBundle, ginInjector);
			// create tool tip for defined activities only
			if(((ActivityGraphNode) node).getType() == ActivityType.UNDEFINED) {
				addUndefinedToolTip(container);
			} else {
				addToolTipToContainer(node, container.getContent(), DisplayConstants.ACTIVITY);				
			}
			return container;
		} else if(node instanceof ExpandGraphNode) {
			return ProvViewUtil.createExpandContainer((ExpandGraphNode)node, sageImageBundle, presenter, this);
		} else if(node instanceof ExternalGraphNode) {			
			ProvNodeContainer container = ProvViewUtil.createExternalUrlContainer((ExternalGraphNode) node, iconsImageBundle);
			addToolTipToContainer(node, container.getContent(), DisplayConstants.EXTERNAL_URL);
			return container;
		}
		return null;
	}

	private void addToolTipToContainer(final ProvGraphNode node, final Component container, final String title) {					
		final PopupPanel popup = DisplayUtils.addToolTip(container, DisplayUtils.getLoadingHtml(sageImageBundle));
		container.addListener(Events.OnMouseOver, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {	
				// load the tooltip contents only once
				if(filledPopoverIds.containsKey(node.getId())) {															
					return;
				}															
				// retrieve info
				presenter.getInfo(node.getId(), new AsyncCallback<KeyValueDisplay<String>>() {						
					@Override
					public void onSuccess(KeyValueDisplay<String> result) {
						renderPopover(ProvViewUtil.createEntityPopoverHtml(result).asString());
					}
					
					@Override
					public void onFailure(Throwable caught) {
						renderPopover(DisplayConstants.DETAILS_UNAVAILABLE);						
					}
					
					private void renderPopover(String rendered) {
						filledPopoverIds.put(container.getId(), rendered);
						popup.setWidget(new HTML(rendered));						
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
				thisLayoutContainer.layout(true);
				
				// add it to window
				container.addStyleName("scroll-auto");				
				final Window window = new Window();
				// 90% h/w
				window.setSize(
						new Double(com.google.gwt.user.client.Window.getClientWidth() * .97).intValue(),
						new Double(com.google.gwt.user.client.Window.getClientHeight() * .97).intValue()); 
				window.setPlain(true);
				window.setModal(true);
				window.setHeaderVisible(true);
				window.setHeading(DisplayConstants.PROVENANCE);
				window.addListener(Events.OnClick, new Listener<BaseEvent>() {
					@Override
					public void handleEvent(BaseEvent be) {
						if(!blockCloseFullscreen) {
							window.hide();
						}
					}
				});
				window.addListener(Events.Hide, new Listener<ComponentEvent>() {
			        @Override
			        public void handleEvent(ComponentEvent be) {
			        	addFullScreenAnchor();
			        	container.removeStyleName("scroll-auto");
			        	thisLayoutContainer.add(container);
			        	container.setSize(prov.getWidth(), height); // reset height as window alters it 
			        	prov.setHeight(height);
			        	thisLayoutContainer.layout(true);
			        	inFullScreen = false;
			        }

			    });
				window.setLayout(new FitLayout());
				LayoutContainer white = new LayoutContainer(new FitLayout());
				white.addStyleName("whiteBackground");
				white.add(container, new FitData(4));
				window.add(white);				
				window.addButton(new Button(DisplayConstants.CLOSE, new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						window.hide();
					}
				}));
				window.setButtonAlign(HorizontalAlignment.RIGHT);
				inFullScreen = true;
				window.show();
			}
		});
	}
		
	private void addFullScreenAnchor() {
		container.insert(fullScreenAnchor, 0, new MarginData(1, 0, 0, 1));
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
				container.showMessage("<span class=\"small moveup-5\">(" + DisplayConstants.OLD_VERSION + ")</span>", DisplayConstants.THERE_IS_A_NEWER_VERSION);				
			}
		}
	}
	
	
}
