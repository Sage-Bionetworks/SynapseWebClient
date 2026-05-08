package org.sagebionetworks.web.client.widget.header;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cookie.CookieProvider;

public class Header implements HeaderView.Presenter, IsWidget {

  public static final String WWW_SYNAPSE_ORG = "www.synapse.org";

  private HeaderView view;
  private SynapseJSNIUtils synapseJSNIUtils;
  CookieProvider cookies;

  @Inject
  public Header(
    HeaderView view,
    SynapseJSNIUtils synapseJSNIUtils,
    EventBus eventBus,
    CookieProvider cookies,
    ClientCache localStorage
  ) {
    this.view = view;
    this.cookies = cookies;
    this.synapseJSNIUtils = synapseJSNIUtils;
    view.clear();

    view.setPresenter(this);
    initStagingAlert();
    view.getEventBinder().bindEventHandlers(this, eventBus);
  }

  public void initStagingAlert() {
    String hostName = synapseJSNIUtils.getCurrentHostName().toLowerCase();
    boolean visible = !hostName.contains(WWW_SYNAPSE_ORG);
    view.setStagingAlertVisible(visible);
  }

  public void configure() {
    refresh();
  }

  public Widget asWidget() {
    view.setPresenter(this);
    return view.asWidget();
  }

  public void refresh() {
    view.refresh();
  }
}
