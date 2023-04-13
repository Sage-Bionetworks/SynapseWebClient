package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.jsinterop.EntityBadgeIconsProps;
import org.sagebionetworks.web.client.jsinterop.SynapseReactClientFullContextProviderProps;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

public interface EntityBadgeView extends IsWidget, SupportsLazyLoadInterface {
  void setEntity(EntityHeader header);

  void showLoadError(String entityId);

  void setClickHandler(ClickHandler handler);

  void showAddToDownloadList();

  void setSize(String s);

  void setMd5(String s);

  void setIcons(
    EntityBadgeIconsProps props,
    SynapseReactClientFullContextProviderProps contextProps
  );

  void setError(String error);

  void setPresenter(Presenter p);

  String getFriendlySize(Long contentSize, boolean b);

  void setModifiedByUserBadgeClickHandler(ClickHandler handler);

  void showMinimalColumnSet();

  void clearIcons();
  void clearEntityInformation();

  public interface Presenter {
    void onAddToDownloadList();
  }
}
