package org.sagebionetworks.web.client.widget.provenance;

import java.util.HashMap;
import java.util.Set;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
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
	private static final int PROVENANCE_HEIGHT_PX = 255;
	private static final int PROVENANCE_WIDTH_PX = 254;
	private static final LayoutData TOP3_LEFT3 = new MarginData(3, 0, 0, 3);
	
	private LayoutContainer container;
	private LayoutContainer thisLayoutContainer;
	private Anchor fullScreenAnchor;
	private LayoutContainer prov;
	private HTML loadingContainer;
	private boolean blockCloseFullscreen = false;
	private boolean inFullScreen = false;
	
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
		initJsPlumb();
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


	@Override
	public int getDefaultHeight() {
		return PROVENANCE_HEIGHT_PX;
	}

	
	/*
	 * Private Methods
	 */	
	private void createGraph() {
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
				prov.add(getNodeContainer(node));
			}						
		}
		
		container.add(prov, new MarginData(5));
		this.addStyleName("scroll-auto");
		container.layout(true);
		
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
			LayoutContainer container = ProvViewUtil.createActivityContainer((ActivityGraphNode)node, iconsImageBundle, ginInjector);
			// create tool tip for defined activities only
			if(((ActivityGraphNode) node).getType() == ActivityType.UNDEFINED) {
				addUndefinedToolTip(container);
			} else {
				addToolTipToContainer(node, container, DisplayConstants.ACTIVITY);				
			}
			return container;
		} else if(node instanceof ExpandGraphNode) {
			return ProvViewUtil.createExpandContainer((ExpandGraphNode)node, sageImageBundle, presenter, this);
		} else if(node instanceof ExternalGraphNode) {			
			LayoutContainer container = ProvViewUtil.createExternalUrlContainer((ExternalGraphNode) node, iconsImageBundle);
			addToolTipToContainer(node, container, DisplayConstants.EXTERNAL_URL);
			return container;
		}
		return null;
	}

	private void addToolTipToContainer(final ProvGraphNode node, final LayoutContainer container, final String title) {					
		//container.setToolTip(ProvViewUtil.createTooltipConfig(title, DisplayUtils.getLoadingHtml(sageImageBundle)));			
		container.addListener(Events.OnMouseOver, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {	
				// load the tooltip contents only once
				if(filledPopoverIds.containsKey(node.getId())) {															
//					container.setToolTip(ProvViewUtil.createTooltipConfig(title, filledPopoverIds.get(node.getId())));
					return;
				}															
				// retrieve info
				presenter.getInfo(node.getId(), new AsyncCallback<KeyValueDisplay<String>>() {						
					@Override
					public void onSuccess(KeyValueDisplay<String> result) {
						String rendered = ProvViewUtil.createEntityPopoverHtml(result).asString();
						filledPopoverIds.put(container.getId(), rendered);
					    //container.setToolTip(ProvViewUtil.createTooltipConfig(title, rendered));
						//DisplayUtils.addTooltipSpecial(synapseJSNIUtils, container, rendered, TOOLTIP_POSITION.RIGHT);
						container.setTitle(rendered);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						//container.setToolTip(ProvViewUtil.createTooltipConfig(title, DisplayConstants.ERROR_GENERIC_RELOAD));
						container.setTitle(DisplayConstants.ERROR_GENERIC_RELOAD);
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
				window.setModal(false);
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
	
	
}
