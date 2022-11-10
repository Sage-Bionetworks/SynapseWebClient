package org.sagebionetworks.web.client.widget.search;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.Portal;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class UserGroupSuggestion
  implements IsSerializable, SuggestOracle.Suggestion {

  private UserGroupHeader header;
  private String prefix;
  private int width;
  public static final String DATA_USER_GROUP_ID = "data-user-group-id";
  public static final String DATA_IS_INDIVIDUAL = "data-is-individual";

  UserProfileAsyncHandler userProfileAsyncHandler;

  public UserGroupSuggestion() {}

  public UserGroupSuggestion(
    UserGroupHeader header,
    String prefix,
    int width,
    UserProfileAsyncHandler userProfileAsyncHandler
  ) {
    this.header = header;
    this.prefix = prefix;
    this.width = width;
    this.userProfileAsyncHandler = userProfileAsyncHandler;
  }

  @Override
  public String getDisplayString() {
    StringBuilder result = new StringBuilder();
    String uniqueId = HTMLPanel.createUniqueId();
    result.append(
      "<div id=\"" +
      uniqueId +
      "\" " +
      DATA_IS_INDIVIDUAL +
      "=\"" +
      header.getIsIndividual() +
      "\" " +
      DATA_USER_GROUP_ID +
      "=\"" +
      header.getOwnerId() +
      "\" class=\"padding-left-5 userGroupSuggestion\" style=\"height:28px; width:" +
      width +
      "px;\"></div>"
    );
    loadBadge(uniqueId);
    return result.toString();
  }

  private void loadBadge(String elementId) {
    Scheduler
      .get()
      .scheduleDeferred(
        new Command() {
          @Override
          public void execute() {
            Element el = Document.get().getElementById(elementId);
            if (el != null) {
              String id = el.getAttribute(DATA_USER_GROUP_ID);
              boolean isIndividual = Boolean.valueOf(
                el.getAttribute(DATA_IS_INDIVIDUAL)
              );
              if (isIndividual) {
                UserBadge userBadge = Portal.getInjector().getUserBadgeWidget();
                userBadge.addStyleNames("ignore-click-events");
                userProfileAsyncHandler.getUserProfile(
                  id,
                  new AsyncCallback<UserProfile>() {
                    @Override
                    public void onSuccess(UserProfile profile) {
                      Div div = new Div();
                      div.setMarginTop(3);
                      div.addStyleName("flexcontainer-row");
                      userBadge.addStyleNames(
                        "flexcontainer-column flexcontainer-justify-center"
                      );
                      div.add(userBadge.asWidget());

                      Div extraInfoDiv = new Div();
                      extraInfoDiv.add(
                        new Span(getExtraUserInformation(profile))
                      );
                      extraInfoDiv.setMarginTop(1);
                      extraInfoDiv.addStyleName(
                        "flexcontainer-column flexcontainer-column-fill-width overflowHidden blackText-imp"
                      );
                      div.add(extraInfoDiv);
                      el.appendChild(div.getElement());
                      userBadge.configure(profile);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                      el.appendChild(userBadge.asWidget().getElement());
                      userBadge.configure(id);
                    }
                  }
                );
              } else {
                TeamBadge teamBadge = Portal.getInjector().getTeamBadgeWidget();
                teamBadge.configure(id);
                teamBadge.addStyleName("ignore-click-events");
                el.appendChild(teamBadge.asWidget().getElement());
              }
            }
          }
        }
      );
  }

  private String getExtraUserInformation(UserProfile profile) {
    StringBuilder sb = new StringBuilder();
    if (profile.getFirstName() != null) {
      sb.append(profile.getFirstName().trim());
      sb.append(" ");
    }
    if (profile.getLastName() != null) {
      sb.append(profile.getLastName().trim());
      sb.append(" ");
    }

    if (
      profile.getCompany() != null && profile.getCompany().trim().length() > 0
    ) {
      sb.append("(");
      sb.append(profile.getCompany().trim());
      sb.append(")");
    }

    return sb.toString();
  }

  @Override
  public String getReplacementString() {
    // Example output:
    // Pac Man | 114085
    StringBuilder sb = new StringBuilder();
    if (!header.getIsIndividual()) sb.append("(Team) ");

    String firstName = header.getFirstName();
    String lastName = header.getLastName();
    String username = header.getUserName();
    sb.append(DisplayUtils.getDisplayName(firstName, lastName, username));
    sb.append("  |  " + header.getOwnerId());
    return sb.toString();
  }

  public String getId() {
    return header.getOwnerId();
  }

  public String isIndividual() {
    return header.getIsIndividual().toString();
  }

  public String getName() {
    return header.getUserName();
  }

  public UserGroupHeader getHeader() {
    return header;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }
}
