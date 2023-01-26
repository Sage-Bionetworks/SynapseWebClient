package org.sagebionetworks.web.client.widget.header;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DateTimeUtilsImpl;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;

public class Header implements HeaderView.Presenter, IsWidget {

  public static final String WWW_SYNAPSE_ORG = "www.synapse.org";

  private HeaderView view;
  private SynapseJSNIUtils synapseJSNIUtils;
  CookieProvider cookies;
  private ClientCache localStorage;
  public static boolean isShowingPortalAlert = false;
  public static JSONObjectAdapter portalAlertJson = null;

  @Inject
  public Header(
    HeaderView view,
    SynapseJSNIUtils synapseJSNIUtils,
    EventBus eventBus,
    CookieProvider cookies,
    ClientCache localStorage,
    JSONObjectAdapter jsonObjectAdapter
  ) {
    this.view = view;
    this.cookies = cookies;
    this.localStorage = localStorage;
    this.synapseJSNIUtils = synapseJSNIUtils;
    view.clear();

    view.setPresenter(this);
    initStagingAlert();
    view.getEventBinder().bindEventHandlers(this, eventBus);
    view.setCookieNotificationVisible(
      !localStorage.contains(AuthenticationControllerImpl.COOKIES_ACCEPTED)
    );
    view.setNIHAlertVisible(
      !localStorage.contains(
        AuthenticationControllerImpl.NIH_NOTIFICATION_DISMISSED
      )
    );

    // portal alert state sticks around for entire app session
    String portalAlertString = cookies.getCookie(CookieKeys.PORTAL_CONFIG);
    isShowingPortalAlert = portalAlertString != null;
    if (isShowingPortalAlert) {
      cookies.removeCookie(CookieKeys.PORTAL_CONFIG);
      try {
        portalAlertJson = jsonObjectAdapter.createNew(portalAlertString);
      } catch (JSONObjectAdapterException e) {
        synapseJSNIUtils.consoleError(e);
      }
    } else {
      portalAlertJson = null;
    }
    view.setPortalAlertVisible(isShowingPortalAlert, portalAlertJson);
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
    view.setPortalAlertVisible(isShowingPortalAlert, portalAlertJson);
    view.refresh();
  }

  @Override
  public void onCookieNotificationDismissed() {
    view.setCookieNotificationVisible(false);
    localStorage.put(
      AuthenticationControllerImpl.COOKIES_ACCEPTED,
      Boolean.TRUE.toString(),
      DateTimeUtilsImpl.getYearFromNow().getTime()
    );
  }

  @Override
  public void onNIHNotificationDismissed() {
    view.setNIHAlertVisible(false);
    localStorage.put(
      AuthenticationControllerImpl.NIH_NOTIFICATION_DISMISSED,
      Boolean.TRUE.toString(),
      DateTimeUtilsImpl.getYearFromNow().getTime()
    );
  }
}
