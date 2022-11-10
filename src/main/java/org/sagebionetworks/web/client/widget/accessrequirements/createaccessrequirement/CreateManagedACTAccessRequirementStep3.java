package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.HashSet;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.team.UserTeamBadge;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

/**
 * Second page of creating an access requirement (ACT)
 *
 * @author Jay
 *
 */
public class CreateManagedACTAccessRequirementStep3
  implements ModalPage, CreateManagedACTAccessRequirementStep3View.Presenter {

  CreateManagedACTAccessRequirementStep3View view;
  ModalPresenter modalPresenter;
  ManagedACTAccessRequirement accessRequirement;
  SynapseJavascriptClient jsClient;
  AccessControlList originalAcl;
  String reviewerPrincipalId;
  UserTeamBadge reviewerUserTeamBadge;
  public static final String TOO_MANY_ACL_ENTRIES_ERROR =
    "This Access Requirment has an ACL that contains more than one item.  Beware, this dialog will destroy other entries on save.";

  @Inject
  public CreateManagedACTAccessRequirementStep3(
    CreateManagedACTAccessRequirementStep3View view,
    SynapseJavascriptClient jsClient,
    SynapseSuggestBox suggestBox,
    UserGroupSuggestionProvider provider,
    UserTeamBadge reviewerUserTeamBadge
  ) {
    super();
    this.view = view;
    this.jsClient = jsClient;
    this.reviewerUserTeamBadge = reviewerUserTeamBadge;
    view.setPresenter(this);
    suggestBox.setSuggestionProvider(provider);
    suggestBox.setTypeFilter(TypeFilter.ALL);
    suggestBox.setPlaceholderText(
      "Username, name (first and last) or team name."
    );
    view.setReviewerSearchBox(suggestBox);
    view.setReviewerBadge(reviewerUserTeamBadge);

    suggestBox.addItemSelectedHandler(
      new CallbackP<UserGroupSuggestion>() {
        public void invoke(UserGroupSuggestion suggestion) {
          onSynapseSuggestSelected(suggestion);
          suggestBox.clear();
        }
      }
    );
  }

  public void onSynapseSuggestSelected(final UserGroupSuggestion suggestion) {
    updateReviewerPrincipalId(suggestion.getHeader().getOwnerId());
  }

  @Override
  public void onRemoveReviewer() {
    updateReviewerPrincipalId(null);
  }

  private void updateReviewerPrincipalId(String principalId) {
    reviewerPrincipalId = principalId;
    if (principalId != null) {
      reviewerUserTeamBadge.configure(reviewerPrincipalId);
      view.setReviewerUIVisible(true);
    } else {
      view.setReviewerUIVisible(false);
    }
  }

  /**
   * Configure this widget before use.
   *
   */
  public void configure(ManagedACTAccessRequirement accessRequirement) {
    this.accessRequirement = accessRequirement;
    // GET the AR ACL
    modalPresenter.setLoading(true);
    modalPresenter.clearErrors();
    FluentFuture<AccessControlList> getAclFuture = jsClient.getAccessRequirementACL(
      accessRequirement.getId().toString()
    );
    getAclFuture.addCallback(
      new FutureCallback<AccessControlList>() {
        @Override
        public void onSuccess(AccessControlList accessRequirementACL) {
          originalAcl = accessRequirementACL;
          //initialize reviewer principal ID
          if (
            originalAcl.getResourceAccess() != null &&
            !originalAcl.getResourceAccess().isEmpty()
          ) {
            // Edge case detection (user set up this ACL via another client)
            if (originalAcl.getResourceAccess().size() > 1) {
              modalPresenter.setErrorMessage(TOO_MANY_ACL_ENTRIES_ERROR);
            }
            ResourceAccess firstRA = originalAcl
              .getResourceAccess()
              .iterator()
              .next();
            updateReviewerPrincipalId(firstRA.getPrincipalId().toString());
          }
          modalPresenter.setLoading(false);
        }

        @Override
        public void onFailure(Throwable t) {
          if (t instanceof NotFoundException) {
            originalAcl = null;
            updateReviewerPrincipalId(null);
          } else {
            modalPresenter.setError(t);
          }
          modalPresenter.setLoading(false);
        }
      },
      directExecutor()
    );
  }

  @Override
  public void onPrimary() {
    modalPresenter.setLoading(true);
    // if not set, then delete the ACL (if it was originally set) or do nothing (if not set)
    if (reviewerPrincipalId == null) {
      if (originalAcl != null) {
        deleteAcl();
      } else {
        // nothing to do, finish!
        modalPresenter.onFinished();
      }
    } else {
      // if user/team is set, then create (if not originally set) or update the ACL (if previously set).
      if (originalAcl == null) {
        createAcl();
      } else {
        updateAcl();
      }
    }
  }

  private void deleteAcl() {
    FluentFuture<Void> deleteAclFuture = jsClient.deleteAccessRequirementACL(
      accessRequirement.getId().toString()
    );
    deleteAclFuture.addCallback(
      new FutureCallback<Void>() {
        @Override
        public void onSuccess(Void v) {
          modalPresenter.onFinished();
        }

        @Override
        public void onFailure(Throwable t) {
          modalPresenter.setError(t);
        }
      },
      directExecutor()
    );
  }

  // based on the reviewer shown in the view, get a new ACL resource access set
  private HashSet<ResourceAccess> getNewResourceAccessSet() {
    HashSet<ResourceAccess> resourceAccessSet = new HashSet<>();
    ResourceAccess newRA = new ResourceAccess();
    newRA.setAccessType(
      AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_REVIEW_SUBMISSIONS)
    );
    newRA.setPrincipalId(Long.parseLong(reviewerPrincipalId));
    resourceAccessSet.add(newRA);
    return resourceAccessSet;
  }

  private void createAcl() {
    AccessControlList newAcl = new AccessControlList();
    newAcl.setResourceAccess(getNewResourceAccessSet());

    FluentFuture<AccessControlList> createAclFuture = jsClient.createAccessRequirementACL(
      accessRequirement.getId().toString(),
      newAcl
    );
    createAclFuture.addCallback(
      new FutureCallback<AccessControlList>() {
        @Override
        public void onSuccess(AccessControlList acl) {
          modalPresenter.onFinished();
        }

        @Override
        public void onFailure(Throwable t) {
          modalPresenter.setError(t);
        }
      },
      directExecutor()
    );
  }

  private void updateAcl() {
    originalAcl.setResourceAccess(getNewResourceAccessSet());
    FluentFuture<AccessControlList> updateAclFuture = jsClient.updateAccessRequirementACL(
      accessRequirement.getId().toString(),
      originalAcl
    );
    updateAclFuture.addCallback(
      new FutureCallback<AccessControlList>() {
        @Override
        public void onSuccess(AccessControlList acl) {
          modalPresenter.onFinished();
        }

        @Override
        public void onFailure(Throwable t) {
          modalPresenter.setError(t);
        }
      },
      directExecutor()
    );
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void setModalPresenter(ModalPresenter modalPresenter) {
    this.modalPresenter = modalPresenter;
    modalPresenter.setTitle(
      "People with permission in this Access Requirement"
    );
    modalPresenter.setPrimaryButtonText(DisplayConstants.FINISH);
  }
}
