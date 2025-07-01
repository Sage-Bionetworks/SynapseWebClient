package org.sagebionetworks.web.client.widget;

import static org.sagebionetworks.web.shared.WidgetConstants.JSONSCHEMA_FORM_POST_URL_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.JSONSCHEMA_FORM_SCHEMA_URL_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.JSONSCHEMA_FORM_UI_SCHEMA_URL_KEY;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Map;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.DynamicFormProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

public class DynamicForm implements IsWidget, WidgetRendererPresenter {

  private final SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public DynamicForm(SynapseReactClientFullContextPropsProvider propsProvider) {
    this.propsProvider = propsProvider;
  }

  ReactComponent component = new ReactComponent();

  public void configure(String schemaUrl, String uiSchemaUrl, String postUrl) {
    DynamicFormProps props = DynamicFormProps.create(
      schemaUrl,
      uiSchemaUrl,
      postUrl
    );
    ReactElement el = React.createElementWithSynapseContext(
      SRC.SynapseComponents.DynamicForm,
      props,
      propsProvider.getJsInteropContextProps()
    );
    component.render(el);
  }

  @Override
  public Widget asWidget() {
    return component;
  }

  @Override
  public void configure(
    WikiPageKey wikiKey,
    Map<String, String> widgetDescriptor,
    Callback widgetRefreshRequired,
    Long wikiVersionInView
  ) {
    String schemaUrl = widgetDescriptor.get(JSONSCHEMA_FORM_SCHEMA_URL_KEY);
    String uiSchemaUrl = widgetDescriptor.get(
      JSONSCHEMA_FORM_UI_SCHEMA_URL_KEY
    );
    String postUrl = widgetDescriptor.get(JSONSCHEMA_FORM_POST_URL_KEY);
    configure(schemaUrl, uiSchemaUrl, postUrl);
  }
}
