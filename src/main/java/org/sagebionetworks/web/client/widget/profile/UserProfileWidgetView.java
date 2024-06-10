package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;

public interface UserProfileWidgetView extends IsWidget {
  void setUsername(String userName);

  void setFirstName(String firstName);

  void setLastName(String lastName);

  void setBio(String summary);

  void addImageWidget(IsWidget image);

  void addFileInputWidget(IsWidget fileInputWidget);

  void setCurrentPosition(String position);

  void setCurrentAffiliation(String company);

  void setIndustry(String industry);

  void setLocation(String location);

  void setLink(String url);

  void setSynAlert(IsWidget w);

  void setOwnerId(String userId);
  void clearEmails();
  void setEmails(List<String> emails, String notificationEmail);
  void setCanEdit(boolean canEdit);
  void setOrcIdHref(String orcIdHref);

  void setEmailsVisible(boolean visible);
}
