package org.sagebionetworks.web.client.widget.breadcrumb;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;
import org.sagebionetworks.web.client.SynapseView;

public interface BreadcrumbView extends IsWidget, SynapseView {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  public void setPresenter(Presenter presenter);

  public void setLinksList(List<LinkData> breadcrumbs);

  public void setLinksList(List<LinkData> breadcrumbs, String current);

  /**
   * Presenter interface
   */
  public interface Presenter {
    /**
     * Available for the view the change the current Place
     *
     * @param place
     */
    public void goTo(Place place);

    void clear();
  }
}
