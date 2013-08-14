package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MathJaxWidgetViewImpl extends LayoutContainer implements MathJaxWidgetView {

	private Presenter presenter;
	private SynapseJSNIUtils synapseJSNIUtils;
	private HTMLPanel mathContainer;
	
	@Inject
	public MathJaxWidgetViewImpl(SynapseJSNIUtils synapseJSNIUtils) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.addStyleName("inline-block");
	}
	
	@Override
	public void configure(String equation) {
		this.removeAll();
		mathContainer = new HTMLPanel(SafeHtmlUtils.htmlEscapeAllowEntities(equation));
		mathContainer.addStyleName("inline-block");
		this.add(mathContainer);
	}	
	
	@Override
	protected void onLoad() {
		super.onLoad();
		synapseJSNIUtils.processWithMathJax(mathContainer.getElement());
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	
	/*
	 * Private Methods
	 */

}
