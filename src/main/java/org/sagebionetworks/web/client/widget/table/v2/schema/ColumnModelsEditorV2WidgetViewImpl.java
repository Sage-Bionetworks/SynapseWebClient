package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.JSON;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.ReactRef;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.TableColumnSchemaFormProps;
import org.sagebionetworks.web.client.jsinterop.TableColumnSchemaFormRef;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class ColumnModelsEditorV2WidgetViewImpl
  implements ColumnModelsEditorV2WidgetView {

  public interface Binder
    extends UiBinder<Widget, ColumnModelsEditorV2WidgetViewImpl> {}

  private static final Binder uiBinder = GWT.create(Binder.class);

  @UiField
  ReactComponentDiv reactContainer;

  SynapseReactClientFullContextPropsProvider propsProvider;
  Widget widget;

  AdapterFactory adapterFactory;

  ReactRef<TableColumnSchemaFormRef> componentRef;

  @Inject
  public ColumnModelsEditorV2WidgetViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider,
    AdapterFactory adapterFactory
  ) {
    widget = uiBinder.createAndBindUi(this);
    this.propsProvider = propsProvider;
    this.adapterFactory = adapterFactory;
  }

  @Override
  public void configure(
    EntityType entityType,
    ViewScope viewScope,
    List<ColumnModel> startingModels
  ) {
    componentRef = React.createRef();
    TableColumnSchemaFormProps props = TableColumnSchemaFormProps.create(
      entityType,
      viewScope,
      startingModels,
      componentRef
    );
    ReactNode reactElement = React.createElementWithSynapseContext(
      SRC.SynapseComponents.TableColumnSchemaForm,
      props,
      propsProvider.getJsInteropContextProps()
    );
    reactContainer.render(reactElement);
  }

  @Override
  public List<ColumnModel> getEditedColumnModels() {
    Object[] editedColumnModels = componentRef.current.getEditedColumnModels();
    List<ColumnModel> asList = new ArrayList<>();
    for (Object o : editedColumnModels) {
      try {
        ColumnModel cm = new ColumnModel(
          adapterFactory.createNew(JSON.stringify(o))
        );
        asList.add(cm);
      } catch (JSONObjectAdapterException e) {
        e.printStackTrace();
      }
    }
    return asList;
  }

  @Override
  public boolean validate() {
    return componentRef.current.validate();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}
