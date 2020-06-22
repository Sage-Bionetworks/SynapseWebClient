package org.sagebionetworks.web.client.view.bootstrap.table;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpHandler;

public class TableHeaderResizeGrip extends Div {
	public static Element curCol = null, nxtCol = null;
	public static int curColWidth = -1, nxtColWidth = -1, pageX = -1;
	
	// TODO: add MOUSE_MOVE_HANDLER and MOUSE_UP_HANDLER to Document!
	public static final MouseMoveHandler MOUSE_MOVE_HANDLER = event -> {
		if (curCol != null) {
			int diffX = event.getClientX() - pageX;
			if (nxtCol != null) {
				nxtCol.setAttribute("style", "width: " + (nxtColWidth - (diffX))+"px;");
			}
			curCol.setAttribute("style", "width: " + (curColWidth - (diffX))+"px;");
			
			SynapseJSNIUtilsImpl._consoleLog("MOUSE_MOVE_HANDLER: curCol is not null.");
			SynapseJSNIUtilsImpl._consoleLog("diffX="+diffX);
		}
	};
	public static final MouseUpHandler MOUSE_UP_HANDLER = event -> {
		curCol = null;
		nxtCol = null;
		pageX = -1;
		nxtColWidth = -1;
		curColWidth = -1;
	};
	
	
	public TableHeaderResizeGrip() {
		makeResizable();
	}

	public static final MouseDownHandler MOUSE_DOWN_HANDLER = event -> {
		// TODO: get the parent TH?
		SynapseJSNIUtilsImpl._consoleLog("MOUSE_DOWN_HANDLER: set curCol nxtCol.");

		curCol = ((Div)event.getSource()).getElement();
		nxtCol = curCol.getNextSiblingElement();
		pageX = event.getClientX();
		curColWidth = curCol.getOffsetWidth();
		if (nxtCol != null) {
			nxtColWidth = nxtCol.getOffsetWidth();
		}
		SynapseJSNIUtilsImpl._consoleLog("pageX="+pageX);
		SynapseJSNIUtilsImpl._consoleLog("curColWidth="+curColWidth);
		SynapseJSNIUtilsImpl._consoleLog("nxtColWidth="+nxtColWidth);

	};
	
	public void makeResizable() {
		addStyleName("th-resizer");
		// get th and make position-relative
		addDomHandler(MOUSE_DOWN_HANDLER, MouseDownEvent.getType());
	}
}
