package org.sagebionetworks.web.client.view.bootstrap.table;

import com.google.gwt.dom.client.Document;
import org.gwtbootstrap3.client.ui.base.ComplexWidget;

/**
 * Simple <tbody>
 *
 * @author jmhill
 *
 */
public class TBody extends ComplexWidget {

  public TBody() {
    setElement(Document.get().createTBodyElement());
  }
}
