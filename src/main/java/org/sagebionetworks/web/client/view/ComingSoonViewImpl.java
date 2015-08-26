package org.sagebionetworks.web.client.view;

import org.gwtvisualizationwrappers.client.biodalliance.Biodalliance013dev;
import org.gwtvisualizationwrappers.client.cytoscape.CytoscapeGraph242;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ComingSoonViewImpl extends Composite implements ComingSoonView {

	public interface ComingSoonViewImplUiBinder extends UiBinder<Widget, ComingSoonViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel biodallianceView;
	
	@UiField
	SimplePanel cytoscapeView;
	
	private Presenter presenter;
	private IconsImageBundle icons;
	private Header headerWidget;
	private Footer footerWidget;
	ProvenanceWidget provenanceWidget;
	SynapseJSNIUtils synapseJSNIUtils;
	JiraURLHelper jiraErrorHelper;
	SynapseClientAsync synapseClient;
	AuthenticationController authenticationController;
	
	@Inject
	public ComingSoonViewImpl(ComingSoonViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, IconsImageBundle icons,
			SynapseClientAsync synapseClient,
			SageImageBundle sageImageBundle, SynapseJSNIUtils synapseJSNIUtils, ProvenanceWidget provenanceWidget,
			PortalGinInjector ginInjector,
			JiraURLHelper jiraErrorHelper, AuthenticationController authenticationController ) {		
		initWidget(binder.createAndBindUi(this));

		this.icons = icons;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.synapseClient = synapseClient;
		this.jiraErrorHelper = jiraErrorHelper;
		this.provenanceWidget = provenanceWidget;
		this.authenticationController = authenticationController;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());	
//		cytoscapeView.addAttachHandler(new AttachEvent.Handler() {
//			@Override
//			public void onAttachOrDetach(AttachEvent event) {
//				if (event.isAttached()) {
//					String id = "cy1";
//					cytoscapeView.getElement().setId(id);
//					//cyjs is exported from Cytoscape via File->Export->Network->Cytoscape.js JSON (.cyjs)
//					String cyjs = CYJS;
//					//styleJson is exported from Cytoscape via File->Export->Style->style for Cytoscape.js (JSON)
//					String styleJson = null; //set to null to avoid checking in large test constant
//					new CytoscapeGraph242().show(id,  cyjs, styleJson);
//				};
//			}
//		});

		biodallianceView.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					String id = "biodallianceId";
					biodallianceView.getElement().setId(id);
					new Biodalliance013dev().show(getBiodallianceBrowserConfig(id));
				};
			}
		});
		
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		//provenanceWidget.setHeight(400);
//		((LayoutContainer)provenanceWidget.asWidget()).setAutoHeight(true);
		
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
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
	
	private native JavaScriptObject getBiodallianceBrowserConfig(String containerId) /*-{
		var resolverFunction = function(url) {
                       return fetch(url, {  
						  credentials: 'include'  //sending credentials with a fetch request (session cookie)
						}).then(function(resp) {
                           return resp.json();
                       }).then(function(rdata) {
                           return rdata.url;
                       });
                    }
		var biodallianceBrowserConfig = {
				pageName: containerId,
				chr: '21', 
				viewStart:  33031597, 
				viewEnd:  33041570, 
				cookieKey: 'human', 
				fullScreen: true,
				coordSystem: { 
					speciesName: 'human', 
					taxon: 9606, 
					auth: 'NCBI', 
					version: '37', 
					ucscName: 'hg19'},
				baseColors: {
	                 'A': 'black',
	                 'C': 'black',
	                 'G': 'black',
	                 'T': 'black',
	                 '-': 'black', //deletion
	                 'I': 'red'    //insertion
	            },
				sources: 
					[{name: 'Genome',
					twoBitURI: 'Portal/fileresolver?entityId=syn4557603&version=1',
					tier_type: 'sequence',
					provides_entrypoints: true,
					pinned: true,
					resolver: resolverFunction}, 

					{name: 'GENCODE',
					bwgURI: 'Portal/fileresolver?entityId=syn4557576&version=1', //'human/gencode.bb',
					stylesheet_uri: 'Portal/fileresolver?entityId=syn4557577&version=1',//'human/gencode.xml',	
					collapseSuperGroups: true, 
					trixURI: 'Portal/fileresolver?entityId=syn455757&version=1',//'human/geneIndex.ix',
					subtierMax:5,
					pinned:true,
					resolver: resolverFunction
					},
					{name: 'A2_i14.mkdup.coordsort.bw',
						collapseSuperGroups:true,
						bwgURI: 'Portal/fileresolver?entityId=syn3928320&version=1',//'case/A2_i14.mkdup.coordsort.bw',
						style: [{type : 'default',
								style: {glyph: 'HISTOGRAM',
										COLOR1:'blue',
										COLOR2:'blue',
										COLOR3:'blue',
										HEIGHT:100}}],
						resolver: resolverFunction
					}]
				};
		return biodallianceBrowserConfig;
	}-*/;
	

	
	private static final String CYJS="{\r\n" + 
			"  \"format_version\" : \"1.0\",\r\n" + 
			"  \"generated_by\" : \"cytoscape-3.2.1\",\r\n" + 
			"  \"target_cytoscapejs_version\" : \"~2.1\",\r\n" + 
			"  \"data\" : {\r\n" + 
			"    \"selected\" : true,\r\n" + 
			"    \"__Annotations\" : [ ],\r\n" + 
			"    \"shared_name\" : \"TeamExample-UACC812-Network-Prior.sif\",\r\n" + 
			"    \"SUID\" : 52,\r\n" + 
			"    \"name\" : \"TeamExample-UACC812-Network-Prior.sif\"\r\n" + 
			"  },\r\n" + 
			"  \"elements\" : {\r\n" + 
			"    \"nodes\" : [ {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"128\",\r\n" + 
			"        \"shared_name\" : \"GSK3-alpha-beta_pS21_S9\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"SUID\" : 128,\r\n" + 
			"        \"name\" : \"GSK3-alpha-beta_pS21_S9\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"115\",\r\n" + 
			"        \"shared_name\" : \"Src_pY527\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"SUID\" : 115,\r\n" + 
			"        \"name\" : \"Src_pY527\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"113\",\r\n" + 
			"        \"shared_name\" : \"Src_pY416\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"SUID\" : 113,\r\n" + 
			"        \"name\" : \"Src_pY416\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"111\",\r\n" + 
			"        \"shared_name\" : \"S6_pS240_S244\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"SUID\" : 111,\r\n" + 
			"        \"name\" : \"S6_pS240_S244\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"107\",\r\n" + 
			"        \"shared_name\" : \"Rb_pS807_S811\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"SUID\" : 107,\r\n" + 
			"        \"name\" : \"Rb_pS807_S811\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"106\",\r\n" + 
			"        \"shared_name\" : \"PRAS40_pT246\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"SUID\" : 106,\r\n" + 
			"        \"name\" : \"PRAS40_pT246\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"92\",\r\n" + 
			"        \"shared_name\" : \"STAT3_pY705\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"SUID\" : 92,\r\n" + 
			"        \"name\" : \"STAT3_pY705\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"90\",\r\n" + 
			"        \"shared_name\" : \"GSK3-alpha-beta_pS9\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"SUID\" : 90,\r\n" + 
			"        \"name\" : \"GSK3-alpha-beta_pS9\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"83\",\r\n" + 
			"        \"shared_name\" : \"CHK1_pS345\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"SUID\" : 83,\r\n" + 
			"        \"name\" : \"CHK1_pS345\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"76\",\r\n" + 
			"        \"shared_name\" : \"MAPK_pT202_Y204\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"SUID\" : 76,\r\n" + 
			"        \"name\" : \"MAPK_pT202_Y204\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"67\",\r\n" + 
			"        \"shared_name\" : \"ACC_pS79\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"SUID\" : 67,\r\n" + 
			"        \"name\" : \"ACC_pS79\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"63\",\r\n" + 
			"        \"shared_name\" : \"BAD_pS112\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"SUID\" : 63,\r\n" + 
			"        \"name\" : \"BAD_pS112\"\r\n" + 
			"      }\r\n" + 
			"    } ],\r\n" + 
			"    \"edges\" : [ {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"116\",\r\n" + 
			"        \"source\" : \"115\",\r\n" + 
			"        \"target\" : \"92\",\r\n" + 
			"        \"interaction\" : \"1\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"shared_interaction\" : \"1\",\r\n" + 
			"        \"shared_name\" : \"Src_pY527 (1) STAT3_pY705\",\r\n" + 
			"        \"SUID\" : 116,\r\n" + 
			"        \"name\" : \"Src_pY527 (1) STAT3_pY705\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"114\",\r\n" + 
			"        \"source\" : \"111\",\r\n" + 
			"        \"target\" : \"113\",\r\n" + 
			"        \"interaction\" : \"1\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"shared_interaction\" : \"1\",\r\n" + 
			"        \"shared_name\" : \"S6_pS240_S244 (1) Src_pY416\",\r\n" + 
			"        \"SUID\" : 114,\r\n" + 
			"        \"name\" : \"S6_pS240_S244 (1) Src_pY416\"\r\n" + 
			"      }\r\n" + 
			"    }, {\r\n" + 
			"      \"data\" : {\r\n" + 
			"        \"id\" : \"108\",\r\n" + 
			"        \"source\" : \"106\",\r\n" + 
			"        \"target\" : \"107\",\r\n" + 
			"        \"interaction\" : \"1\",\r\n" + 
			"        \"selected\" : false,\r\n" + 
			"        \"shared_interaction\" : \"1\",\r\n" + 
			"        \"shared_name\" : \"PRAS40_pT246 (1) Rb_pS807_S811\",\r\n" + 
			"        \"SUID\" : 108,\r\n" + 
			"        \"name\" : \"PRAS40_pT246 (1) Rb_pS807_S811\"\r\n" + 
			"      }\r\n" + 
			"    } ]\r\n" + 
			"  }\r\n" + 
			"}";
}
