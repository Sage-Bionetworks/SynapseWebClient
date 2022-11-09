package org.sagebionetworks.web.client.view.bootstrap.table;

import com.google.gwt.dom.client.Document;
import org.gwtbootstrap3.client.ui.base.ComplexWidget;

/**
 * Simple
 * <td>
 *
 * @author jmhill
 *
 */
public class TableData extends ComplexWidget {

  public TableData() {
    setElement(Document.get().createTDElement());
  }
}
