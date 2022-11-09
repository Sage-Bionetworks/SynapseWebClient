package org.sagebionetworks.web.client.widget.doi;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;
import java.util.Optional;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.VersionInfo;

public interface CreateOrUpdateDoiModalView extends IsWidget {
  void setPresenter(Presenter presenter);

  interface Presenter {
    void onSaveDoi();
    void onVersionChange(Optional<Long> version);
  }

  String getCreators();

  void setCreators(String creators);

  String getTitles();

  void setTitles(String titles);

  String getResourceTypeGeneral();

  void setResourceTypeGeneral(String resourceTypeGeneral);

  Long getPublicationYear();

  void setPublicationYear(Long publicationYear);

  void setOverwriteWarningVisible(boolean visible);

  void show();

  void hide();

  void setModalTitle(String title);

  void setJobTrackingWidget(IsWidget w);

  void setSynAlert(IsWidget w);

  void reset();

  void setIsLoading(boolean isLoading);

  void setVersions(List<VersionInfo> versions, Optional<Long> selectedVersion);

  void setEntity(Entity entityType);
}
