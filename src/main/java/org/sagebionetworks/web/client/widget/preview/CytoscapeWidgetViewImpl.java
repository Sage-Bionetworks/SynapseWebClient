package org.sagebionetworks.web.client.widget.preview;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.resources.ResourceLoader;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CytoscapeWidgetViewImpl extends LayoutContainer implements CytoscapeWidgetView {

	private static final LayoutData TOP3_LEFT3 = new MarginData(3, 0, 0, 3);
	private static int sequence = 0;
	private Presenter presenter;
	private ResourceLoader resourceLoader;
	private LayoutContainer container;
	private HTML loadingContainer;
	private String graphJson;
	
	@Inject
	public CytoscapeWidgetViewImpl(ResourceLoader resourceLoader, SageImageBundle sageImageBundle) {
		this.resourceLoader = resourceLoader;		
		container = new LayoutContainer();
		container.setId(getNewId());
		container.addStyleName("slider-area-inner");
		container.setHeight(500);
		container.setWidth(500);
		this.add(container);
		
		loadingContainer = new HTML(DisplayUtils.getLoadingHtml(sageImageBundle, "Loading Cytoscape"));
		graphJson = null;
	}

	@Override
	public void configure(String graphJson) {
		this.graphJson = graphJson;
		final CytoscapeWidgetViewImpl self = this;
	}	

	@Override
	public void onRender(Element parent, int index) {
		super.onRender(parent, index);	
		// load Cytoscape Javascript
		resourceLoader.requires(ClientProperties.CYTOSCAPE_JS, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				initCytoscape(container.getId());
				//buildGraph();
			}
			@Override
			public void onFailure(Throwable caught) {
			}
		});		
	}

	private static native void initCytoscape(String containerId) /*-{
		
		$wnd.jQuery('#'+containerId).cytoscape({
		  
		  elements: {
		    nodes: [
		      { data: { id: 'j', name: 'Jerry' } },
		      { data: { id: 'e', name: 'Elaine' } }
		    ],
		    edges: [
		      { data: { source: 'j', target: 'e' } }
		    ]
		  },
		  
		  ready: function(){
		    $wnd.cy = this;
		    
		    cy.elements().unselectify();
		    
		    cy.on('tap', 'node', function(e){
		      var node = e.cyTarget; 
		      var neighborhood = node.neighborhood().add(node);
		      
		      cy.elements().addClass('faded');
		      neighborhood.removeClass('faded');
		    });
		    
		    cy.on('tap', function(e){
		      if( e.cyTarget === cy ){
		        cy.elements().removeClass('faded');
		      }
		    });
		  }
		});



	}-*/;

	private void buildGraph() {	
		if(graphJson != null) {
//			JSONValue graph = JSONParser.parseStrict(graphJson);
//			if(graph.isObject() == null) {
//				// bad json format
//				showErrorMessage(DisplayConstants.ERROR_LOADING_CYTOSCAPE);
//				return;
//			}
			// send graph to cytoscape
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
	public void showLoading() {
		container.add(loadingContainer, TOP3_LEFT3);
		container.layout(true);
	}

	@Override
	public void clear() {
		container.removeAll();
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		this.removeAll();
		HTML alert = new HTML();
		alert.addStyleName("alert");
		alert.setText(message);
		this.add(alert);
		this.layout();
	}

	/*
	 * Private Methods
	 */
	private String getNewId() {
		return "elems" + ++sequence;
	}
	
}
