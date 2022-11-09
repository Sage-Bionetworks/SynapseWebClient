package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.jsinterop.EntityFinderScope;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;

public class ProvenanceConfigViewImpl implements ProvenanceConfigView {

  public interface ProvenanceConfigViewImplUiBinder
    extends UiBinder<Widget, ProvenanceConfigViewImpl> {}

  private Presenter presenter;

  @UiField
  TextBox entityListField;

  @UiField
  TextBox depthField;

  @UiField
  TextBox displayHeightField;

  @UiField
  CheckBox showExpandCheckbox;

  @UiField
  Button entityFinderButton;

  Widget widget;

  @Inject
  public ProvenanceConfigViewImpl(
    ProvenanceConfigViewImplUiBinder binder,
    EntityFinderWidget.Builder entityFinderBuilder
  ) {
    widget = binder.createAndBindUi(this);

    entityFinderButton.addClickHandler(event ->
      entityFinderBuilder
        .setInitialScope(EntityFinderScope.CURRENT_PROJECT)
        .setInitialContainer(EntityFinderWidget.InitialContainer.PROJECT)
        .setHelpMarkdown(
          "Search or Browse Synapse to find an item and display the Provenance Graph within the Wiki page"
        )
        .setPromptCopy("Find items to insert a Provenance Graph")
        .setMultiSelect(true)
        .setSelectableTypes(EntityFilter.ALL_BUT_LINK)
        .setVersionSelection(EntityFinderWidget.VersionSelection.TRACKED)
        .setSelectedMultiHandler((selected, finder) -> {
          for (Reference entity : selected) {
            appendEntityListValue(entity);
          }
          finder.hide();
        })
        .build()
        .show()
    );
  }

  @Override
  public void initView() {
    depthField.setValue("1");
    entityListField.setValue("");
    displayHeightField.setValue("");
  }

  @Override
  public void checkParams() throws IllegalArgumentException {}

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
  public void clear() {}

  @Override
  public String getDepth() {
    return depthField.getValue();
  }

  @Override
  public void setDepth(String depth) {
    depthField.setValue(depth);
  }

  @Override
  public String getEntityList() {
    if (
      entityListField.getValue() != null
    ) return entityListField.getValue(); else return null;
  }

  @Override
  public void setEntityList(String entityList) {
    entityListField.setValue(entityList);
  }

  @Override
  public boolean isExpanded() {
    return showExpandCheckbox.getValue();
  }

  @Override
  public void setIsExpanded(boolean b) {
    showExpandCheckbox.setValue(b);
  }

  @Override
  public void setProvDisplayHeight(String provDisplayHeight) {
    displayHeightField.setValue(provDisplayHeight);
  }

  @Override
  public String getProvDisplayHeight() {
    return displayHeightField.getValue();
  }

  /*
   * Private Methods
   */
  private void appendEntityListValue(Reference selected) {
    String str = entityListField.getValue();
    if (str == null) str = "";
    if (!str.equals("")) str += ",";
    str += DisplayUtils.createEntityVersionString(selected);
    entityListField.setValue(str);
  }
}
