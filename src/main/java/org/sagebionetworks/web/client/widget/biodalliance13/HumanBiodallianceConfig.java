package org.sagebionetworks.web.client.widget.biodalliance13;

import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceConfigInterface;
import org.sagebionetworks.web.client.SynapseProperties;
import com.google.inject.Inject;

public class HumanBiodallianceConfig implements BiodallianceConfigInterface {

	public static final String HUMAN_SPECIES_NAME = "human";
	public static final int HUMAN_TAXON = 9606;
	public static final String HUMAN_VERSION = "37";
	public static final String HUMAN_USCS_NAME = "hg19";
	public static final String HUMAN_AUTH_NAME = "NCBI";

	public static String humanTwoBitURI;
	public static String humanBwgURI;
	public static String humanTrixURI;
	public static String humanTrixxURI;
	public static String humanStylesheetURI;

	public static boolean isUrlInitialized = false;
	SynapseProperties synapseProperties;

	@Inject
	public HumanBiodallianceConfig(SynapseProperties synapseProperties) {
		this.synapseProperties = synapseProperties;

		if (!isUrlInitialized) {
			initURIs();
		}
	}

	public void initURIs() {
		humanTwoBitURI = BiodallianceWidget.getFileResolverURL(synapseProperties.getSynapseProperty("org.sagebionetworks.portal.biodalliance.human.twobit"));
		humanBwgURI = BiodallianceWidget.getFileResolverURL(synapseProperties.getSynapseProperty("org.sagebionetworks.portal.biodalliance.human.bwg"));
		humanTrixURI = BiodallianceWidget.getFileResolverURL(synapseProperties.getSynapseProperty("org.sagebionetworks.portal.biodalliance.human.trix"));
		humanTrixxURI = BiodallianceWidget.getFileResolverURL(synapseProperties.getSynapseProperty("org.sagebionetworks.portal.biodalliance.human.trixx"));
		humanStylesheetURI = BiodallianceWidget.getFileResolverURL(synapseProperties.getSynapseProperty("org.sagebionetworks.portal.biodalliance.human.stylesheet"));
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
	public String getTrixxURI() {
		return humanTrixxURI;
	}

	@Override
	public String getStylesheetURI() {
		return humanStylesheetURI;
	}


}
