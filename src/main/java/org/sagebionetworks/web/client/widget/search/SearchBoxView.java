package org.sagebionetworks.web.client.widget.search;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;

public interface SearchBoxView extends IsWidget, SynapseView {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  public void setPresenter(Presenter presenter);

  /**
   * resets the view to default state
   */
  public void clear();

  /**
   * Presenter interface
   */
  public interface Presenter {
    void search(String value);
  }

  public void setVisible(boolean isVisible);
}
