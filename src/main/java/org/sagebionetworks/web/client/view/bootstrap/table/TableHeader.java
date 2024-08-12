package org.sagebionetworks.web.client.view.bootstrap.table;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import org.gwtbootstrap3.client.ui.base.ComplexWidget;

/**
 * Simple
 * <th>
 *
 * @author jmhill
 *
 */
public class TableHeader extends ComplexWidget {

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
    DOM.setStyleAttribute(getElement(), TableData.MIN_WIDTH, minWidth);
  }
}
