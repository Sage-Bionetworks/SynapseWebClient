package org.sagebionetworks.web.client.widget.provenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.provenance.ActivityTreeNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.EntityTreeNode;
import org.sagebionetworks.web.shared.provenance.ExpandTreeNode;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceWidgetViewImpl extends LayoutContainer implements ProvenanceWidgetView {
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;		
	private ProvTreeNode tree;
	private List<Connection> connections;
	private LayoutContainer debug;
	private SynapseJSNIUtils synapseJSNIUtils;
	private HashMap<String,String> filledPopoverIds;
	
	private static final int DEFAULT_HEIGHT_PX = 275;
	private int height = DEFAULT_HEIGHT_PX;
	
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
	public void setTree(ProvTreeNode root) {
		this.tree = root;
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
		
		if(tree != null) {			
			addNodeTree(tree, prov);
		}
		
		this.add(prov, new MarginData(5));
		this.addStyleName("scroll-auto");
		this.layout(true);
		
		// assure DOM elements are in before asking jsPlumb to connect them
		for(Connection connection : connections) {
			connect(connection.getParentId(), connection.getChildId());
		}
	}
	
	/**
	 * Add node and it's children to the tree
	 * @param root
	 * @param prov
	 */
	private void addNodeTree(ProvTreeNode root, LayoutContainer prov) {
		if(root == null) return;
		Anchor a = new Anchor();
		Hyperlink h = new Hyperlink();		
		prov.add(getNodeContainer(root));
		Iterator<ProvTreeNode> itr = root.iterator();
		while(itr.hasNext()) {
			ProvTreeNode child = itr.next();
			addNodeTree(child, prov);
			connections.add(new Connection(root.getId(), child.getId()));
		}
	}
	
	/**
	 * Create an actual LayoutContainer rendering of the node
	 * @param node
	 * @return
	 */
	private LayoutContainer getNodeContainer(final ProvTreeNode node) {
		if(node instanceof EntityTreeNode) {
			LayoutContainer container = ProvViewUtil.createEntityContainer((EntityTreeNode)node, iconsImageBundle);
			addToolTipToContainer(node, container, DisplayConstants.ENTITY);			
			return container;
		} else if(node instanceof ActivityTreeNode) {
			LayoutContainer container = ProvViewUtil.createActivityContainer((ActivityTreeNode)node, iconsImageBundle);
			// create tool tip for defined activities only
			if(((ActivityTreeNode) node).getType() == ActivityType.UNDEFINED) {
				addUndefinedToolTip(container);
			} else {
				addToolTipToContainer(node, container, DisplayConstants.ACTIVITY);				
			}
			return container;
		} else if(node instanceof ExpandTreeNode) {
			return ProvViewUtil.createExpandContainer((ExpandTreeNode)node, sageImageBundle);
		}
		return null;
	}

	private void addToolTipToContainer(final ProvTreeNode node, final LayoutContainer container, final String title) {		
		container.setToolTip(ProvViewUtil.createTooltipConfig(title, DisplayUtils.getLoadingHtml(sageImageBundle)));			
		container.addListener(Events.OnMouseOver, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {	
				// load the tooltip contents only once
				if(filledPopoverIds.containsKey(container.getId())) {															
					container.setToolTip(ProvViewUtil.createTooltipConfig(title, filledPopoverIds.get(container.getId())));
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
		jsPlumb.connect({source:parentId, target:childId, overlays:$wnd.overlays});
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
						Anchors :  [ "TopCenter", "BottomCenter" ]
					});				
						
					// declare some common values:
					var arrowCommon = { foldback:0.7, fillStyle:color, length:10, width:10 },
						// use three-arg spec to create two different arrows with the common values:
						overlays = [
							[ "Arrow", { location:0.001, direction:-1 }, arrowCommon ]
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
