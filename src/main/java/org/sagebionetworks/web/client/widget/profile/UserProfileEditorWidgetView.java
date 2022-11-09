package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface UserProfileEditorWidgetView extends IsWidget {
  public interface Presenter {
    // inform presenter that the user wants to save the edits to their profile
    void onSave();
    void onCancel();
    // inform presenter that the user is editing their profile info
    void setIsEditingMode(boolean isEditing);
  }

  void setUsername(String userName);

  void setFirstName(String firstName);

  String getFirstName();

  String getLastName();

  String getUsername();

  void setLastName(String lastName);

  void setBio(String summary);

  String getBio();

  void showUsernameError(String error);

  void hideUsernameError();

  void addImageWidget(IsWidget image);

  void addFileInputWidget(IsWidget fileInputWidget);

  public void setPresenter(Presenter presenter);

  String getLink();

  void showLinkError(String string);

  void hideLinkError();

  String getCurrentPosition();

  void setCurrentPosition(String position);

  void setCurrentAffiliation(String company);

  String getCurrentAffiliation();

  void setIndustry(String industry);

  String getIndustry();

  void setLocation(String location);

  String getLocation();

  void setLink(String url);

  void setSynAlert(IsWidget w);

  void addKeyDownHandlerToFields(KeyDownHandler keyDownHandler);

  // if in edit mode, then editable text fields will be available.  If not, their static values will be shown (but still in a form).
  void setEditMode(boolean isEditing);

  void setOwnerId(String userId);
  void setEmail(String email);
  void resetSaveButtonState();
  void setCanEdit(boolean canEdit);

  void setOrcIdHref(String orcIdHref);
}
