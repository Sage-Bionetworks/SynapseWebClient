package org.sagebionetworks.web.client.widget.entity.renderer;


import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtvisualizationwrappers.client.cytoscape.CytoscapeGraph25;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View that contains the Cytoscape JS visualization.
 * @author jayhodgson
 *
 */
public class CytoscapeViewImpl implements CytoscapeView {
	
	public interface Binder extends	UiBinder<Widget, CytoscapeViewImpl> {}

	private Presenter presenter;
	
	Widget widget;
	
	@UiField
	Div visualizationContainer;
	@UiField
	Div synAlertContainer;
	boolean isAttached, isConfigured;
	String cyJS, styleJson;
	@Inject
	public CytoscapeViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		isAttached=false;
		isConfigured=false;
		visualizationContainer.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					isAttached = true;
					showIfAttachedAndConfigured();
				};
			}
		});
	}
	
	@Override
	public void configure(String cyJs, String styleJson) {
		this.cyJS = cyJs;
		this.styleJson = styleJson;
		isConfigured = true;
		showIfAttachedAndConfigured();
	}
	
	private void showIfAttachedAndConfigured() {
		if (isAttached && isConfigured) {
			visualizationContainer.clear();
			String id = Document.get().createUniqueId();
			visualizationContainer.getElement().setId(id);
			new CytoscapeGraph25().show(id,  cyJS, styleJson);
		}
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setLoading(boolean loading) {
	}
	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	
	@Override
	public void setGraphVisible(boolean isVisible) {
		visualizationContainer.setVisible(isVisible);
	}
}
