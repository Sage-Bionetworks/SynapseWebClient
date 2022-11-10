package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;

public interface ComingSoonView extends IsWidget, SynapseView {
  /**
   * Set this view's presenter
   *
   * @param presenter
   */
  public void setPresenter(Presenter presenter);

  public interface Presenter {}
}
