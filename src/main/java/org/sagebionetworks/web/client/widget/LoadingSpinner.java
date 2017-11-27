package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class LoadingSpinner implements IsWidget {
	
	public interface Binder extends UiBinder<Widget, LoadingSpinner> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	Widget widget;
	
	@UiField
	Div loadingSpinnerDiv;
	
	/**
	 * ## Usage
	 * 
	 * In your ui.xml, add the loading widget.
	 * ```
	 * xmlns:w="urn:import:org.sagebionetworks.web.client.widget"
	 * <w:LoadingSpinner size="100px" />
	 * ```
	 */
	public LoadingSpinner() {
		widget = uiBinder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}
	
	public void setSize(int px) {
		setSize(px + "px");
	}
	
	public void setSize(String size) {
		widget.setHeight(size);
		widget.setWidth(size);
	}
	
	public void setAddStyleNames(String styleNames) {
		widget.addStyleName(styleNames);
	}
}
