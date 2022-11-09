package org.sagebionetworks.web.client.widget.team;

import static org.sagebionetworks.web.client.ValidationUtils.isValidEmail;
import static org.sagebionetworks.web.client.presenter.ProfilePresenter.IS_CERTIFIED;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoDialog;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;

public class InviteWidget implements InviteWidgetView.Presenter {

  public static final String NO_USERS_OR_EMAILS_ADDED_ERROR_MESSAGE =
    "Please add at least one user or email address and try again.";
  public static final String INVALID_EMAIL_ERROR_MESSAGE =
    "Please select a suggested user or provide a valid email address and try again.";
  private InviteWidgetView view;
  private Team team;
  private Callback teamUpdatedCallback;
  private SynapseAlert synAlert;
  private SynapseSuggestBox peopleSuggestWidget;
  private List<String> inviteEmails, inviteUsers;
  private String currentlyProcessingEmail, currentlyProcessingUser, invitationMessage;
  private PortalGinInjector ginInjector;
  private AsyncCallback<MembershipInvitation> inviteCallback;
  private AsyncCallback<Void> voidInviteCallback;
  private boolean isCertified;
  private QuizInfoDialog quizInfoDialog;

  @Inject
  public InviteWidget(
    InviteWidgetView view,
    SynapseAlert synAlert,
    SynapseSuggestBox peopleSuggestBox,
    UserGroupSuggestionProvider provider,
    PortalGinInjector ginInjector
  ) {
    this.view = view;
    this.synAlert = synAlert;
    this.ginInjector = ginInjector;
    this.peopleSuggestWidget = peopleSuggestBox;
    peopleSuggestWidget.setSuggestionProvider(provider);
    peopleSuggestWidget.setTypeFilter(TypeFilter.USERS_ONLY);
    peopleSuggestWidget.setPlaceholderText("Enter a name or email address...");
    view.setSuggestWidget(peopleSuggestBox.asWidget());
    view.setSynAlertWidget(synAlert.asWidget());
    peopleSuggestWidget.addKeyDownHandler(event -> {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        addSuggestion();
      }
    });
    peopleSuggestWidget.addItemSelectedHandler(suggestion -> {
      if (suggestion != null) {
        addSuggestion();
      }
    });
    inviteCallback =
      new AsyncCallback<MembershipInvitation>() {
        @Override
        public void onSuccess(MembershipInvitation result) {
          if (currentlyProcessingEmail != null) {
            inviteEmails.remove(currentlyProcessingEmail);
          } else if (currentlyProcessingUser != null) {
            inviteUsers.remove(currentlyProcessingUser);
          }
          doSendInvites();
        }

        @Override
        public void onFailure(Throwable caught) {
          synAlert.showError(caught.getMessage());
          refreshInvitees();
        }
      };
    voidInviteCallback =
      new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
          inviteCallback.onFailure(caught);
        }

        @Override
        public void onSuccess(Void result) {
          inviteCallback.onSuccess(null);
        }
      };
    view.setPresenter(this);
  }

  public QuizInfoDialog getQuizInfoDialog() {
    if (quizInfoDialog == null) {
      quizInfoDialog = ginInjector.getQuizInfoDialog();
    }
    return quizInfoDialog;
  }

  /**
   * @return true if successfully added an item (or no item was to be added), false if the input
   *         suggestion was invalid
   */
  public boolean addSuggestion() {
    synAlert.clear();
    String input = peopleSuggestWidget.getText();
    if (isValidEmail(input)) {
      if (!isCertified) {
        view.hide();
        getQuizInfoDialog().show();
        return false;
      }
      inviteEmails.add(input);
      view.addEmailToInvite(input);
      peopleSuggestWidget.clear();
    } else if (peopleSuggestWidget.getSelectedSuggestion() != null) {
      String userId = peopleSuggestWidget.getSelectedSuggestion().getId();
      inviteUsers.add(userId);
      view.addUserToInvite(userId);
      peopleSuggestWidget.clear();
    } else if (
      input != null &&
      !input.isEmpty() &&
      peopleSuggestWidget.getSelectedSuggestion() == null
    ) {
      synAlert.showError(INVALID_EMAIL_ERROR_MESSAGE);
      return false;
    }
    return true;
  }

  @Override
  public void configure(Team team) {
    clear();
    inviteEmails = new ArrayList<String>();
    inviteUsers = new ArrayList<String>();
    this.team = team;
    isCertified = false;

    ginInjector
      .getSynapseJavascriptClient()
      .getUserBundle(
        Long.parseLong(
          ginInjector.getAuthenticationController().getCurrentUserPrincipalId()
        ),
        IS_CERTIFIED,
        new AsyncCallback<UserBundle>() {
          @Override
          public void onSuccess(UserBundle bundle) {
            isCertified = bundle.getIsCertified();
          }

          @Override
          public void onFailure(Throwable caught) {
            synAlert.handleException(caught);
          }
        }
      );
  }

  public void clear() {
    view.clear();
    peopleSuggestWidget.clear();
    synAlert.clear();
  }

  public Widget asWidget() {
    return view.asWidget();
  }

  public void setRefreshCallback(Callback teamUpdatedCallback) {
    this.teamUpdatedCallback = teamUpdatedCallback;
  }

  @Override
  public void doSendInvites(String invitationMessage) {
    // if anything is in the invitation field, then pick it up before processing
    synAlert.clear();
    if (addSuggestion()) {
      if (inviteEmails.isEmpty() && inviteUsers.isEmpty()) {
        synAlert.showError(NO_USERS_OR_EMAILS_ADDED_ERROR_MESSAGE);
        return;
      }
      view.setLoading(true);

      this.invitationMessage = invitationMessage;
      doSendInvites();
    }
  }

  /**
   * Recursively process the user invitations (emails, then Synapse users).
   */
  public void doSendInvites() {
    currentlyProcessingUser = null;
    currentlyProcessingEmail = null;
    if (!inviteEmails.isEmpty()) {
      // kick off the next email invite
      currentlyProcessingEmail = inviteEmails.get(0);
      MembershipInvitation invite = new MembershipInvitation();
      invite.setInviteeEmail(currentlyProcessingEmail);
      invite.setTeamId(team.getId());
      invite.setMessage(invitationMessage);
      ginInjector
        .getSynapseJavascriptClient()
        .createMembershipInvitation(invite, inviteCallback);
    } else if (!inviteUsers.isEmpty()) {
      // kick off the next user invite
      currentlyProcessingUser = inviteUsers.get(0);
      // get the current team status
      ginInjector
        .getSynapseJavascriptClient()
        .getTeamMembershipStatus(
          team.getId(),
          currentlyProcessingUser,
          new AsyncCallback<TeamMembershipStatus>() {
            @Override
            public void onFailure(Throwable caught) {
              // something went wrong trying to get the status.  Just try to invite.
              synAlert.consoleError(caught.getMessage());
              createMembershipInvitation();
            }

            public void onSuccess(TeamMembershipStatus membershipStatus) {
              if (membershipStatus.getIsMember()) {
                // no-op.  go to next
                inviteCallback.onSuccess(null);
                return;
              }
              // if we can join the team without creating the invite (like if we
              // are a team admin, or there is an open membership request), then
              // just do that!
              if (membershipStatus.getCanJoin()) {
                ginInjector
                  .getSynapseJavascriptClient()
                  .addTeamMember(
                    currentlyProcessingUser,
                    team.getId(),
                    voidInviteCallback
                  );
              } else if (!membershipStatus.getHasOpenInvitation()) {
                createMembershipInvitation();
              }
            }

            private void createMembershipInvitation() {
              MembershipInvitation invite = new MembershipInvitation();
              invite.setInviteeId(currentlyProcessingUser);
              invite.setTeamId(team.getId());
              invite.setMessage(invitationMessage);
              ginInjector
                .getSynapseJavascriptClient()
                .createMembershipInvitation(invite, inviteCallback);
            }
          }
        );
    } else {
      // done!
      view.hide();
      view.showInfo("Invitation(s) Sent");
      teamUpdatedCallback.invoke();
      view.setLoading(false);
    }
  }

  @Override
  public void show() {
    clear();
    view.show();
  }

  @Override
  public void hide() {
    view.hide();
  }

  @Override
  public void removeEmailToInvite(String email) {
    inviteEmails.remove(email);
  }

  @Override
  public void removeUserToInvite(String userId) {
    inviteUsers.remove(userId);
  }

  public void refreshInvitees() {
    view.clear();
    view.setLoading(false);
    for (String email : inviteEmails) {
      view.addEmailToInvite(email);
    }
    for (String userId : inviteUsers) {
      view.addUserToInvite(userId);
    }
  }
}
