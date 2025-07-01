package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class JSONSchemaFormConfigEditor implements WidgetEditorPresenter {

  private JSONSchemaFormConfigView view;
  private Map<String, String> descriptor;

  @Inject
  public JSONSchemaFormConfigEditor(JSONSchemaFormConfigView view) {
    this.view = view;
    view.initView();
  }

  @Override
  public void configure(
    WikiPageKey wikiKey,
    Map<String, String> widgetDescriptor,
    DialogCallback dialogCallback
  ) {
    descriptor = widgetDescriptor;

    if (
      descriptor.get(WidgetConstants.JSONSCHEMA_FORM_SCHEMA_URL_KEY) != null
    ) {
      view.setSchemaUrl(
        descriptor.get(WidgetConstants.JSONSCHEMA_FORM_SCHEMA_URL_KEY)
      );
    }
    if (
      descriptor.get(WidgetConstants.JSONSCHEMA_FORM_UI_SCHEMA_URL_KEY) != null
    ) {
      view.setUiSchemaUrl(
        descriptor.get(WidgetConstants.JSONSCHEMA_FORM_UI_SCHEMA_URL_KEY)
      );
    }
    if (descriptor.get(WidgetConstants.JSONSCHEMA_FORM_POST_URL_KEY) != null) {
      view.setPostUrl(
        descriptor.get(WidgetConstants.JSONSCHEMA_FORM_POST_URL_KEY)
      );
    }
  }

  public void clearState() {
    view.clear();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void updateDescriptorFromView() {
    descriptor.clear();
    // update widget descriptor from the view
    String schemaUrl = view.getSchemaUrl();
    descriptor.put(WidgetConstants.JSONSCHEMA_FORM_SCHEMA_URL_KEY, schemaUrl);

    String uiSchemaUrl = view.getUiSchemaUrl();
    descriptor.put(
      WidgetConstants.JSONSCHEMA_FORM_UI_SCHEMA_URL_KEY,
      uiSchemaUrl
    );

    String postUrl = view.getPostUrl();
    descriptor.put(WidgetConstants.JSONSCHEMA_FORM_POST_URL_KEY, postUrl);
  }

  @Override
  public String getTextToInsert() {
    return null;
  }

  @Override
  public List<String> getNewFileHandleIds() {
    return null;
  }

  @Override
  public List<String> getDeletedFileHandleIds() {
    return null;
  }
}
