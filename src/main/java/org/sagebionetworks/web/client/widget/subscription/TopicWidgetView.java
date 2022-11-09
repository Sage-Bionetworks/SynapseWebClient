package org.sagebionetworks.web.client.widget.subscription;

import com.google.gwt.user.client.ui.IsWidget;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.SynapseView;

public interface TopicWidgetView extends IsWidget, SynapseView {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  void setPresenter(Presenter presenter);

  void setTopicText(String text);

  void setTopicHref(String href);

  void addStyleNames(String styleNames);

  void setIcon(IconType type);

  /**
   * Presenter interface
   */
  public interface Presenter {}
}
