package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MathJaxConfigEditor implements MathJaxConfigView.Presenter, WidgetEditorPresenter {
	private Map<String, String> descriptor;
	private MathJaxConfigView view;
	
	@Inject
	public MathJaxConfigEditor(MathJaxConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}		
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor) {
		descriptor = widgetDescriptor;
		String equation = descriptor.get(WidgetConstants.MATHJAX_WIDGET_EQUATION_KEY);
		view.setEquation(equation);
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void updateDescriptorFromView() {
		//update widget descriptor from the view
		view.checkParams();
		String equation = view.getEquation();
		if (!equation.startsWith(WebConstants.MATHJAX_PREFIX))
			equation = WebConstants.MATHJAX_PREFIX + equation;
		if (!equation.endsWith(WebConstants.MATHJAX_SUFFIX))
			equation = equation + WebConstants.MATHJAX_SUFFIX;
		
		descriptor.put(WidgetConstants.MATHJAX_WIDGET_EQUATION_KEY, equation);
	}
	
	@Override
	public int getDisplayHeight() {
		return view.getDisplayHeight();
	}
	
	@Override
	public int getAdditionalWidth() {
		return view.getAdditionalWidth();
	}
	
	@Override
	public String getTextToInsert() {
		return null;
	}
	
	/*
	 * Private Methods
	 */
}
