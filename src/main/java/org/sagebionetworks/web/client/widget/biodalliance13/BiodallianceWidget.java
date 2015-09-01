package org.sagebionetworks.web.client.widget.biodalliance13;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gwtvisualizationwrappers.client.biodalliance.Biodalliance013dev;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BiodallianceWidget implements BiodallianceWidgetView.Presenter, IsWidget, WidgetRendererPresenter{

	public static final String FILE_RESOLVER_URL="Portal/" + WebConstants.FILE_ENTITY_RESOLVER_SERVLET+"?";
	
	public enum Species {
		HUMAN, MOUSE
	}
	
	public static final Species DEFAULT_SPECIES= Species.HUMAN;
	public static final String DEFAULT_CHR= "1";
	public static final int DEFAULT_VIEW_START = 3025001;
	public static final int DEFAULT_VIEW_END = 3525001;
	
	BiodallianceWidgetView view;
	AuthenticationController authenticationController;
	GlobalApplicationState globalApplicationState;
	HumanBiodallianceConfig humanConfig; 
	MouseBiodallianceConfig mouseConfig;
	boolean isConfigured;
	BiodallianceConfigInterface currentConfig;
	String initChr;
	int initViewStart, initViewEnd;
	List<BiodallianceSource> sources;
	
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
		isConfigured = false;
		view.setPresenter(this);
	}
	
	public void configure(Species species, 
			String initChr,
			int initViewStart,
			int initViewEnd, 
			List<BiodallianceSource> sources) {
		this.initChr = initChr;
	    this.initViewStart = initViewStart;
	    this.initViewEnd = initViewEnd;
	    this.sources = sources;
		if (Species.HUMAN.equals(species)) {
			currentConfig = humanConfig;
		} else if (Species.MOUSE.equals(species)) {
			currentConfig = mouseConfig;
		}
		
		isConfigured = true;
		//if view is already attached, then show the browser
		if (view.isAttached()) {
			showBiodallianceBrowser();
		}
	}
	
	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> descriptor,
			Callback widgetRefreshRequired, Long wikiVersionInView) {
		//get values from descriptor (params map) and pass to other configure.
		Species species = DEFAULT_SPECIES;
		
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_SPECIES_KEY)){
			species = Species.valueOf(descriptor.get(WidgetConstants.BIODALLIANCE_SPECIES_KEY));
		}
		
		String chr = DEFAULT_CHR;
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_CHR_KEY)){
			chr = descriptor.get(WidgetConstants.BIODALLIANCE_CHR_KEY);
		}
		
		int viewStart = DEFAULT_VIEW_START;
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_VIEW_START_KEY)){
			viewStart = Integer.parseInt(descriptor.get(WidgetConstants.BIODALLIANCE_VIEW_START_KEY));
		}
		
		int viewEnd = DEFAULT_VIEW_END;
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_VIEW_END_KEY)){
			viewEnd = Integer.parseInt(descriptor.get(WidgetConstants.BIODALLIANCE_VIEW_END_KEY));
		}
		
		List<BiodallianceSource> sources = new ArrayList<BiodallianceSource>();
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + 0)){
			//discover all sources
			sources.addAll(getSources(descriptor));
		}
		configure(species, chr, viewStart, viewEnd, sources);
	}
	
	public static List<BiodallianceSource> getSources(Map<String, String> descriptor) {
		//reconstruct biodalliance sources (if there are any)
		List<BiodallianceSource> sources = new ArrayList<BiodallianceSource>();
		int i = 0;
		while (descriptor.containsKey(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + i)) {
			String sourceJsonString = descriptor.get(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX+i);
			sources.add(new BiodallianceSource(sourceJsonString));
			i++;
		}
		return sources;
	}
	
	public static String getFileResolverURL(String entityIdAndVersion) {
		if (entityIdAndVersion != null) {
			String[] tokens = entityIdAndVersion.split("\\.");
			if (tokens.length == 2) {
				return getFileResolverURL(tokens[0], tokens[1]);
			}
		}
		return null;
	}
	
	public static String getFileResolverURL(String entityId, String version) {
		StringBuilder sb = new StringBuilder();
		sb.append(FILE_RESOLVER_URL);
		sb.append("entityId=");
		sb.append(entityId);
		if (version != null) {
			sb.append("&version=");
			sb.append(version);
		}
		return sb.toString();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void showBiodallianceBrowser() {
		JavaScriptObject config = createNewBiodallianceBrowserConfig(initChr, initViewStart, initViewEnd, currentConfig);
		
		//add a source(s)
		for (BiodallianceSource source : sources) {
			if (BiodallianceSource.SourceType.BIGWIG.equals(source.getSourceType())) {
				addBigwigSource(config, source);	
			} else if (BiodallianceSource.SourceType.VCF.equals(source.getSourceType())) {
				addVCFSource(config, source);
			}
		}
		
		new Biodalliance013dev().show(config);
	}
	
	@Override
	public void viewAttached() {
		if (isConfigured) {
			//ready to show
			showBiodallianceBrowser();
		}
	}
	
	private JavaScriptObject createNewBiodallianceBrowserConfig(
			String initChr,
			int initViewStart,
			int initViewEnd,
			BiodallianceConfigInterface config
			) {
		long uniqueId = new Date().getTime();
		String containerId = "biodallianceContainerId"+uniqueId;
		view.setContainerId(containerId);
		
		return createNewBiodallianceBrowserConfig(containerId, initChr, initViewStart, initViewEnd, config.getTwoBitURI(),
				config.getBwgURI(), config.getStylesheetURI(), config.getTrixURI(), config.getSpeciesName(), config.getTaxon(), config.getAuthName(),
				config.getVersion(), config.getUscsName());
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
			String coordSystemUcscName
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
				noPersist: true,
				chr: initChr, 
				viewStart:  initViewStart, 
				viewEnd:  initViewEnd, 
				cookieKey: coordSystemSpeciesName, 
				fullScreen: false,
				coordSystem: { 
					speciesName: coordSystemSpeciesName, 
					taxon: coordSystemTaxon, 
					auth: coordSystemAuth, 
					version: coordSystemVersion, 
					ucscName: coordSystemUcscName},
				baseColors: {
	                 'A': 'black',
	                 'C': 'black',
	                 'G': 'black',
	                 'T': 'black',
	                 '-': 'black', //deletion
	                 'I': 'red'    //insertion
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
		addBigwigSource(biodallianceBrowserConfig, source.getSourceName(), source.getSourceURI(), source.getStyleType(), source.getStyleGlyphType(), source.getStyleColor(), source.getTrackHeightPx());
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
	
	private void addVCFSource(
			JavaScriptObject biodallianceBrowserConfig,
			BiodallianceSource source) {
		addVCFSource(biodallianceBrowserConfig, source.getSourceName(), source.getSourceURI(), source.getStyleType(), source.getStyleGlyphType(), source.getStyleColor(), source.getTrackHeightPx());
	}
	
	private native void addVCFSource(
			JavaScriptObject biodallianceBrowserConfig,
			String sourceName,
			String sourceURI,
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
			uri: sourceURI,
			payload: 'vcf',
			tier_type: 'tabix',
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
