package org.sagebionetworks.web.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface SageImageBundle extends ClientBundle {

	@Source("images/icon-gene-expression-16.gif")
	ImageResource iconGeneExpression16();

	@Source("images/icon-phenotypes-16.gif")
	ImageResource iconPhenotypes16();
	
	@Source("images/icon-genotypes-16.gif")
	ImageResource iconGenotype16();
		
	@Source("images/Sage-Header.png")
	ImageResource sageLogoAndTitle();
	
	@Source("images/loading-16.gif")
	ImageResource loading16();	
	
	@Source("images/loading-31.gif")
	ImageResource loading31();	
	
	@Source("images/transparent-16.png")
	ImageResource iconTransparent16();
	
	@Source("images/upArrow.png")
	ImageResource iconUpArrow();
	
	@Source("images/downArrow.png")
	ImageResource iconDownArrow();
	
	@Source("images/searchButtonIcon.png")
	ImageResource searchButtonIcon();

	@Source("images/headerSearchButtonIcon.png")
	ImageResource searchButtonHeaderIcon();
	
	@Source("images/expand.png")
	ImageResource expand();

	@Source("images/logo-R.png")
	ImageResource logoR45();
	
	@Source("images/logo-java.png")
	ImageResource logoJava45();
	
	@Source("images/logo-python.png")
	ImageResource logoPython45();
	
	@Source("images/logo-Shell.png")
	ImageResource logoCommandLine45();
	
	@Source("images/genomeSpace-logo-title-16.gif")
	ImageResource genomeSpaceLogoTitle16();
	
	@Source("images/SynapseHeader.png")
	ImageResource synaspseHeader();
	
	@Source("images/SynapseTextHeader.png")
	ImageResource synapseTextHeader();
	
	@Source("images/LogoHeader.png")
	ImageResource logoHeader();
	
	@Source("images/SynapseLogo-small.png")
	ImageResource synapseLogoSmall();
	
	@Source("images/NCI_logo.jpg")
	ImageResource nciLogo();

	@Source("images/LSDF_logo.jpg")
	ImageResource lsdfLogo();

	@Source("images/NHLBI_logo.jpg")
	ImageResource nhlbiLogo();

	@Source("images/Sloan_logo.jpg")
	ImageResource sloanLogo();

	@Source("images/spotlight/Dream9_AD.png")
	ImageResource dream9AD();
	
	@Source("images/spotlight/Dream85_Banner.jpg")
	ImageResource dream85Banner();
	
	@Source("images/spotlight/Dream9_AML.png")
	ImageResource dream9AML();
	
	@Source("images/spotlight/Dream9_BROAD.jpg")
	ImageResource dream9Broad();
	
	@Source("images/spotlight/Dream85_SM.jpg")
	ImageResource dream85SM();
	
	@Source("images/spotlight/TcgaPancancer.jpg")
	ImageResource tcgaPancancer();
	
	@Source("images/certificate.png")
	ImageResource certificate();

	@Source("images/greyArrow.png")
	ImageResource greyArrow();
}
