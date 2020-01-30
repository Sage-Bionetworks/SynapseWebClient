package org.sagebionetworks.web.unitclient.widget.table;

import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * A very simple IsWidget
 * 
 */
public class IsWidgetStub implements IsWidget, HasKeyDownHandlers {

	KeyDownHandler handler;
	int rowIndex;
	int colIndex;

	/**
	 * Create with an address.
	 * 
	 * @param rowIndex
	 * @param colIndex
	 */
	public IsWidgetStub(int rowIndex, int colIndex) {
		super();
		this.rowIndex = rowIndex;
		this.colIndex = colIndex;
	}

	@Override
	public Widget asWidget() {
		return null;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		if (this.handler != null) {
			this.handler.onKeyDown((KeyDownEvent) event);
		}
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		this.handler = handler;
		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				IsWidgetStub.this.handler = null;
			}
		};
	}

	@Override
	public String toString() {
		return "IsWidgetStub [rowIndex=" + rowIndex + ", colIndex=" + colIndex + "]";
	}

}
