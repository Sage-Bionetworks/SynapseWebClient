package org.sagebionetworks.web.client.widget.docker.modal;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface AddDockerCommitModalView extends IsWidget {
  public interface Presenter {
    void onSave();
    void onCancel();
  }

  void setPresenter(Presenter presenter);

  void show();

  void hide();

  String getDigest();

  String getTag();

  void clear();

  void setAlert(Widget widget);

  void setModalTitle(String title);
}
