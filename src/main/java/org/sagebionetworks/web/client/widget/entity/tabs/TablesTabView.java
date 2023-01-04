package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TablesTabView extends IsWidget {
  void setProjectLevelUIVisible(boolean visible);

  public interface Presenter {}

  void setTitle(String title);

  void setDescription(String s);

  void setHelpLink(String s);

  void setTitlebar(Widget w);

  void setTitlebarVisible(boolean visible);

  void setBreadcrumb(Widget w);

  void setBreadcrumbVisible(boolean visible);

  void setTableList(Widget w);

  void setTableListVisible(boolean visible);

  void setTableEntityWidget(Widget w);

  void clearTableEntityWidget();

  void setEntityMetadata(Widget w);

  void setEntityMetadataVisible(boolean visible);

  void setSynapseAlert(Widget w);

  void setModifiedCreatedBy(IsWidget modifiedCreatedBy);

  void setProvenance(IsWidget w);

  void setTableUIVisible(boolean visible);

  void setActionMenu(IsWidget w);

  void setWikiPage(Widget w);

  void setWikiPageVisible(boolean visible);

  void setVersionAlertVisible(boolean visible);

  void setVersionAlertCopy(String title, String message);

  void setVersionAlertPrimaryText(String text);

  void setVersionAlertPrimaryAction(ClickHandler handler);

  void setVersionAlertSecondaryAction(
    String text,
    ClickHandler handler,
    boolean enabled,
    String tooltipText
  );
}
