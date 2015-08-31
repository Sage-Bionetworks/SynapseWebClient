package org.sagebionetworks.web.client.widget.biodalliance13;

import org.sagebionetworks.web.client.GlobalApplicationState;

import com.google.inject.Inject;

public class MouseBiodallianceConfig implements BiodallianceConfigInterface {
	GlobalApplicationState globalApplicationState;
	public static boolean isUrlInitialized = false;
	
	public static final String MOUSE_SPECIES_NAME = "mouse";
	public static final int MOUSE_TAXON = 10090;
	public static final String MOUSE_VERSION = "38";
	public static final String MOUSE_USCS_NAME = "mm10";
	public static final String MOUSE_AUTH_NAME = "GRCm";

	public static String mouseTwoBitURI;
	public static String mouseBwgURI;
	public static String mouseTrixURI;
	public static String mouseStylesheetURI;
	
	
	@Inject
	public MouseBiodallianceConfig(GlobalApplicationState globalApplicationState) {
		this.globalApplicationState = globalApplicationState;
		
		if (!isUrlInitialized) {
			initURIs();
		}
	}
	public void initURIs() {
		mouseTwoBitURI = BiodallianceWidget.getFileResolverURL(globalApplicationState.getSynapseProperty("org.sagebionetworks.portal.biodalliance.mouse.twobit"));
		mouseBwgURI = BiodallianceWidget.getFileResolverURL(globalApplicationState.getSynapseProperty("org.sagebionetworks.portal.biodalliance.mouse.bwg"));
		mouseTrixURI = BiodallianceWidget.getFileResolverURL(globalApplicationState.getSynapseProperty("org.sagebionetworks.portal.biodalliance.mouse.trix"));
		mouseStylesheetURI = BiodallianceWidget.getFileResolverURL(globalApplicationState.getSynapseProperty("org.sagebionetworks.portal.biodalliance.mouse.stylesheet"));
		isUrlInitialized = true;
	}
	@Override
	public String getSpeciesName() {
		return MOUSE_SPECIES_NAME;
	}
	@Override
	public int getTaxon() {
		return MOUSE_TAXON;
	}
	@Override
	public String getVersion() {
		return MOUSE_VERSION;
	}
	@Override
	public String getUscsName() {
		return MOUSE_USCS_NAME;
	}
	@Override
	public String getAuthName() {
		return MOUSE_AUTH_NAME;
	}
	@Override
	public String getTwoBitURI() {
		return mouseTwoBitURI;
	}
	@Override
	public String getBwgURI() {
		return mouseBwgURI;
	}
	@Override
	public String getTrixURI() {
		return mouseTrixURI;
	}
	@Override
	public String getStylesheetURI() {
		return mouseStylesheetURI;
	}

}
