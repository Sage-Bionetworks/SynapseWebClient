package org.sagebionetworks.web.client.widget.biodalliance;

import org.sagebionetworks.web.client.GlobalApplicationState;

import com.google.inject.Inject;

public class HumanBiodallianceConfig implements BiodallianceConfigInterface {
	GlobalApplicationState globalApplicationState;
	
	public static final String HUMAN_SPECIES_NAME = "human";
	public static final int HUMAN_TAXON = 9606;
	public static final String HUMAN_VERSION = "37";
	public static final String HUMAN_USCS_NAME = "hg19";
	public static final String HUMAN_AUTH_NAME = "NCBI";
	
	public static String humanTwoBitURI;
	public static String humanBwgURI;
	public static String humanTrixURI;
	public static String humanStylesheetURI;
	
	public static boolean isUrlInitialized = false;
	
	@Inject
	public HumanBiodallianceConfig(GlobalApplicationState globalApplicationState) {
		this.globalApplicationState = globalApplicationState;
		
		if (!isUrlInitialized) {
			initURIs();
		}
	}
	public void initURIs() {
		humanTwoBitURI = BiodallianceWidget.getFileResolverURL(globalApplicationState.getSynapseProperty("org.sagebionetworks.portal.biodalliance.human.twobit"));
		humanBwgURI = BiodallianceWidget.getFileResolverURL(globalApplicationState.getSynapseProperty("org.sagebionetworks.portal.biodalliance.human.bwg"));
		humanTrixURI = BiodallianceWidget.getFileResolverURL(globalApplicationState.getSynapseProperty("org.sagebionetworks.portal.biodalliance.human.trix"));
		humanStylesheetURI = BiodallianceWidget.getFileResolverURL(globalApplicationState.getSynapseProperty("org.sagebionetworks.portal.biodalliance.human.stylesheet"));
		isUrlInitialized = true;
	}
	@Override
	public String getSpeciesName() {
		return HUMAN_SPECIES_NAME;
	}
	@Override
	public int getTaxon() {
		return HUMAN_TAXON;
	}
	@Override
	public String getVersion() {
		return HUMAN_VERSION;
	}
	@Override
	public String getUscsName() {
		return HUMAN_USCS_NAME;
	}
	@Override
	public String getAuthName() {
		return HUMAN_AUTH_NAME;
	}
	@Override
	public String getTwoBitURI() {
		return humanTwoBitURI;
	}
	@Override
	public String getBwgURI() {
		return humanBwgURI;
	}
	@Override
	public String getTrixURI() {
		return humanTrixURI;
	}
	@Override
	public String getStylesheetURI() {
		return humanStylesheetURI;
	}
	
	
}
