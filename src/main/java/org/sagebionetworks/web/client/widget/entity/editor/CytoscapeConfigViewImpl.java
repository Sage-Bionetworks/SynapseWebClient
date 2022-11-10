package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;

public class CytoscapeConfigViewImpl implements CytoscapeConfigView {

  public interface CytoscapeConfigViewImplUiBinder
    extends UiBinder<Widget, CytoscapeConfigViewImpl> {}

  private Presenter presenter;

  @UiField
  TextBox cyJsEntity;

  @UiField
  TextBox styleEntity;

  @UiField
  Button findCyJSButton;

  @UiField
  Button findJSONStyleButton;

  @UiField
  TextBox displayHeightField;

  EntityFinderWidget cyJsFinder, styleFinder;

  Widget widget;

  @Inject
  public CytoscapeConfigViewImpl(
    CytoscapeConfigViewImplUiBinder binder,
    EntityFinderWidget.Builder entityFinderBuilder
  ) {
    widget = binder.createAndBindUi(this);

    this.cyJsFinder =
      entityFinderBuilder
        .setMultiSelect(false)
        .setSelectableTypes(EntityFilter.FILE)
        .setVersionSelection(EntityFinderWidget.VersionSelection.DISALLOWED)
        .setSelectedHandler((selected, finder) -> {
          cyJsEntity.setValue(selected.getTargetId());
          finder.hide();
        })
        .build();

    this.styleFinder =
      entityFinderBuilder
        // Same properties except the handler
        .setSelectedHandler((selected, finder) -> {
          styleEntity.setValue(selected.getTargetId());
          finder.hide();
        })
        .build();

    findCyJSButton.addClickHandler(event -> cyJsFinder.show());
    findJSONStyleButton.addClickHandler(event -> styleFinder.show());
  }

  @Override
  public void initView() {}

  @Override
  public void checkParams() throws IllegalArgumentException {
    if ("".equals(cyJsEntity.getValue())) throw new IllegalArgumentException(
      DisplayConstants.ERROR_SELECT_CYTOSCAPE_FILE
    );
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void showLoading() {}

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public String getEntity() {
    return cyJsEntity.getValue();
  }

  @Override
  public void setEntity(String entityId) {
    cyJsEntity.setValue(entityId);
  }

  @Override
  public String getStyleEntity() {
    return styleEntity.getValue();
  }

  @Override
  public void setStyleEntity(String entityId) {
    styleEntity.setValue(entityId);
  }

  @Override
  public void clear() {
    cyJsEntity.setValue("");
    styleEntity.setValue("");
    displayHeightField.setValue("");
  }

  @Override
  public String getHeight() {
    return displayHeightField.getValue();
  }

  @Override
  public void setHeight(String height) {
    displayHeightField.setValue(height);
  }
}
