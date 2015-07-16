package org.sagebionetworks.web.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface SageImageBundle extends ClientBundle {

	@Source("resource/images/icon-gene-expression-16.gif")
	ImageResource iconGeneExpression16();

	@Source("resource/images/icon-phenotypes-16.gif")
	ImageResource iconPhenotypes16();
	
	@Source("resource/images/icon-genotypes-16.gif")
	ImageResource iconGenotype16();
		
	@Source("resource/images/loading-16.gif")
	ImageResource loading16();	
	
	@Source("resource/images/loading-31.gif")
	ImageResource loading31();	
	
	@Source("resource/images/transparent-16.png")
	ImageResource iconTransparent16();
	
	@Source("resource/images/upArrow.png")
	ImageResource iconUpArrow();
	
	@Source("resource/images/downArrow.png")
	ImageResource iconDownArrow();
	
	@Source("resource/images/searchButtonIcon.png")
	ImageResource searchButtonIcon();

	@Source("resource/images/headerSearchButtonIcon.png")
	ImageResource searchButtonHeaderIcon();
	
	@Source("resource/images/expand.png")
	ImageResource expand();

	@Source("resource/images/logo-R.png")
	ImageResource logoR45();
	
	@Source("resource/images/logo-java.png")
	ImageResource logoJava45();
	
	@Source("resource/images/logo-python.png")
	ImageResource logoPython45();
	
	@Source("resource/images/logo-Shell.png")
	ImageResource logoCommandLine45();
	
	@Source("resource/images/genomeSpace-logo-title-16.gif")
	ImageResource genomeSpaceLogoTitle16();
	
	@Source("resource/images/NCI_logo.jpg")
	ImageResource nciLogo();
	
	@Source("resource/images/CTF_logo.png")
	ImageResource ctfLogo();

	@Source("resource/images/LSDF_logo.jpg")
	ImageResource lsdfLogo();

	@Source("resource/images/NHLBI_logo.jpg")
	ImageResource nhlbiLogo();

	@Source("resource/images/Sloan_logo.jpg")
	ImageResource sloanLogo();
	
	@Source("resource/images/NIA_logo.jpg")
	ImageResource niaLogo();
	
	@Source("resource/images/NIMH_logo.jpg")
	ImageResource nimhLogo();
	
	@Source("resource/images/spotlight/Astrazeneca_Sanger_banner.jpg")
	ImageResource astrazenecaSanger();
	
	@Source("resource/images/spotlight/ALS2_banner.jpg")
	ImageResource als2();
	
	@Source("resource/images/certificate.png")
	ImageResource certificate();

	@Source("resource/images/greyArrow.png")
	ImageResource greyArrow();
	
	/**
	 * New home page artifacts
	 */
	@Source("resource/images/collaborate.png")
	ImageResource collaborate();
	@Source("resource/images/directory.png")
	ImageResource directory();
	@Source("resource/images/dream.png")
	ImageResource dream();
	@Source("resource/images/lock.png")
	ImageResource unlock();
	@Source("resource/images/logo.png")
	ImageResource logo();
	@Source("resource/images/people.png")
	ImageResource people();
	@Source("resource/images/prov.png")
	ImageResource prov();
	@Source("resource/images/synapse_logo.png")
	ImageResource synapseLogo();
}
