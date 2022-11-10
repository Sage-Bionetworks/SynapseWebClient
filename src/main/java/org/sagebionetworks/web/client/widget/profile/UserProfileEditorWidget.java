package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.utils.Callback;

public interface UserProfileEditorWidget extends IsWidget {
  /**
   * Configure this widget before using.
   *
   * @param profile
   */
  void configure(UserProfile profile, String orcIDHref, Callback callback);

  /**
   * Are the values in this editor valid?
   *
   * @return
   */
  boolean isValid();

  /**
   * Get the first Name.
   *
   * @return
   */
  String getFirstName();

  /**
   * Get the image FileHandle ID.
   *
   * @return
   */
  String getImageId();

  /**
   * Get the user's last name
   *
   * @return
   */
  String getLastName();

  /**
   * Get the username.
   *
   * @return
   */
  String getUsername();

  /**
   * Current position
   *
   * @return
   */
  String getPosition();

  /**
   * Current affiliation
   *
   * @return
   */
  String getCompany();

  /**
   * Industry/Discipline
   *
   * @return
   */
  String getIndustry();

  /**
   * City, Country
   *
   * @return
   */
  String getLocation();

  /**
   * Link to more info
   *
   * @return
   */
  String getUrl();

  /**
   * Bio
   *
   * @return
   */
  String getSummary();

  void addKeyDownHandler(KeyDownHandler keyDownHandler);

  void setUploadingCallback(Callback startedUploadingCallback);

  void setUploadingCompleteCallback(Callback uploadCompleteCallback);

  void setIsEditingMode(boolean isEditing);
  boolean isEditingMode();
}
