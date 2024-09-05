package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.utils.Callback;

public interface UserProfileWidget extends IsWidget {
  /**
   * Configure this widget before using.
   *
   * @param profile
   */
  void configure(UserProfile profile, String orcIDHref, Callback callback);
}
