package org.sagebionetworks.web.client.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceSource;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceSource.SourceType;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget.Species;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.core.shared.GWT;
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
			JiraURLHelper jiraErrorHelper, AuthenticationController authenticationController,
			BiodallianceWidget biodallianceWidget) {		
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
		String sourceName="A2_i14.mkdup.coordsort.bw";
		String styleType = "default";
		String styleGlyphType = "HISTOGRAM";
		String styleColor = "grey";
		int trackHeightPx = 120;
		BiodallianceSource source = new BiodallianceSource();
		source.configure(sourceName, "syn3928320", 1L, SourceType.BIGWIG);
		source.setStyle(styleType, styleGlyphType, styleColor, trackHeightPx);
		
		HashMap<String, String> wikiDescriptor = new HashMap<String, String>();
		wikiDescriptor.put(WidgetConstants.BIODALLIANCE_SPECIES_KEY, "HUMAN");
		wikiDescriptor.put(WidgetConstants.BIODALLIANCE_CHR_KEY, "1");
		wikiDescriptor.put(WidgetConstants.BIODALLIANCE_VIEW_START_KEY, "3025001");
		wikiDescriptor.put(WidgetConstants.BIODALLIANCE_VIEW_END_KEY, "3525001");
		String json = source.toJsonObject().toString();
		wikiDescriptor.put(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX+"0", json);
		
		//test configure based on wiki descriptor
		biodallianceWidget.configure(null, wikiDescriptor, null, null);
		biodallianceView.add(biodallianceWidget.asWidget());
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
