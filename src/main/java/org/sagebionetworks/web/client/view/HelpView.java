package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.WikiPageKey;

public interface HelpView extends IsWidget, SynapseView {
  public void showHelpPage(WikiPageKey wikiKey);

  /**
   * Set this view's presenter
   *
   * @param presenter
   */
  public void setPresenter(Presenter presenter);

  public interface Presenter {}
}
