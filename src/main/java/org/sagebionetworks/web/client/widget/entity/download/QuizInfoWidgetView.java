package org.sagebionetworks.web.client.widget.entity.download;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;

public interface QuizInfoWidgetView extends IsWidget, SynapseView {
  void setPresenter(Presenter presenter);

  void configure();

  /**
   * Presenter interface
   */
  public interface Presenter {}
}
