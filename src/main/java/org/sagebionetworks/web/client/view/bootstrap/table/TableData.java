package org.sagebionetworks.web.client.view.bootstrap.table;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import org.gwtbootstrap3.client.ui.base.ComplexWidget;

/**
 * Simple
 * <td>
 *
 * @author jmhill
 *
 */
public class TableData extends ComplexWidget {

  public static final String MIN_WIDTH = "minWidth";

  public TableData() {
    setElement(Document.get().createTDElement());
  }

  /**
   * Set the minimum width of a column i.e. "75px"
   *
   * @param minWidth
   */
  public void setMinimumWidth(String minWidth) {
    DOM.setStyleAttribute(getElement(), MIN_WIDTH, minWidth);
  }
}
