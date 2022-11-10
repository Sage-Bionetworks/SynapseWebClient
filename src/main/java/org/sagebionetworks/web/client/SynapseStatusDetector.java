package org.sagebionetworks.web.client;

import static org.sagebionetworks.web.client.ClientProperties.DEFAULT_PLACE_TOKEN;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import java.util.Date;
import org.sagebionetworks.repo.model.status.StackStatus;
import org.sagebionetworks.repo.model.status.StatusEnum;
import org.sagebionetworks.web.client.jsinterop.ToastMessageOptions;
import org.sagebionetworks.web.client.place.Down;

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

  @Inject
  public SynapseStatusDetector(
    GWTWrapper gwt,
    PopupUtilsView popupUtils,
    DateTimeUtils dateTimeUtils,
    StackConfigServiceAsync stackConfig,
    SynapseJSNIUtils jsniUtils,
    GlobalApplicationState globalAppState
  ) {
    this.gwt = gwt;
    this.popupUtils = popupUtils;
    this.dateTimeUtils = dateTimeUtils;
    this.stackConfig = stackConfig;
    this.jsniUtils = jsniUtils;
    this.globalAppState = globalAppState;
  }

  public void start() {
    _getCurrentStatus(this);
    _getUnresolvedIncidents(this);
    _getScheduledMaintenance(this);
    getSynapseStackStatus();

    gwt.scheduleFixedDelay(
      () -> {
        _getCurrentStatus(this);
        _getUnresolvedIncidents(this);
        _getScheduledMaintenance(this);
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
            globalAppState
              .getPlaceChanger()
              .goTo(new Down(DEFAULT_PLACE_TOKEN));
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

  public void showScheduledMaintenance(
    String name,
    String scheduledFor,
    String scheduledUntil
  ) {
    Date startDate = getDate(scheduledFor);
    String startTime = dateTimeUtils.getDateTimeString(startDate);
    boolean openInCurrentWindow = false;
    ToastMessageOptions toastOptions = new ToastMessageOptions.Builder()
      .setAutoCloseInMs(INTERVAL_MS - 1200)
      .setPrimaryButton(
        "Open Status Page",
        "http://status.synapse.org/",
        openInCurrentWindow
      )
      .build();
    popupUtils.notify(
      "Maintenance on " + startTime + " - " + name,
      DisplayUtils.NotificationVariant.INFO,
      toastOptions
    );
  }

  public Date getDate(String iso8601DateString) {
    Date result = null;
    try {
      result = iso8601DateFormat.parse(iso8601DateString);
    } catch (Exception e) {
      SynapseJSNIUtilsImpl._consoleError(e);
    }
    return result;
  }

  public void showOutage(String info) {
    boolean openInCurrentWindow = false;
    ToastMessageOptions toastOptions = new ToastMessageOptions.Builder()
      .setAutoCloseInMs(INTERVAL_MS - 1200)
      .setPrimaryButton(
        "Open Status Page",
        "http://status.synapse.org/",
        openInCurrentWindow
      )
      .build();
    popupUtils.notify(
      info,
      DisplayUtils.NotificationVariant.DANGER,
      toastOptions
    );
  }

  private static native void _getCurrentStatus(SynapseStatusDetector x) /*-{
		if ($wnd.StatusPage) {
			var sp = new $wnd.StatusPage.page(
				{
					page : @org.sagebionetworks.web.client.SynapseStatusDetector::STATUS_PAGE_IO_PAGE
				});
			sp.status({
				success : function(data) {
					if (data.status.indicator !== 'none') {
						// Houston, we have a problem...
						var description = data.status.description;
						x.@org.sagebionetworks.web.client.SynapseStatusDetector::showOutage(Ljava/lang/String;)(description);
					}
				}
			});
		}
	}-*/;

  private static native void _getUnresolvedIncidents(
    SynapseStatusDetector x
  ) /*-{
		if ($wnd.StatusPage) {
			var sp = new $wnd.StatusPage.page(
				{
					page : @org.sagebionetworks.web.client.SynapseStatusDetector::STATUS_PAGE_IO_PAGE
				});
			sp.incidents({
				filter : 'unresolved',
				success : function(data) {
					if (data.incidents[0]) {
						var incident = data.incidents[0];
						var description = incident.name;
						x.@org.sagebionetworks.web.client.SynapseStatusDetector::showOutage(Ljava/lang/String;)(description);
					}
				}
			})
		}
	}-*/;

  private static native void _getScheduledMaintenance(
    SynapseStatusDetector x
  ) /*-{
		if ($wnd.StatusPage) {
			var sp = new $wnd.StatusPage.page(
				{
					page : @org.sagebionetworks.web.client.SynapseStatusDetector::STATUS_PAGE_IO_PAGE
				});
			sp.scheduled_maintenances({
				filter : 'upcoming',
				success : function(data) {
					if (data.scheduled_maintenances[0]) {
						var scheduledMaintenance = data.scheduled_maintenances[0];
						var scheduledFor = scheduledMaintenance.scheduled_for;
						var scheduledUntil = scheduledMaintenance.scheduled_until;
						var name = scheduledMaintenance.name;
						// there's a scheduled maintenance. invoke alert with info.
						x.@org.sagebionetworks.web.client.SynapseStatusDetector::showScheduledMaintenance(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(name, scheduledFor, scheduledUntil);
					}
				}
			});
		}
	}-*/;
}
