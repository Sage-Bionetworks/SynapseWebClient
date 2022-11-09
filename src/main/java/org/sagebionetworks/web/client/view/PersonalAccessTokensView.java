package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.presenter.PersonalAccessTokensPresenter;

public interface PersonalAccessTokensView extends IsWidget {
  /**
   * Set this view's presenter
   *
   * @param presenter
   */
  public void setPresenter(PersonalAccessTokensPresenter presenter);

  /**
   * Renders the view for a given presenter
   */
  public void render();
}
