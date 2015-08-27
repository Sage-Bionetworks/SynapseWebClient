package org.sagebionetworks.web.client.widget.biodalliance;

import java.util.Date;

import org.gwtvisualizationwrappers.client.biodalliance.Biodalliance013dev;
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
	
	@Inject
	public BiodallianceWidget(BiodallianceWidgetView view, AuthenticationController authenticationController) {
		this.view = view;
		this.authenticationController = authenticationController;
		view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void viewAttached() {
		//init
		long uniqueId = new Date().getTime();
		String containerId = "biodallianceContainerId"+uniqueId;
		view.setContainerId(containerId);
		String initChr = "21";
	    int initViewStart = 33031597;
	    int initViewEnd = 33041570;
	    String genomeFileURI = FILE_RESOLVER_URL + "entityId=syn4557603&version=1";
	    String gencodeBBFileURI = FILE_RESOLVER_URL + "entityId=syn4557576&version=1";
	    String gencodeXMLFileURI = FILE_RESOLVER_URL + "entityId=syn4557577&version=1";
	    String gencodeIndexFileURI = FILE_RESOLVER_URL + "entityId=syn455757&version=1";
	    String coordSystemSpeciesName = "human";
	    int coordSystemTaxon = 9606;
	    String coordSystemAuth = "NCBI";
	    String coordSystemVersion = "37";
	    String coordSystemUcscName = "hg19";
	    String baseColor = "black";
	    String deletionColor = "black";
	    String insertionColor = "red";
		JavaScriptObject config = createNewBiodallianceBrowserConfig(containerId, initChr, initViewStart, initViewEnd, genomeFileURI, gencodeBBFileURI, gencodeXMLFileURI, gencodeIndexFileURI, 
				coordSystemSpeciesName, coordSystemTaxon, coordSystemAuth, coordSystemVersion, coordSystemUcscName, baseColor, deletionColor, insertionColor);
		
		
		//add a source
		String sourceName="A2_i14.mkdup.coordsort.bw";
		String sourceBwgURI = FILE_RESOLVER_URL + "entityId=syn3928320&version=1"; //'case/A2_i14.mkdup.coordsort.bw',
		String styleType = "default";
		String styleGlyphType = "HISTOGRAM";
		String styleColor = "red";
		int trackHeightPx = 120;
		String sourceStylesheetURI = null;
		String sourceTrixURI = null;
		addBigwigSource(config, sourceName, sourceBwgURI, sourceStylesheetURI, sourceTrixURI, styleType, styleGlyphType, styleColor, trackHeightPx);
		new Biodalliance013dev().show(config);
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
	
	private native void addBigwigSource(
			JavaScriptObject biodallianceBrowserConfig,
			String sourceName,
			String sourceBwgURI,
			String sourceStylesheetURI,
			String sourceTrixURI,
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
			stylesheet_uri: sourceStylesheetURI,	
			collapseSuperGroups: true, 
			trixURI: sourceTrixURI,
			resolver: resolverFunction
	    }
	    biodallianceBrowserConfig.sources.push(newSource);
}-*/;
	
}
