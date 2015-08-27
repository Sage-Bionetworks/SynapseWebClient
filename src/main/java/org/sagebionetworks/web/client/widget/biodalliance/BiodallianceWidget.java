package org.sagebionetworks.web.client.widget.biodalliance;

import java.util.Date;

import org.gwtvisualizationwrappers.client.biodalliance.Biodalliance013dev;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BiodallianceWidget implements BiodallianceWidgetView.Presenter, IsWidget{

	public static final String FILE_RESOLVER_URL="Portal/" + WebConstants.FILE_ENTITY_RESOLVER_SERVLET+"?";
	
	BiodallianceWidgetView view;
	AuthenticationController authenticationController;
	GlobalApplicationState globalApplicationState;
	HumanBiodallianceConfig humanConfig; 
	MouseBiodallianceConfig mouseConfig;
	@Inject
	public BiodallianceWidget(BiodallianceWidgetView view, 
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			HumanBiodallianceConfig humanConfig, 
			MouseBiodallianceConfig mouseConfig) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.humanConfig = humanConfig;
		this.mouseConfig = mouseConfig;
		view.setPresenter(this);
	}
	
	public static String getFileResolverURL(String entityIdAndVersion) {
		StringBuilder sb = new StringBuilder();
		if (entityIdAndVersion != null) {
			String[] tokens = entityIdAndVersion.split("\\.");
			if (tokens.length == 2) {
				sb.append(FILE_RESOLVER_URL);
				sb.append("entityId=");
				sb.append(tokens[0]);
				sb.append("&version=");
				sb.append(tokens[1]);
			}
		}
		return sb.toString();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void viewAttached() {
		//init
		String initChr = "21";
	    int initViewStart = 33031597;
	    int initViewEnd = 33041570;
	    String baseColor = "black";
	    String deletionColor = "black";
	    String insertionColor = "red";
	    
		JavaScriptObject config = createNewBiodallianceBrowserConfig(initChr, initViewStart, initViewEnd, baseColor, deletionColor, insertionColor, humanConfig);
		
		//add a source
		String sourceName="A2_i14.mkdup.coordsort.bw";
		String sourceBwgURI = FILE_RESOLVER_URL + "entityId=syn3928320&version=1"; //'case/A2_i14.mkdup.coordsort.bw',
		String styleType = "default";
		String styleGlyphType = "HISTOGRAM";
		String styleColor = "red";
		int trackHeightPx = 120;
		BiodallianceSource source = new BiodallianceSource(sourceName, "syn3928320", 1L, styleType, styleGlyphType, styleColor, trackHeightPx);
		addBigwigSource(config, source);
		new Biodalliance013dev().show(config);
	}
	
	private JavaScriptObject createNewBiodallianceBrowserConfig(
			String initChr,
			int initViewStart,
			int initViewEnd,
			String baseColor,
			String deletionColor,
			String insertionColor,
			BiodallianceConfigInterface config
			) {
		long uniqueId = new Date().getTime();
		String containerId = "biodallianceContainerId"+uniqueId;
		view.setContainerId(containerId);
		
		return createNewBiodallianceBrowserConfig(containerId, initChr, initViewStart, initViewEnd, config.getTwoBitURI(),
				config.getBwgURI(), config.getStylesheetURI(), config.getTrixURI(), config.getSpeciesName(), config.getTaxon(), config.getAuthName(),
				config.getVersion(), config.getUscsName(), baseColor, deletionColor, insertionColor);
	}
	
	private native JavaScriptObject createNewBiodallianceBrowserConfig(
			String containerId,
			String initChr,
			int initViewStart,
			int initViewEnd,
			String genomeFileURI,
			String gencodeBBFileURI,
			String gencodeXMLFileURI, //stylesheet
			String gencodeIndexFileURI,
			String coordSystemSpeciesName,
			int coordSystemTaxon,
			String coordSystemAuth,
			String coordSystemVersion,
			String coordSystemUcscName,
			String baseColor,
			String deletionColor,
			String insertionColor
			) /*-{
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
				chr: initChr, 
				viewStart:  initViewStart, 
				viewEnd:  initViewEnd, 
				cookieKey: coordSystemSpeciesName, 
				fullScreen: true,
				coordSystem: { 
					speciesName: coordSystemSpeciesName, 
					taxon: coordSystemTaxon, 
					auth: coordSystemAuth, 
					version: coordSystemVersion, 
					ucscName: coordSystemUcscName},
				baseColors: {
	                 'A': baseColor,
	                 'C': baseColor,
	                 'G': baseColor,
	                 'T': baseColor,
	                 '-': deletionColor, //deletion
	                 'I': insertionColor    //insertion
	            },
				sources: [{name: 'Genome',
					twoBitURI: genomeFileURI,
					tier_type: 'sequence',
					provides_entrypoints: true,
					pinned: true,
					resolver: resolverFunction}, 

					{name: 'GENCODE',
					bwgURI: gencodeBBFileURI,
					stylesheet_uri: gencodeXMLFileURI,
					collapseSuperGroups: true, 
					trixURI: gencodeIndexFileURI,
					subtierMax:5,
					pinned:true,
					resolver: resolverFunction
					}]
		};
		return biodallianceBrowserConfig;
	}-*/;
	
	private void addBigwigSource(
			JavaScriptObject biodallianceBrowserConfig,
			BiodallianceSource source) {
		addBigwigSource(biodallianceBrowserConfig, source.getSourceName(), source.getSourceBwgURI(), source.getStyleType(), source.getStyleGlyphType(), source.getStyleColor(), source.getTrackHeightPx());
	}
	private native void addBigwigSource(
			JavaScriptObject biodallianceBrowserConfig,
			String sourceName,
			String sourceBwgURI,
			String styleType, 
			String styleGlyphType,
			String styleColor,
			int trackHeightPx
			) /*-{
		var resolverFunction = function(url) {
		   return fetch(url, {  
			  credentials: 'include'  //sending credentials with a fetch request (session cookie)
			}).then(function(resp) {
		       return resp.json();
		   }).then(function(rdata) {
		       return rdata.url;
		   });
		}
	    var newSource = {
	    	name: sourceName,
			collapseSuperGroups:true,
			bwgURI: sourceBwgURI,
			style: [{type : styleType,
					style: {glyph: styleGlyphType,
							COLOR1:styleColor,
							COLOR2:styleColor,
							COLOR3:styleColor,
							HEIGHT:trackHeightPx}}],
			collapseSuperGroups: true, 
			resolver: resolverFunction
	    }
	    biodallianceBrowserConfig.sources.push(newSource);
}-*/;
	
}
