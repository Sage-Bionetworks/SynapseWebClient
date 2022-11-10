package org.sagebionetworks.web.client.view.bootstrap.table;

import com.google.gwt.dom.client.Document;
import org.gwtbootstrap3.client.ui.base.ComplexWidget;

/**
 * Simple
 * <tr>
 *
 * @author jmhill
 *
 */
public class TableRow extends ComplexWidget {

  public TableRow() {
    setElement(Document.get().createTRElement());
  }
}
