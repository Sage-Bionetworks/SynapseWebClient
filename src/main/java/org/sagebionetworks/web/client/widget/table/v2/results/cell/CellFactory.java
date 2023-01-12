package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.inject.Inject;
import java.util.function.Consumer;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorButton;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer.RenderOption;

/**
 * Factory for creating table cells.
 *
 * @author John
 *
 */
public class CellFactory {

  PortalGinInjector ginInjector;

  @Inject
  public CellFactory(PortalGinInjector ginInjector) {
    this.ginInjector = ginInjector;
  }

  public Cell createRenderer(ColumnModel model) {
    // SWC-5806: Special case the "id" column, to only show the Synapse ID.
    if (
      model.getName() == "id" && model.getColumnType() == ColumnType.ENTITYID
    ) {
      EntityIdCellRenderer renderer = ginInjector.createEntityIdCellRenderer();
      renderer.setRenderOption(RenderOption.ID);
      return renderer;
    }
    return createRenderer(model.getColumnType());
  }

  public Cell createRenderer(ColumnType type) {
    switch (type) {
      case ENTITYID:
        return ginInjector.createEntityIdCellRenderer();
      case DATE:
        return ginInjector.createDateCellRenderer();
      case LINK:
        return ginInjector.createLinkCellRenderer();
      case FILEHANDLEID:
        return ginInjector.createFileCellRenderer();
      case USERID:
        return ginInjector.createUserIdCellRenderer();
      case STRING_LIST:
      case BOOLEAN_LIST:
      case INTEGER_LIST:
        return ginInjector.createStringListRendererCellView();
      case ENTITYID_LIST:
        return ginInjector.createEntityIdListRendererCellView();
      case USERID_LIST:
        return ginInjector.createUserIdListRendererCellView();
      case DATE_LIST:
        return ginInjector.createDateListRendererCellView();
      //TODO: add special renderers for entity id list and user id list
      default:
        return ginInjector.createStringRendererCellView();
    }
  }

  public CellEditor createEditor(ColumnModel model) {
    CellEditor editor;
    // enums get their own special editor
    if (model.getEnumValues() != null && !model.getEnumValues().isEmpty()) {
      EnumCellEditor enumEditor = ginInjector.createEnumCellEditor();
      enumEditor.configure(model.getEnumValues());
      editor = enumEditor;
    } else {
      switch (model.getColumnType()) {
        case STRING_LIST:
        case INTEGER_LIST:
        case BOOLEAN_LIST:
        case DATE_LIST:
        case ENTITYID_LIST:
        case USERID_LIST:
          JSONListCellEditor listEditor = ginInjector.createJSONListCellEditor();
          listEditor.setColumnModel(model);
          editor = listEditor;
          break;
        case DATE:
          editor = ginInjector.createDateCellEditor();
          break;
        case BOOLEAN:
          editor = ginInjector.createBooleanCellEditor();
          break;
        case ENTITYID:
          editor = ginInjector.createEntityIdCellEditor();
          break;
        case DOUBLE:
          editor = ginInjector.createDoubleCellEditor();
          break;
        case INTEGER:
          editor = ginInjector.createIntegerCellEditor();
          break;
        case FILEHANDLEID:
          editor = ginInjector.createFileCellEditor();
          break;
        case USERID:
          editor = ginInjector.createUserIdCellEditor();
          break;
        default:
          StringEditorCell stringEditor = ginInjector.createStringEditorCell();
          stringEditor.setMaxSize(model.getMaximumSize());
          editor = stringEditor;
      }
    }
    // Configure each editor with the default value.
    editor.setValue(model.getDefaultValue());
    return editor;
  }

  public CellEditor createFormEditor(ColumnModel model) {
    CellEditor editor;
    // enums get their own special editor
    if (model.getEnumValues() != null && !model.getEnumValues().isEmpty()) {
      EnumFormCellEditor enumEditor = ginInjector.createEnumFormCellEditor();
      enumEditor.configure(model.getEnumValues());
      editor = enumEditor;
    } else {
      switch (model.getColumnType()) {
        case STRING_LIST:
        case INTEGER_LIST:
        case BOOLEAN_LIST:
        case DATE_LIST:
          //				case ENTITYID_LIST:
          //				case USERID_LIST:
          JSONListCellEditor listEditor = ginInjector.createJSONListCellEditor();
          listEditor.setColumnModel(model);
          editor = listEditor;
          break;
        case DATE:
          editor = ginInjector.createDateCellEditor();
          break;
        case BOOLEAN:
          editor = ginInjector.createBooleanFormCellEditor();
          break;
        case ENTITYID:
          editor = ginInjector.createEntityIdCellEditor();
          break;
        case DOUBLE:
          editor = ginInjector.createDoubleCellEditor();
          break;
        case INTEGER:
          editor = ginInjector.createIntegerCellEditor();
          break;
        case FILEHANDLEID:
          editor = ginInjector.createFileCellEditor();
          break;
        case USERID:
          editor = ginInjector.createUserIdCellEditor();
          break;
        case MEDIUMTEXT:
        case LARGETEXT:
          editor = ginInjector.createLargeTextFormCellEditor();
          break;
        default:
          StringEditorCell stringEditor = ginInjector.createStringEditorCell();
          stringEditor.setMaxSize(model.getMaximumSize());
          editor = stringEditor;
      }
    }
    // Configure each editor with the default value.
    editor.setValue(model.getDefaultValue());
    return editor;
  }

  public static TableRow appendDeleteButton(
    final CellEditor editor,
    Consumer<CellEditor> additionalDeleteCallback
  ) {
    TableRow row = new TableRow();
    Button deleteButton = new Button();
    deleteButton.setIcon(IconType.TIMES);
    deleteButton.setType(ButtonType.LINK);
    deleteButton.addClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          row.removeFromParent();
          additionalDeleteCallback.accept(editor);
        }
      }
    );
    deleteButton.setSize(ButtonSize.EXTRA_SMALL);
    deleteButton.addStyleName("center-in-div");

    TableData deleteButtonWrapper = new TableData();
    deleteButtonWrapper.add(deleteButton);
    deleteButtonWrapper.setWidth("35px");

    TableData editorTableData = new TableData();
    editorTableData.add(editor.asWidget());

    row.add(editorTableData);
    row.add(deleteButtonWrapper);

    return row;
  }
}
