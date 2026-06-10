package org.sagebionetworks.web.client;

import static org.sagebionetworks.web.shared.WebConstants.REPO_SERVICE_URL_KEY;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import javax.inject.Inject;
import org.sagebionetworks.repo.model.status.StackStatus;
import org.sagebionetworks.repo.model.status.StatusEnum;
import org.sagebionetworks.web.client.cache.SessionStorage;

public class SynapseStatusDetector {

  public static final int INTERVAL_MS = 1000 * 60; // check once every minute
  PopupUtilsView popupUtils;
  GWTWrapper gwt;
  public static final String STATUS_PAGE_IO_PAGE = "kh896k90gyvg";
  DateTimeFormat iso8601DateFormat = DateTimeFormat.getFormat(
    PredefinedFormat.ISO_8601
  );
  DateTimeUtils dateTimeUtils;
  StackConfigServiceAsync stackConfig;
  SynapseJSNIUtils jsniUtils;
  GlobalApplicationState globalAppState;
  SynapseProperties synapseProperties;
  SessionStorage sessionStorage;

  @Inject
  public SynapseStatusDetector(
    GWTWrapper gwt,
    PopupUtilsView popupUtils,
    DateTimeUtils dateTimeUtils,
    StackConfigServiceAsync stackConfig,
    SynapseJSNIUtils jsniUtils,
    GlobalApplicationState globalAppState,
    SynapseProperties synapseProperties,
    SessionStorage sessionStorage
  ) {
    this.gwt = gwt;
    this.popupUtils = popupUtils;
    this.dateTimeUtils = dateTimeUtils;
    this.stackConfig = stackConfig;
    this.jsniUtils = jsniUtils;
    this.globalAppState = globalAppState;
    this.synapseProperties = synapseProperties;
    this.sessionStorage = sessionStorage;
  }

  public void start() {
    getSynapseStackStatus();

    gwt.scheduleFixedDelay(
      () -> {
        getSynapseStackStatus();
      },
      INTERVAL_MS
    );
  }

  public void getSynapseStackStatus() {
    stackConfig.getCurrentStatus(
      new AsyncCallback<StackStatus>() {
        @Override
        public void onSuccess(StackStatus status) {
          if (StatusEnum.READ_WRITE != status.getStatus()) {
            // Synapse is down (RO mode or Down)
            String repoServiceUrl = synapseProperties.getSynapseProperty(
              REPO_SERVICE_URL_KEY
            );
            sessionStorage.setItem("repoServiceUrl", repoServiceUrl);
            sessionStorage.setItem("returnUrl", gwt.getCurrentURL());
            gwt.assignThisWindowWith("/ServerDown.html");
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          jsniUtils.consoleError(
            "Unable to get Synapse stack status: " + caught.getMessage()
          );
        }
      }
    );
  }
}
