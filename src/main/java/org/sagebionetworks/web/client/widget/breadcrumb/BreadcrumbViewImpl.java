package org.sagebionetworks.web.client.widget.breadcrumb;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.BreadcrumbItem;
import org.sagebionetworks.web.client.jsinterop.EntityPageBreadcrumbsProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class BreadcrumbViewImpl implements BreadcrumbView {

  private final SynapseReactClientFullContextPropsProvider propsProvider;
  ReactComponentDiv container;
  private Presenter presenter;

  @Inject
  public BreadcrumbViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    this.propsProvider = propsProvider;
    container = new ReactComponentDiv();
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
    setLinksList(breadcrumbs, null);
  }

  @Override
  public void setLinksList(List<LinkData> breadcrumbs, String current) {
    List<BreadcrumbItem> items = breadcrumbs
      .stream()
      .map(data -> {
        String href = null;
        if (data.getPlace() instanceof Synapse) {
          Synapse synapsePlace = (Synapse) data.getPlace();
          href =
            (
              "#" +
              DisplayUtils.getSynapseHistoryTokenNoHash(
                synapsePlace.getEntityId(),
                synapsePlace.getVersionNumber(),
                synapsePlace.getArea(),
                synapsePlace.getAreaToken()
              )
            );
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
        return BreadcrumbItem.create(data.getText(), false, href, clickHandler);
      })
      .collect(Collectors.toList());
    // If there's a "current" item, add it to the end of the list
    if (current != null) {
      items.add(BreadcrumbItem.create(current, true, null, null));
    }

    EntityPageBreadcrumbsProps props = EntityPageBreadcrumbsProps.create(
      items.toArray(new BreadcrumbItem[0])
    );

    ReactNode element = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityPageBreadcrumbs,
      props,
      propsProvider.getJsInteropContextProps()
    );

    container.render(element);
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
