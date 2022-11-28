package org.sagebionetworks.web.client.widget.entity.restriction.v2;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_COLLECTOR_URL;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_DESCRIPTION_PART_1;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_DESCRIPTION_PART_2;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_PRIORITY;
import static org.sagebionetworks.web.shared.WebConstants.REVIEW_DATA_REQUEST_COMPONENT_ID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.RestrictionLevel;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;

public class RestrictionWidget
  implements RestrictionWidgetView.Presenter, SynapseWidgetPresenter, IsWidget {

  private AuthenticationController authenticationController;
  private RestrictionWidgetView view;
  private boolean showChangeLink, showCurrentAccessUI, showFolderRestrictionsUI;
  private Entity entity;
  private boolean canChangePermissions;
  private DataAccessClientAsync dataAccessClient;
  private SynapseAlert synAlert;
  private IsACTMemberAsyncHandler isACTMemberAsyncHandler;
  private SynapseJavascriptClient jsClient;
  GWTWrapper gwt;
  SynapseJSNIUtils jsniUtils;
  GlobalApplicationState globalAppState;

  @Inject
  public RestrictionWidget(
    RestrictionWidgetView view,
    AuthenticationController authenticationController,
    DataAccessClientAsync dataAccessClient,
    SynapseAlert synAlert,
    IsACTMemberAsyncHandler isACTMemberAsyncHandler,
    SynapseJavascriptClient jsClient,
    GWTWrapper gwt,
    SynapseJSNIUtils jsniUtils,
    GlobalApplicationState globalAppState
  ) {
    this.view = view;
    this.authenticationController = authenticationController;
    this.dataAccessClient = dataAccessClient;
    fixServiceEntryPoint(dataAccessClient);
    this.synAlert = synAlert;
    this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
    this.jsClient = jsClient;
    this.gwt = gwt;
    this.jsniUtils = jsniUtils;
    this.globalAppState = globalAppState;
    view.setSynAlert(synAlert.asWidget());
    view.setPresenter(this);
    //by default, show the current users access to the given entity.
    showCurrentAccessUI = true;
    // by default, we are not showing the folder restrictions UI
    showFolderRestrictionsUI = false;
  }

  public void configure(Entity entity, boolean canChangePermissions) {
    this.entity = entity;
    this.canChangePermissions = canChangePermissions;
    loadRestrictionInformation();
    if (showCurrentAccessUI) {
      Long versionNumber = null;
      if (entity instanceof Versionable) {
        versionNumber = ((Versionable) entity).getVersionNumber();
      }
      view.configureCurrentAccessComponent(entity.getId(), versionNumber);
    }
  }

  public void setShowChangeLink(boolean showChangeLink) {
    this.showChangeLink = showChangeLink;
  }

  public void showFolderRestrictionUI() {
    showFolderRestrictionsUI = true;
    view.showFolderRestrictionUI();
  }

  public boolean isAnonymous() {
    return !authenticationController.isLoggedIn();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void loadRestrictionInformation() {
    view.clear();
    synAlert.clear();
    jsClient.getRestrictionInformation(
      entity.getId(),
      RestrictableObjectType.ENTITY,
      new AsyncCallback<RestrictionInformationResponse>() {
        @Override
        public void onSuccess(
          RestrictionInformationResponse restrictionInformation
        ) {
          configureUI(restrictionInformation);
        }

        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
        }
      }
    );
  }

  public void configureUI(
    RestrictionInformationResponse restrictionInformation
  ) {
    boolean isAnonymous = isAnonymous();
    boolean hasAdministrativeAccess = false;

    if (!isAnonymous) {
      hasAdministrativeAccess = canChangePermissions;
    }
    RestrictionLevel restrictionLevel = restrictionInformation.getRestrictionLevel();

    switch (restrictionLevel) {
      case OPEN:
        view.showNoRestrictionsUI();
        break;
      case RESTRICTED_BY_TERMS_OF_USE:
      case CONTROLLED_BY_ACT:
        view.showControlledUseUI();
        if (showFolderRestrictionsUI) {
          // also show the link to the terms
          view.showFolderRestrictionsLink(entity.getId());
        }
        break;
      default:
        throw new IllegalArgumentException(restrictionLevel.toString());
    }

    // ARs can technically be applied, but they don't work in a useful way, so hide the button (SWC-5909)
    boolean entityTypeAllowsRestrictions =
      !(entity instanceof EntityView) && !(entity instanceof Dataset);

    // show the info link if there are any restrictions, or if we are supposed to show the flag link (to
    // allow people to flag or admin to "change" the data access level).
    boolean isChangeLink =
      restrictionLevel == RestrictionLevel.OPEN &&
      hasAdministrativeAccess &&
      entityTypeAllowsRestrictions;
    boolean isRestricted = restrictionLevel != RestrictionLevel.OPEN;
    if ((isChangeLink && showChangeLink) || isRestricted) {
      if (isChangeLink) view.showChangeLink();
    }
  }

  @Override
  public void imposeRestrictionOkClicked() {
    Boolean isYesSelected = view.isYesHumanDataRadioSelected();
    Boolean isNoSelected = view.isNoHumanDataRadioSelected();

    if (isNoSelected != null && isNoSelected) {
      // no-op, just hide the dialog
      imposeRestrictionCancelClicked();
    } else if (
      (isYesSelected == null || !isYesSelected) &&
      (isNoSelected == null || !isNoSelected)
    ) {
      // no selection
      view.showErrorMessage("You must make a selection before continuing.");
    } else {
      view.showLoading();
      view.setImposeRestrictionModalVisible(false);

      dataAccessClient.createLockAccessRequirement(
        entity.getId(),
        new AsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            view.showInfo("Successfully imposed restriction");
            // reconfigure (reload restriction information and reconfigure HasAccess)
            configure(entity, canChangePermissions);
          }

          @Override
          public void onFailure(Throwable caught) {
            synAlert.handleException(caught);
          }
        }
      );
    }
  }

  @Override
  public void imposeRestrictionCancelClicked() {
    view.setImposeRestrictionModalVisible(false);
  }

  @Override
  public void notHumanDataClicked() {
    // and show the warning message
    view.setNotSensitiveHumanDataMessageVisible(true);
  }

  @Override
  public void yesHumanDataClicked() {
    // and hide the warning message
    view.setNotSensitiveHumanDataMessageVisible(false);
  }

  @Override
  public void changeClicked() {
    isACTMemberAsyncHandler.isACTActionAvailable(
      new CallbackP<Boolean>() {
        @Override
        public void invoke(Boolean isACT) {
          if (isACT) {
            // go to access requirements place where they can modify access requirements
            AccessRequirementsPlace place = new AccessRequirementsPlace("");
            place.putParam(AccessRequirementsPlace.ID_PARAM, entity.getId());
            place.putParam(
              AccessRequirementsPlace.TYPE_PARAM,
              RestrictableObjectType.ENTITY.toString()
            );
            globalAppState.getPlaceChanger().goTo(place);
          } else {
            view.showVerifyDataSensitiveDialog();
          }
        }
      }
    );
  }

  public void setShowCurrentAccessUI(boolean showCurrentAccessUI) {
    this.showCurrentAccessUI = showCurrentAccessUI;
  }
}
