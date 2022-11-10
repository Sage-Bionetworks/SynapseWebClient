package org.sagebionetworks.web.client.widget;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import java.util.Iterator;

public interface RadioWidget {
  public interface Presenter {}

  Widget asWidget();

  void add(Widget widget);

  void clear();

  Iterator<Widget> iterator();

  boolean remove(Widget widget);

  void setGroupName(String groupName);

  void addClickHandler(ClickHandler handler);
}
