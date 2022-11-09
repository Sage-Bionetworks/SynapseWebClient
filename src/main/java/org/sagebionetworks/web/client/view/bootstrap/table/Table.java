package org.sagebionetworks.web.client.view.bootstrap.table;

import com.google.gwt.dom.client.Document;
import org.gwtbootstrap3.client.ui.base.ComplexWidget;

/**
 * Simple
 * <table class="table">
 *
 * @author jmhill
 *
 */
public class Table extends ComplexWidget {

  public Table() {
    setElement(Document.get().createTableElement());
  }

  public void setAlign(String alignment) {
    getElement().setAttribute("align", alignment);
  }
}
