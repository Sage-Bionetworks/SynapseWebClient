package org.sagebionetworks.web.client.widget.table;

import static org.sagebionetworks.web.client.DisplayUtils.TEXTBOX_SELECT_ALL_FIELD_CLICKHANDLER;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.EntityRefCollectionView;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.widget.EntityTypeIcon;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeViewImpl;
import org.sagebionetworks.web.client.widget.user.UserBadge;

/**
 * Simple list item for an entity.
 */
public class TableEntityListGroupItem implements IsWidget {

  Anchor entityAnchor;

  @UiField
  FocusPanel iconContainer;

  @UiField
  TextBox idField;

  @UiField
  Label typeField;

  @UiField
  Div createdByField;

  @UiField
  Div modifiedByField;

  @UiField
  Label itemCountField;

  @UiField
  TableData itemCountColumn;

  @UiField
  Label modifiedOnField;

  @UiField
  Label createdOnField;

  @UiField
  FlowPanel entityContainer;

  @UiField
  EntityTypeIcon icon;

  public interface Binder
    extends UiBinder<IsWidget, TableEntityListGroupItem> {}

  public IsWidget w;
  private UserBadge createdByBadge;
  private UserBadge modifiedByBadge;
  private DateTimeUtils dateTimeUtils;
  private SynapseJavascriptClient jsClient;

  @Inject
  TableEntityListGroupItem(
    Binder binder,
    UserBadge createdByBadge,
    UserBadge modifiedByBadge,
    DateTimeUtils dateTimeUtils,
    PortalGinInjector ginInjector,
    SynapseJavascriptClient jsClient
  ) {
    w = binder.createAndBindUi(this);
    this.createdByBadge = createdByBadge;
    this.modifiedByBadge = modifiedByBadge;
    this.dateTimeUtils = dateTimeUtils;
    this.jsClient = jsClient;
    if (EntityBadgeViewImpl.placeChanger == null) {
      EntityBadgeViewImpl.placeChanger =
        ginInjector.getGlobalApplicationState().getPlaceChanger();
    }
    idField.addClickHandler(TEXTBOX_SELECT_ALL_FIELD_CLICKHANDLER);
  }

  public void configure(EntityHeader header, final ClickHandler clickHandler) {
    entityAnchor = new Anchor();
    entityAnchor.addClickHandler(EntityBadgeViewImpl.STANDARD_CLICKHANDLER);
    entityAnchor.setText(header.getName());
    entityAnchor.addStyleName("link");
    entityAnchor.setHref("#!Synapse:" + header.getId());
    entityAnchor
      .getElement()
      .setAttribute(EntityBadgeViewImpl.ENTITY_ID_ATTRIBUTE, header.getId());
    entityContainer.add(entityAnchor);
    idField.setText(header.getId());
    if (header.getCreatedBy() != null) {
      createdByBadge.configure(header.getCreatedBy());
      createdByField.add(createdByBadge);
    }
    if (header.getModifiedBy() != null) {
      modifiedByBadge.configure(header.getModifiedBy());
      modifiedByField.add(modifiedByBadge);
    }
    if (header.getModifiedOn() != null) {
      modifiedOnField.setText(
        dateTimeUtils.getDateTimeString(header.getModifiedOn())
      );
    }
    if (header.getCreatedOn() != null) {
      createdOnField.setText(
        dateTimeUtils.getDateTimeString(header.getCreatedOn())
      );
    }

    icon.setType(EntityTypeUtils.getEntityType(header));
    typeField.setText(
      EntityTypeUtils.getFriendlyTableTypeName(header.getType())
    );
    if (
      EntityType.dataset.equals(EntityTypeUtils.getEntityType(header)) ||
      EntityType.datasetcollection.equals(EntityTypeUtils.getEntityType(header))
    ) {
      jsClient.getEntityBundleFromCache(
        header.getId(),
        new AsyncCallback<EntityBundle>() {
          @Override
          public void onFailure(Throwable caught) {
            DisplayUtils.showErrorMessage(caught.getMessage());
          }

          @Override
          public void onSuccess(EntityBundle result) {
            EntityRefCollectionView dataset = (EntityRefCollectionView) result.getEntity();
            int itemCount = dataset.getItems() == null
              ? 0
              : dataset.getItems().size();
            itemCountField.setText(
              NumberFormat.getDecimalFormat().format(itemCount)
            );
          }
        }
      );
    }
  }

  @Override
  public Widget asWidget() {
    return w.asWidget();
  }

  public void setItemCountVisible(boolean visible) {
    this.itemCountColumn.setVisible(visible);
    if (visible) {
      itemCountColumn.addStyleName("visible-md visible-lg");
    } else {
      itemCountColumn.removeStyleName("visible-md visible-lg");
    }
  }
}
