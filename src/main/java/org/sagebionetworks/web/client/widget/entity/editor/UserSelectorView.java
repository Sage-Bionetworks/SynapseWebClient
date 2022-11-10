package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;

public interface UserSelectorView extends IsWidget {
  void show();

  void hide();

  void setSelectBox(Widget w);

  void setPresenter(Presenter p);

  void addModalShownHandler(ModalShownHandler modalShownHandler);

  public interface Presenter {
    void onModalShown();

    void onModalHidden();
  }
}
