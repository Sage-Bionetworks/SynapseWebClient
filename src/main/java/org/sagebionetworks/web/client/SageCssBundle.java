package org.sagebionetworks.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;

public interface SageCssBundle extends ClientBundle {
	public static final SageCssBundle INSTANCE =  GWT.create(SageCssBundle.class);

	@NotStrict
	@Source("resource/css/jquery/themes/base/jquery.ui.all.css")
	CssResource jqueryThemeCss();
	
	@NotStrict
	@Source("resource/css/provenance.css")
	CssResource provCss();
	
	@NotStrict
	@Source("resource/css/code-highlighting.css")
	CssResource codeHighlightingCss();
	
	@NotStrict
	@Source("resource/css/Portal.css")
	CssResource portalCss();
}
