package org.sagebionetworks.web.client.view.bootstrap.table;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.RootPanel;

public class TableHeaderResizeGrip extends Div {
	public static Element curCol = null, nxtCol = null;
	public static int curColWidth = -1, nxtColWidth = -1, pageX = -1;
	// TODO: add MOUSE_MOVE_HANDLER and MOUSE_UP_HANDLER to Document!
	public static final MouseMoveHandler MOUSE_MOVE_HANDLER = event -> {
		if (curCol != null) {
			int diffX = event.getClientX() - pageX;
			if (nxtCol != null && diffX > 0) {
				updateWidth(nxtCol, nxtColWidth - diffX);
			}
			updateWidth(curCol, curColWidth + diffX);
		}
	};
	
	public static void updateWidth(Element thElement, int newWidth) {
		String newWidthPx = newWidth + "px;";
		thElement.setAttribute("style", "width: " + newWidthPx + "min-width: " + newWidthPx);
	}
	
	public static final MouseUpHandler MOUSE_UP_HANDLER = event -> {
		curCol = null;
		nxtCol = null;
		pageX = -1;
		nxtColWidth = -1;
		curColWidth = -1;
	};
	
	static {
		RootPanel.get().addDomHandler(MOUSE_MOVE_HANDLER, MouseMoveEvent.getType());
		RootPanel.get().addDomHandler(MOUSE_UP_HANDLER, MouseUpEvent.getType());
	}
	
	public TableHeaderResizeGrip() {
		makeResizable();
	}

	public static final MouseDownHandler MOUSE_DOWN_HANDLER = event -> {
		// get the parent th element
		curCol = ((Div)event.getSource()).getElement().getParentElement();
		// and the next th element over
		nxtCol = curCol.getNextSiblingElement();
		pageX = event.getClientX();
		curColWidth = curCol.getOffsetWidth();
		if (nxtCol != null) {
			nxtColWidth = nxtCol.getOffsetWidth();
		}
	};
	
	public void makeResizable() {
		addStyleName("th-resizer");
		// get th and make position-relative
		addDomHandler(MOUSE_DOWN_HANDLER, MouseDownEvent.getType());
	}
}
