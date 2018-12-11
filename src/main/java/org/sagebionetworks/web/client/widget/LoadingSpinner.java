package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class LoadingSpinner implements IsWidget {
	public interface Binder extends UiBinder<Widget, LoadingSpinner> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	
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
		uiBinder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return loadingSpinnerDiv;
	}
	
	public void setVisible(boolean visible) {
		loadingSpinnerDiv.setVisible(visible);
	}
	
	public void setSize(int px) {
		setSize(px + "px");
	}
	
	public void setSize(String size) {
		loadingSpinnerDiv.setSize(size, size);
		_setSize(loadingSpinnerDiv.getElement(), size);
	}
	
	public static native void _setSize(Element elem, String size) /*-{
		elem.style.width = size;
		elem.style.height = size;
		elem.style.backgroundSize = size + ' ' + size;
	}-*/;
	
	public void setAddStyleNames(String styleNames) {
		loadingSpinnerDiv.addStyleName(styleNames);
	}
	
	public boolean isVisible() {
		return loadingSpinnerDiv.isVisible();
	}
	public void setMarginLeft(double margin) {
		loadingSpinnerDiv.setMarginLeft(margin);
	}
	public void setMarginRight(double margin) {
		loadingSpinnerDiv.setMarginLeft(margin);
	}
	public boolean isAttached() {
		return loadingSpinnerDiv.isAttached();
	}
	public void setIsWhite(boolean isWhite) {
		String spinnerStyle = isWhite ? "white-spinner" : "spinner";
		String oldStyle = isWhite ? "spinner" : "white-spinner";
		loadingSpinnerDiv.removeStyleName(oldStyle);
		loadingSpinnerDiv.addStyleName(spinnerStyle);
	}
}
