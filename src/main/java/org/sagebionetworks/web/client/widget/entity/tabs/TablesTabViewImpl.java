package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.jsinterop.mui.Grid;
import org.sagebionetworks.web.client.widget.FullWidthAlert;

public class TablesTabViewImpl implements TablesTabView {

  @UiField
  SimplePanel tableTitlebarContainer;

  // tables
  @UiField
  SimplePanel tableBreadcrumbContainer;

  @UiField
  SimplePanel tableMetadataContainer;

  @UiField
  SimplePanel tableWidgetContainer;

  @UiField
  SimplePanel tableModifiedAndCreatedContainer;

  @UiField
  SimplePanel tableListWidgetContainer;

  @UiField
  SimplePanel synapseAlertContainer;

  @UiField
  Grid provenanceContainer;

  @UiField
  Div provenanceContainerHighlightBox;

  @UiField
  Div actionMenuContainer;

  @UiField
  Heading title;

  @UiField
  SimplePanel tableWikiPageContainer;

  @UiField
  FullWidthAlert versionAlert;

  @UiField
  Div projectLevelUI;

  @UiField
  Span tableDescription;

  @UiField
  Anchor helpLink;

  public interface TabsViewImplUiBinder
    extends UiBinder<Widget, TablesTabViewImpl> {}

  Widget widget;

  @Inject
  public TablesTabViewImpl(TabsViewImplUiBinder binder) {
    widget = binder.createAndBindUi(this);
    initClickHandlers();
  }

  private void initClickHandlers() {}

  @Override
  public void setBreadcrumb(Widget w) {
    tableBreadcrumbContainer.setWidget(w);
  }

  @Override
  public void setTableList(Widget w) {
    tableListWidgetContainer.setWidget(w);
  }

  @Override
  public void setProjectLevelUIVisible(boolean visible) {
    this.projectLevelUI.setVisible(visible);
  }

  @Override
  public void setTitle(String s) {
    title.setText(s);
  }

  @Override
  public void setDescription(String s) {
    tableDescription.setText(s);
  }

  @Override
  public void setHelpLink(String s) {
    helpLink.setHref(s);
    helpLink.setText(" Learn More");
  }

  @Override
  public void setTitlebar(Widget w) {
    tableTitlebarContainer.setWidget(w);
  }

  @Override
  public void setEntityMetadata(Widget w) {
    tableMetadataContainer.setWidget(w);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setTableEntityWidget(Widget w) {
    tableWidgetContainer.setWidget(w);
  }

  @Override
  public void clearTableEntityWidget() {
    tableWidgetContainer.clear();
  }

  @Override
  public void setSynapseAlert(Widget w) {
    synapseAlertContainer.setWidget(w);
  }

  @Override
  public void setBreadcrumbVisible(boolean visible) {
    title.setVisible(!visible);
    tableBreadcrumbContainer.setVisible(visible);
  }

  @Override
  public void setEntityMetadataVisible(boolean visible) {
    tableMetadataContainer.setVisible(visible);
  }

  @Override
  public void setTableListVisible(boolean visible) {
    tableListWidgetContainer.setVisible(visible);
  }

  @Override
  public void setTitlebarVisible(boolean visible) {
    tableTitlebarContainer.setVisible(visible);
  }

  @Override
  public void setModifiedCreatedBy(IsWidget modifiedCreatedBy) {
    tableModifiedAndCreatedContainer.setWidget(modifiedCreatedBy);
  }

  @Override
  public void setTableUIVisible(boolean visible) {
    provenanceContainer.setVisible(visible);
  }

  @Override
  public void setProvenance(IsWidget w) {
    provenanceContainerHighlightBox.clear();
    provenanceContainerHighlightBox.add(w);
  }

  @Override
  public void setActionMenu(IsWidget w) {
    w.asWidget().removeFromParent();
    actionMenuContainer.clear();
    actionMenuContainer.add(w);
  }

  @Override
  public void setWikiPage(Widget w) {
    tableWikiPageContainer.setWidget(w);
  }

  @Override
  public void setWikiPageVisible(boolean visible) {
    tableWikiPageContainer.setVisible(visible);
  }

  @Override
  public void setVersionAlertVisible(boolean visible) {
    versionAlert.setVisible(visible);
  }

  @Override
  public void setVersionAlertCopy(String title, String message) {
    versionAlert.setMessageTitle(title);
    versionAlert.setMessage(message);
  }

  @Override
  public void setVersionAlertPrimaryText(String text) {
    versionAlert.setPrimaryCTAText(text);
  }

  @Override
  public void setVersionAlertPrimaryAction(ClickHandler handler) {
    versionAlert.addPrimaryCTAClickHandler(handler);
  }

  @Override
  public void setVersionAlertSecondaryAction(
    String text,
    ClickHandler handler,
    boolean enabled,
    String tooltipText
  ) {
    versionAlert.setSecondaryCTAText(text);
    versionAlert.addSecondaryCTAClickHandler(handler);
    versionAlert.setSecondaryButtonEnabled(enabled);
    versionAlert.setSecondaryButtonTooltipText(tooltipText);
  }
}
