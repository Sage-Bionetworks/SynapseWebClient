package org.sagebionetworks.web.client.widget.breadcrumb;

import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.jsinterop.BreadcrumbItem;
import org.sagebionetworks.web.client.jsinterop.EntityPageBreadcrumbsProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class BreadcrumbViewImpl implements BreadcrumbView {

  ReactComponent container;
  private Presenter presenter;

  @Inject
  public BreadcrumbViewImpl() {
    container = new ReactComponent();
  }

  @Override
  public Widget asWidget() {
    return container;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void setLinksList(List<LinkData> breadcrumbs) {
    setLinksList(breadcrumbs, null, null);
  }

  @Override
  public void setLinksList(List<LinkData> breadcrumbs, String current) {
    setLinksList(breadcrumbs, current, null);
  }

  @Override
  public void setLinksList(
    List<LinkData> breadcrumbs,
    String current,
    EntityType currentEntityType
  ) {
    List<BreadcrumbItem> items = breadcrumbs
      .stream()
      .map(data -> {
        String href = null;
        String iconType = getContainerIconType(data.getEntityType());
        if (data.getPlace() instanceof Synapse) {
          Synapse synapsePlace = (Synapse) data.getPlace();
          href =
            (DisplayUtils.getSynapseHistoryToken(
                synapsePlace.getEntityId(),
                synapsePlace.getVersionNumber(),
                synapsePlace.getArea(),
                synapsePlace.getAreaToken()
              ));
        }
        BreadcrumbItem.OnClick clickHandler = null;
        if (data.getPlace() != null) {
          clickHandler =
            event -> {
              if (!(DisplayUtils.isAnyModifierKeyDown(event))) {
                event.preventDefault();
                presenter.goTo(data.getPlace());
              }
            };
        }
        return BreadcrumbItem.create(
          data.getText(),
          false,
          href,
          clickHandler,
          iconType
        );
      })
      .collect(Collectors.toList());
    // If there's a "current" item, add it to the end of the list
    if (current != null) {
      String currentIconType = getContainerIconType(currentEntityType);
      items.add(
        BreadcrumbItem.create(current, true, null, null, currentIconType)
      );
    }

    EntityPageBreadcrumbsProps props = EntityPageBreadcrumbsProps.create(
      items.toArray(new BreadcrumbItem[0])
    );

    ReactElement element = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityPageBreadcrumbs,
      props
    );

    container.render(element);
  }

  // Only container entities (projects and folders) have icons in the breadcrumb.
  private static String getContainerIconType(EntityType type) {
    boolean isContainer =
      type == EntityType.project || type == EntityType.folder;
    return isContainer ? type.name() : null;
  }

  @Override
  public void showLoading() {
    // don't
  }

  @Override
  public void clear() {
    container.clear();
  }

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }
}
