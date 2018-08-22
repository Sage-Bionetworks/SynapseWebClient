package org.sagebionetworks.web.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface SageImageBundle extends ClientBundle {
	
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
	@Source("resource/images/people.png")
	ImageResource people();
	@Source("resource/images/prov.png")
	ImageResource prov();
}
