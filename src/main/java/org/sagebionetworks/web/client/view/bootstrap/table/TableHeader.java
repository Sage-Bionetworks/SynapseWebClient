package org.sagebionetworks.web.client.view.bootstrap.table;

import org.gwtbootstrap3.client.ui.base.ComplexWidget;
import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;

/**
 * Simple
 * <th>
 * 
 * @author jmhill
 *
 */
public class TableHeader extends ComplexWidget {

	public static Element curCol = null, nxtCol = null;
	public static int curColWidth = -1, nxtColWidth = -1, pageX = -1;
	
	public static final MouseMoveHandler MOUSE_MOVE_HANDLER = event -> {
		if (curCol != null) {
			int diffX = event.getClientX() - pageX;
			if (nxtCol != null) {
				nxtCol.setAttribute("style", "width: " + (nxtColWidth - (diffX))+"px;");
			}
			curCol.setAttribute("style", "width: " + (curColWidth - (diffX))+"px;");
		}
	};
	public static final MouseUpHandler MOUSE_UP_HANDLER = event -> {
		curCol = null;
		nxtCol = null;
		pageX = -1;
		nxtColWidth = -1;
		curColWidth = -1;
	};
	
	
	private static final String MIN_WIDTH = "minWidth";

	public TableHeader() {
		setElement(Document.get().createTHElement());
	}

	public String getText() {
		return getElement().getInnerText();
	}

	public void setText(String text) {
		getElement().setInnerText(text);
	}

	/**
	 * Set the minimum width of a column i.e. "75px"
	 * 
	 * @param minWidth
	 */
	public void setMinimumWidth(String minWidth) {
		DOM.setStyleAttribute(getElement(), MIN_WIDTH, minWidth);
	}
	
	public static final MouseDownHandler MOUSE_DOWN_HANDLER = event -> {
		curCol = ((Div)event.getSource()).getElement();
		nxtCol = curCol.getNextSiblingElement();
		pageX = event.getClientX();
		curColWidth = curCol.getOffsetWidth();
		if (nxtCol != null) {
			nxtColWidth = nxtCol.getOffsetWidth();
		}
	};
	
	public void makeResizable() {
		Div div = new Div();
		div.addStyleName("th-resizer");
		div.addDomHandler(MOUSE_DOWN_HANDLER, MouseDownEvent.getType());
		getElement().appendChild(div.getElement());
		DOM.setStyleAttribute(getElement(), "position", "relative");
	}
}
