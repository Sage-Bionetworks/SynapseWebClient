package org.sagebionetworks.web.client;


import static org.sagebionetworks.web.client.ClientProperties.DEFAULT_PLACE_TOKEN;
import static org.sagebionetworks.web.client.ClientProperties.GB;
import static org.sagebionetworks.web.client.ClientProperties.IMAGE_CONTENT_TYPES_SET;
import static org.sagebionetworks.web.client.ClientProperties.KB;
import static org.sagebionetworks.web.client.ClientProperties.MB;
import static org.sagebionetworks.web.client.ClientProperties.STYLE_DISPLAY_INLINE;
import static org.sagebionetworks.web.client.ClientProperties.TABLE_CONTENT_TYPES_SET;
import static org.sagebionetworks.web.client.ClientProperties.TB;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;
import org.sagebionetworks.gwt.client.schema.adapter.DateUtils;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.schema.FORMAT;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.FitImage;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.WidgetSelectionState;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class DisplayUtils {
	private static DateTimeFormat prettyFormat = null; 
	private static Logger displayUtilsLogger = Logger.getLogger(DisplayUtils.class.getName());
	public static PublicPrincipalIds publicPrincipalIds = null;
	public static enum MessagePopup {  
        INFO,
        WARNING,
        QUESTION
	}
	
	public static NotifySettings getDefaultSettings() {
		NotifySettings notifySettings = NotifySettings.newSettings();
		notifySettings.setTemplate("<div data-notify=\"container\" class=\"col-xs-11 alert alert-{0}\" role=\"alert\">\n" + 
				"  <button type=\"button\" aria-hidden=\"true\" class=\"close\" data-notify=\"dismiss\">x</button>\n" + 
				"  <span data-notify=\"icon\"></span>\n" + 
				"  <strong><span data-notify=\"title\">{1}</span></strong>\n" + 
				"  <span data-notify=\"message\">{2}</span>\n" + 
				"  <a href=\"{3}\" target=\"{4}\" data-notify=\"url\"></a>\n" + 
				"</div>");
		
		return notifySettings;
	}
	/**
	 * Returns a properly aligned icon from an ImageResource
	 * @param icon
	 * @return
	 */
	public static String getIconHtml(ImageResource icon) {
		if(icon == null) return null;		
		return "<span class=\"iconSpan\">" + AbstractImagePrototype.create(icon).getHTML() + "</span>";
	}
	public static String getFriendlySize(double size, boolean abbreviatedUnits) {
		NumberFormat df = NumberFormat.getDecimalFormat();
		if(size >= TB) {
            return df.format(size/TB) + (abbreviatedUnits?" TB":" Terabytes");
        }
		if(size >= GB) {
            return df.format(size/GB) + (abbreviatedUnits?" GB":" Gigabytes");
        }
		if(size >= MB) {
            return df.format(size/MB) + (abbreviatedUnits?" MB":" Megabytes");
        }
		if(size >= KB) {
            return df.format(size/KB) + (abbreviatedUnits?" KB":" Kilobytes");
        }
        return df.format(size) + " bytes";
    }
	
	public static String getFileNameFromExternalUrl(String path){
		//grab the text between the last '/' and following '?'
		String fileName = "";
		if (path != null) {
			int lastSlash = path.lastIndexOf("/");
			if (lastSlash > -1) {
				int firstQuestionMark = path.indexOf("?", lastSlash);
				if (firstQuestionMark > -1) {
					fileName = path.substring(lastSlash+1, firstQuestionMark);
				} else {
					fileName = path.substring(lastSlash+1);
				}
			}
		}
		return fileName;
	}
	
	/**
	 * Handles the exception. Returns true if the user has been alerted to the exception already
	 * @param ex
	 * @param placeChanger
	 * @return true if the user has been prompted
	 */
	public static boolean handleServiceException(Throwable ex, GlobalApplicationState globalApplicationState, boolean isLoggedIn, ShowsErrors view) {
		//send exception to the javascript console
		if (displayUtilsLogger != null && ex != null)
			displayUtilsLogger.log(Level.SEVERE, ex.getMessage());
		if(ex instanceof ReadOnlyModeException || ex instanceof SynapseDownException) {
			globalApplicationState.getPlaceChanger().goTo(new Down(DEFAULT_PLACE_TOKEN));
			return true;
		} else if(ex instanceof UnauthorizedException) {
			// send user to login page						
			showInfo(DisplayConstants.SESSION_TIMEOUT, DisplayConstants.SESSION_HAS_TIMED_OUT);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGOUT_TOKEN));
			return true;
		} else if(ex instanceof ForbiddenException) {			
			if(!isLoggedIn) {				
				view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
				globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			} else {
				view.showErrorMessage(DisplayConstants.ERROR_FAILURE_PRIVLEDGES + " " + ex.getMessage());
			}
			return true;
		} else if(ex instanceof BadRequestException) {
			//show error (not to file a jira though)
			view.showErrorMessage(ex.getMessage());
			return true;
		} else if(ex instanceof NotFoundException) {
			view.showErrorMessage(DisplayConstants.ERROR_NOT_FOUND);
			globalApplicationState.getPlaceChanger().goTo(new Home(DEFAULT_PLACE_TOKEN));
			return true;
		} else if (ex instanceof UnknownErrorException) {
			//An unknown error occurred. 
			//Exception handling on the backend now throws the reason into the exception message.  Easy!
			showErrorMessage(ex, globalApplicationState.getJiraURLHelper(), isLoggedIn, ex.getMessage());
			return true;
		}
		
		// For other exceptions, allow the consumer to send a good message to the user
		return false;
	}
	
	public static boolean checkForRepoDown(Throwable caught, PlaceChanger placeChanger, SynapseView view) {
		if(caught instanceof ReadOnlyModeException) {
			view.showErrorMessage(DisplayConstants.SYNAPSE_IN_READ_ONLY_MODE);
			return true;
		} else if(caught instanceof SynapseDownException) {
			placeChanger.goTo(new Down(DEFAULT_PLACE_TOKEN));
			return true;
		}
		return false;
	}
		
	/**
	 * Returns a panel used to show a component is loading in the view
	 * @param sageImageBundle
	 * @return
	 */
	public static Div getLoadingWidget(SageImageBundle sageImageBundle) {
		Div cp = new Div();
		cp.add(new HTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.loading31()))));		
		return cp;
	}
	
	public static String getLoadingHtml(SageImageBundle sageImageBundle) {
		return getLoadingHtml(sageImageBundle, DisplayConstants.LOADING);
	}

	public static String getLoadingHtml(SageImageBundle sageImageBundle, String message) {
		return DisplayUtils.getIconHtml(sageImageBundle.loading16()) + "&nbsp;" + message + "...";
	}


	/**
	 * Shows an info message to the user in the "Global Alert area".
	 * @param title
	 * @param message
	 */
	public static void showInfo(String title, String message) {
		NotifySettings settings = getDefaultSettings();
		settings.setType(NotifyType.INFO);
		notify(title, message, settings);
	}
	
	public static void notify(String title, String message, NotifySettings settings) {
		try{
			Notify.notify(title, message, settings);
		} catch(Throwable t) {
			SynapseJSNIUtilsImpl._consoleError(getStackTrace(t));
		}
	}
	
	/**
	 * Shows an warning message to the user in the "Global Alert area".
	 * @param title
	 * @param message
	 */
	public static void showError(String title, String message, Integer timeout) {
		NotifySettings settings = getDefaultSettings();
		settings.setType(NotifyType.DANGER);
		settings.setAllowDismiss(false);
		if (timeout != null) {
			settings.setDelay(timeout);	
		}
		notify(title, message, settings);
	}
	
	public static void showErrorMessage(String message) {
		showPopup("", message, MessagePopup.WARNING, null, null);
	}
	
	public static void showErrorMessage(String title, String message) {
		showPopup(title, message, MessagePopup.WARNING, null, null);
	}


	/**
	 * @param t
	 * @param jiraHelper
	 * @param profile
	 * @param friendlyErrorMessage
	 *            (optional)
	 */
	public static void showErrorMessage(final Throwable t,
			final JiraURLHelper jiraHelper, boolean isLoggedIn,
			String friendlyErrorMessage) {
		SynapseJSNIUtilsImpl._consoleError(getStackTrace(t));
		if (!isLoggedIn) {
			showErrorMessage(t.getMessage());
			return;
		}
		final Modal d = new Modal();
		d.addStyleName("padding-5");

		final String errorMessage = friendlyErrorMessage == null ? t.getMessage() : friendlyErrorMessage;
		Icon errorIcon = new Icon(IconType.EXCLAMATION_CIRCLE);
		errorIcon.setSize(org.gwtbootstrap3.client.ui.constants.IconSize.TIMES3);
		errorIcon.setPull(Pull.LEFT);
		errorIcon.addStyleName("margin-right-10");
		HTML errorContent = new HTML(errorMessage);
		ModalBody dialogContent = new ModalBody();
		dialogContent.addStyleName("margin-10");
		dialogContent.add(errorIcon);
		dialogContent.add(errorContent);

		// create text area for steps
		FlowPanel formGroup = new FlowPanel();
		formGroup.addStyleName("form-group margin-top-30");
		formGroup.add(new HTML("<label>Describe the problem (optional)</label>"));
		final TextArea textArea = new TextArea();
		textArea.addStyleName("form-control");
		textArea.getElement().setAttribute("placeholder","Steps to reproduce the error");
		textArea.getElement().setAttribute("rows", "4");
		formGroup.add(textArea);
		dialogContent.add(formGroup);
		d.add(dialogContent);
		d.setSize(ModalSize.LARGE);
		d.setTitle("Synapse Error");
		Button sendBugButton = new Button(DisplayConstants.SEND_BUG_REPORT, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				jiraHelper.createIssueOnBackend(textArea.getValue(), t,
						errorMessage, new AsyncCallback<String>() {
							@Override
							public void onSuccess(String result) {
								d.hide();
								showInfo("Report sent", "Thank you!");
							}

							@Override
							public void onFailure(Throwable caught) {
								// failure to create issue!
								DisplayUtils.showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY+"\n" 
								+ caught.getMessage() +"\n\n"
								+ textArea.getValue());
							}
						});
			}
		});
		sendBugButton.setType(org.gwtbootstrap3.client.ui.constants.ButtonType.PRIMARY);
		Button doNotSendBugButton = new Button(DisplayConstants.DO_NOT_SEND_BUG_REPORT, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				d.hide();
			}
		});
		ModalFooter footer = new ModalFooter();
		footer.add(doNotSendBugButton);
		footer.add(sendBugButton);
		d.add(footer);;
		d.show();
	}
	
	public static void showInfoDialog(
			String title, 
			String message,
			Callback okCallback
			) {
		showPopup(title, message, MessagePopup.INFO, okCallback, null);
	}
	
	public static void showConfirmDialog(
			String title, 
			String message,
			Callback yesCallback,
			Callback noCallback
			) {
		showPopup(title, message, MessagePopup.QUESTION, yesCallback, noCallback);
	}
	
	public static void showConfirmDialog(
			String title, 
			String message,
			Callback yesCallback
			) {
		showConfirmDialog(title, message, yesCallback, new Callback() {
			@Override
			public void invoke() {
				//do nothing when No is clicked
			}
		});
	}
	
	public static void showPopup(String title, String message,
			DisplayUtils.MessagePopup iconStyle,
			final Callback primaryButtonCallback,
			final Callback secondaryButtonCallback) {
		
		SafeHtml popupHtml = getPopupSafeHtml(title, message, iconStyle);
		boolean isSecondaryButton = secondaryButtonCallback != null;
		
		if (isSecondaryButton) {
			Bootbox.confirm(popupHtml.asString(), new ConfirmCallback() {
				@Override
				public void callback(boolean isConfirmed) {
					if (isConfirmed) {
						if (primaryButtonCallback != null)
							primaryButtonCallback.invoke();
					} else {
						if (secondaryButtonCallback != null)
							secondaryButtonCallback.invoke();
					}
				}
			});
		} else {
			Bootbox.alert(popupHtml.asString(), new AlertCallback() {
				@Override
				public void callback() {
					if (primaryButtonCallback != null)
						primaryButtonCallback.invoke();
				}
			});
		}
	}
	
	public static SafeHtml getPopupSafeHtml(String title, String message, DisplayUtils.MessagePopup iconStyle) {
		String iconHtml = "";
		if (MessagePopup.INFO.equals(iconStyle))
			iconHtml = getIcon("glyphicon-info-sign font-size-32 col-xs-1");
		else if (MessagePopup.WARNING.equals(iconStyle))
			iconHtml = getIcon("glyphicon-exclamation-sign font-size-32 col-xs-1");
		else if (MessagePopup.QUESTION.equals(iconStyle))
			iconHtml = getIcon("glyphicon-question-sign font-size-32 col-xs-1");
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		if (DisplayUtils.isDefined(title)) {
			builder.appendHtmlConstant("<h5>");
			builder.appendEscaped(title);
			builder.appendHtmlConstant("</h5>");
		}
		builder.appendHtmlConstant("<div class=\"row\">");
		if (iconHtml.length() > 0)
			builder.appendHtmlConstant(iconHtml);
		String messageWidth = DisplayUtils.isDefined(iconHtml) ? "col-xs-11" : "col-xs-12";
		builder.appendHtmlConstant("<div class=\""+messageWidth+"\">");
		builder.appendEscaped(message);
		builder.appendHtmlConstant("</div></div>");
		return builder.toSafeHtml();
	}
	
	public static void scrollToTop(){
		com.google.gwt.user.client.Window.scrollTo(0, 0);
	}
	
	public static String getPrimaryEmail(UserProfile userProfile) {
		List<String> emailAddresses = userProfile.getEmails();
		if (emailAddresses == null || emailAddresses.isEmpty()) throw new IllegalStateException("UserProfile email list is empty");
		return emailAddresses.get(0);
	}
	
	public static String getDisplayName(UserProfile profile) {
		return getDisplayName(profile.getFirstName(), profile.getLastName(), profile.getUserName());
	}
	
	public static String getDisplayName(UserGroupHeader header) {
		return DisplayUtils.getDisplayName(header.getFirstName(), header.getLastName(), header.getUserName());
	}
	
	public static String getDisplayName(String firstName, String lastName, String userName) {
		StringBuilder sb = new StringBuilder();
		boolean hasDisplayName = false;
		if (firstName != null && firstName.length() > 0) {
			sb.append(firstName.trim());
			hasDisplayName = true;
		}
		if (lastName != null && lastName.length() > 0) {
			sb.append(" ");
			sb.append(lastName.trim());
			hasDisplayName = true;
		}
		
		sb.append(getUserName(userName, hasDisplayName));
		
		return sb.toString();
	}
	
	public static boolean isTemporaryUsername(String username){
		if(username == null) throw new IllegalArgumentException("UserName cannot be null");
		return username.startsWith(WebConstants.TEMPORARY_USERNAME_PREFIX);
	}

	public static String getUserName(String userName, boolean inParens) {
		StringBuilder sb = new StringBuilder();
		
		if (userName != null && !isTemporaryUsername(userName)) {
			//if the name is filled in, then put the username in parens
			if (inParens)
				sb.append(" (");
			sb.append(userName);
			if (inParens)
				sb.append(")");
		}
		
		return sb.toString();
	}
	
	public static String getMarkdownWidgetWarningHtml(String warningText) {
		return getWarningHtml(DisplayConstants.MARKDOWN_WIDGET_WARNING, warningText);
	}
	
	public static String getMarkdownAPITableWarningHtml(String warningText) {
		return getWarningHtml(DisplayConstants.MARKDOWN_API_TABLE_WARNING, warningText);
	}
	
	public static String getWarningHtml(String title, String warningText) {
		return getAlertHtml(title, warningText, BootstrapAlertType.WARNING);
	}
	
	public static String getAlertHtml(String title, String text, BootstrapAlertType type) {
		return "<div class=\"alert alert-"+type.toString().toLowerCase()+"\"><span class=\"boldText\">"+ title + "</span> " + SafeHtmlUtils.htmlEscape(text) + "</div>";
	}
	
	public static String getBadgeHtml(String i) {
		return "<span class=\"badge moveup-4\">"+i+"</span>";
	}

	
	public static String uppercaseFirstLetter(String display) {
		return display.substring(0, 1).toUpperCase() + display.substring(1);		
	}
		
	/**
	 * YYYY-MM-DD HH:mm:ss
	 * @param toFormat
	 * @return
	 */
	public static String convertDataToPrettyString(Date toFormat) {
		if(toFormat == null) throw new IllegalArgumentException("Date cannot be null");
		if (prettyFormat == null) {
			prettyFormat = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
		}
		return prettyFormat.format(toFormat);
	}
	
	
	/**
	 * Converts a date to just a date.
     * @return  yyyy-MM-dd
	 * @return
	 */
	public static String converDateaToSimpleString(Date toFormat) {
		if(toFormat == null) throw new IllegalArgumentException("Date cannot be null");
		return DateUtils.convertDateToString(FORMAT.DATE, toFormat);
	}
	
	public static String getTeamHistoryToken(String teamId) {
		Team place = new Team(teamId);
		return "#!" + getTeamPlaceString(Team.class) + ":" + place.toToken();
	}
	
	public static String getTeamSearchHistoryToken(String searchTerm, Integer start) {
		TeamSearch place = new TeamSearch(searchTerm, start);
		return "#!" + getTeamSearchPlaceString(TeamSearch.class) + ":" + place.toToken();
	}
	
	public static String getPeopleSearchHistoryToken(String searchTerm, Integer start) {
		PeopleSearch place = new PeopleSearch(searchTerm, start);
		return "#!" + getPeopleSearchPlaceString(PeopleSearch.class) + ":" + place.toToken();
	}
	
	public static String getTrashHistoryToken(String token, Integer start) {
		Trash place = new Trash(token, start);
		return "#!" + getTrashPlaceString(Trash.class) + ":" + place.toToken();
	}

	public static String getSearchHistoryToken(String searchQuery, Long start) {
		Search place = new Search(searchQuery, start);
		return "#!" + getSearchPlaceString(Search.class) + ":" + place.toToken();
	}
	
	public static String getSynapseHistoryToken(String entityId) {
		return "#" + getSynapseHistoryTokenNoHash(entityId, null);
	}
	
	public static String getSynapseHistoryTokenNoHash(String entityId) {
		return getSynapseHistoryTokenNoHash(entityId, null);
	}
	
	public static String getSynapseHistoryToken(String entityId, Long versionNumber) {
		return "#" + getSynapseHistoryTokenNoHash(entityId, versionNumber);
	}
	
	public static String getSynapseHistoryTokenNoHash(String entityId, Long versionNumber) {
		return getSynapseHistoryTokenNoHash(entityId, versionNumber, null);
	}
	
	public static String getSynapseHistoryTokenNoHash(String entityId, Long versionNumber, Synapse.EntityArea area) {
		return getSynapseHistoryTokenNoHash(entityId, versionNumber, area, null);
	}
	public static String getSynapseHistoryTokenNoHash(String entityId, Long versionNumber, Synapse.EntityArea area, String areaToken) {
		Synapse place = new Synapse(entityId, versionNumber, area, areaToken);
		return "!"+ getPlaceString(Synapse.class) + ":" + place.toToken();
	}
	
	/**
	 * Stub the string removing the last partial word
	 * @param str
	 * @param length
	 * @return
	 */
	public static String stubStr(String str, int length) {
		if(str == null) {
			return "";
		}
		if(str.length() > length) {
			String sub = str.substring(0, length);
			str = sub.replaceFirst(" \\w+$", "") + ".."; // clean off partial last word
		} 
		return str; 
	}

	/**
	 * Stub the string with partial word at end left in 
	 * @param contents
	 * @param maxLength
	 * @return
	 */
	public static String stubStrPartialWord(String contents, int maxLength) {
		String stub = contents;
		if(contents != null && contents.length() > maxLength) {
			stub = contents.substring(0, maxLength-3);
			stub += " ..";
		}
		return stub; 
	}
	
	
	/*
	 * Private methods
	 */
	private static String getPlaceString(Class<Synapse> place) {
		return getPlaceString(place.getName());		
	}
	
	private static String getTeamPlaceString(Class<Team> place) {
		return getPlaceString(place.getName());		
	}

	private static String getTeamSearchPlaceString(Class<TeamSearch> place) {
		return getPlaceString(place.getName());		
	}
	
	private static String getPeopleSearchPlaceString(Class<PeopleSearch> place) {
		return getPlaceString(place.getName());		
	}
	
	private static String getTrashPlaceString(Class<Trash> place) {
		return getPlaceString(place.getName());		
	}

	private static String getSearchPlaceString(Class<Search> place) {
		return getPlaceString(place.getName());		
	}
	
	private static String getPlaceString(String fullPlaceName) {
		fullPlaceName = fullPlaceName.replaceAll(".+\\.", "");
		return fullPlaceName;
	}
	
	/**
	 * Create the url to a profile attachment image.
	 * @param baseURl
	 * @param userId
	 * @param tokenId
	 * @param fileName
	 * @return
	 */
	public static String createUserProfileAttachmentUrl(String baseURl, String userId, String fileHandleId, boolean preview){
		StringBuilder builder = new StringBuilder();
		builder.append(baseURl);
		builder.append("?"+WebConstants.USER_PROFILE_USER_ID+"=");
		builder.append(userId);
		builder.append("&"+WebConstants.USER_PROFILE_IMAGE_ID+"=");
		builder.append(fileHandleId);
		builder.append("&"+WebConstants.USER_PROFILE_PREVIEW+"=");
		builder.append(preview);
		return builder.toString();
	}
	
	public static Popover addPopover(Widget widget, String message) {
		Popover popover = new Popover(widget);
		popover.setPlacement(Placement.AUTO);
		popover.setIsHtml(true);
		popover.setContent(message);
		return popover;
	}
	
	public static Tooltip addTooltip(Widget widget, String tooltipText){
		return addTooltip(widget, tooltipText, Placement.AUTO);
	}
	
	/**
	 * Adds a twitter bootstrap tooltip to the given widget using the standard Synapse configuration.
	 * NOTE: Add the widget to the parent container only after adding the tooltip (the Tooltip is reconfigured on attach event).
	 *
	 * CAUTION - If not used with a non-block level element like
	 * an anchor, img, or span the results will probably not be
	 * quite what you want.  Read the twitter bootstrap documentation
	 * for the options that you can specify in optionsMap
	 *
	 * @param util the JSNIUtils class (or mock)
	 * @param widget the widget to attach the tooltip to
	 * @param tooltipText text to display
	 * @param pos where to position the tooltip relative to the widget
	 */
	public static Tooltip addTooltip(Widget widget, String tooltipText, Placement pos){
		Tooltip t = new Tooltip();
		t.setPlacement(pos);
		t.setTitle(tooltipText);
		t.setIsHtml(true);
		t.setIsAnimated(false);
		t.setTrigger(Trigger.HOVER);
		t.setWidget(widget);
		return t;
	}
	
	public static String getVersionDisplay(Versionable versionable) {		
		String version = "";
		if(versionable == null || versionable.getVersionNumber() == null) return version;

		if(versionable.getVersionLabel() != null && !versionable.getVersionNumber().toString().equals(versionable.getVersionLabel())) {
			version = versionable.getVersionLabel() + " (" + versionable.getVersionNumber() + ")";
		} else {
			version = versionable.getVersionNumber().toString(); 			
		}
		return version;
	}

	// from http://stackoverflow.com/questions/3907531/gwt-open-page-in-a-new-tab
	public static native JavaScriptObject newWindow(String url, String name, String features)/*-{
    	try {
	    	var window = $wnd.open(url, name, features);
	    	return window;
		}catch(err) {
			return null;
		}
	}-*/;

	public static Anchor createIconLink(AbstractImagePrototype icon, ClickHandler clickHandler) {
		Anchor anchor = new Anchor();
		anchor.setHTML(icon.getHTML());
		anchor.addClickHandler(clickHandler);
		return anchor;
	}
	
	public static boolean isInTestWebsite(CookieProvider cookies) {
		return isInCookies(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY, cookies);
	}

	public static void setTestWebsite(boolean testWebsite, CookieProvider cookies) {
		setInCookies(testWebsite, DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY, cookies);
	}
	
	public static final String SYNAPSE_TEST_WEBSITE_COOKIE_KEY = "SynapseTestWebsite";
	
	public static boolean isInCookies(String cookieKey, CookieProvider cookies) {
		return cookies.getCookie(cookieKey) != null;
	}

	public static void setInCookies(boolean value, String cookieKey, CookieProvider cookies) {
		if (value && !isInCookies(cookieKey, cookies)) {
			//set the cookie
			cookies.setCookie(cookieKey, "true");
		} else{
			cookies.removeCookie(cookieKey);
		}
	}

		
	
	/**
	 * Create the URL to a version of a wiki's attachments.
	 * @param baseFileHandleUrl
	 * @param wikiKey
	 * @param fileName
	 * @param preview
	 * @param wikiVersion
	 * @return
	 */
	public static String createVersionOfWikiAttachmentUrl(String baseFileHandleUrl, WikiPageKey wikiKey, String fileName, 
			boolean preview, Long wikiVersion, String xsrfToken) {
		String attachmentUrl = createWikiAttachmentUrl(baseFileHandleUrl, wikiKey, fileName, preview, xsrfToken);
		return attachmentUrl + "&" + WebConstants.WIKI_VERSION_PARAM_KEY + "=" + wikiVersion.toString();
	}
	
	/**
		 * Create the url to a wiki filehandle.
		 * @param baseURl
		 * @param id
		 * @param tokenId
		 * @param fileName
		 * @return
		 */
	public static String createWikiAttachmentUrl(String baseFileHandleUrl, WikiPageKey wikiKey, String fileName, boolean preview, String xsrfToken){
		//direct approach not working.  have the filehandleservlet redirect us to the temporary wiki attachment url instead
//		String attachmentPathName = preview ? "attachmentpreview" : "attachment";
//		return repoServicesUrl 
//				+"/" +wikiKey.getOwnerObjectType().toLowerCase() 
//				+"/"+ wikiKey.getOwnerObjectId()
//				+"/wiki/" 
//				+wikiKey.getWikiPageId()
//				+"/"+ attachmentPathName+"?fileName="+URL.encodePathSegment(fileName);
		String wikiIdParam = wikiKey.getWikiPageId() == null ? "" : "&" + WebConstants.WIKI_ID_PARAM_KEY + "=" + wikiKey.getWikiPageId();

		return baseFileHandleUrl + "?" +
				WebConstants.WIKI_OWNER_ID_PARAM_KEY + "=" + wikiKey.getOwnerObjectId() + "&" +
				WebConstants.WIKI_OWNER_TYPE_PARAM_KEY + "=" + wikiKey.getOwnerObjectType() + "&"+
				WebConstants.XSRF_TOKEN_KEY + "=" + xsrfToken + "&" +
				WebConstants.WIKI_FILENAME_PARAM_KEY + "=" + fileName + "&" +
					WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY + "=" + Boolean.toString(preview) +
					wikiIdParam;
	}
		
	public static String createFileEntityUrl(String baseFileHandleUrl, String entityId, Long versionNumber, boolean preview, String xsrfToken){
		return createFileEntityUrl(baseFileHandleUrl, entityId, versionNumber, preview, false, xsrfToken);
	}
	
	public static String getParamForNoCaching() {
		return WebConstants.NOCACHE_PARAM + new Date().getTime();
	}
	
	/**
	 * Create the url to a FileEntity filehandle.
	 * @param baseURl
	 * @param entityid
	 * @return
	 */
	public static String createFileEntityUrl(String baseFileHandleUrl, String entityId, Long versionNumber, boolean preview, boolean proxy, String xsrfToken){
		String versionParam = versionNumber == null ? "" : "&" + WebConstants.ENTITY_VERSION_PARAM_KEY + "=" + versionNumber.toString();
		return baseFileHandleUrl + "?" +
				WebConstants.ENTITY_PARAM_KEY + "=" + entityId + "&" +
				WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY + "=" + Boolean.toString(preview) + "&" +
				WebConstants.PROXY_PARAM_KEY + "=" + Boolean.toString(proxy) + "&" + 
				WebConstants.XSRF_TOKEN_KEY + "=" + xsrfToken +
				versionParam;
	}
	
	/**
	 * Create the url to a Team icon filehandle.
	 * @param baseURl
	 * @param teamId
	 * @return
	 */
	public static String createTeamIconUrl(String baseFileHandleUrl, String teamId, String xsrfToken){
		return baseFileHandleUrl + "?" +
				WebConstants.TEAM_PARAM_KEY + "=" + teamId + "&" +
				WebConstants.XSRF_TOKEN_KEY + "=" + xsrfToken;
	}
	
	/**
	 * Create the url to the raw file handle id (must be the owner to access)
	 * @param baseURl
	 * @param rawFileHandleId
	 * @return
	 */
	public static String createRawFileHandleUrl(String baseFileHandleUrl, String rawFileHandleId, String xsrfToken){
		return baseFileHandleUrl + "?" +
				WebConstants.RAW_FILE_HANDLE_PARAM + "=" + rawFileHandleId + "&" +
				WebConstants.XSRF_TOKEN_KEY + "=" + xsrfToken;
	}

	public static String createEntityVersionString(Reference ref) {
		return createEntityVersionString(ref.getTargetId(), ref.getTargetVersionNumber());
	}
	
	public static String createEntityVersionString(String id, Long version) {
		String idNotNull = id == null ? "" : id;
		if(version != null)
			return idNotNull+WebConstants.ENTITY_VERSION_STRING+version;
		else 
			return idNotNull;		
	}
	public static Reference parseEntityVersionString(String entityVersion) {
		String[] parts = entityVersion.split(WebConstants.ENTITY_VERSION_STRING);
		if (parts.length == 1) {
			// version may be using a dot delimiter:
			parts = entityVersion.split("\\.");
		}
		Reference ref = null;
		if(parts.length > 0) {
			ref = new Reference();
			ref.setTargetId(parts[0]);
			if(parts.length > 1) {
				try {
					ref.setTargetVersionNumber(Long.parseLong(parts[1]));
				} catch(NumberFormatException e) {}
			}
		}		
		return ref;		
	}
		
	public static boolean isRecognizedImageContentType(String contentType) {
		String lowerContentType = contentType.toLowerCase();
		return IMAGE_CONTENT_TYPES_SET.contains(lowerContentType);
	}
	
	public static boolean isRecognizedTableContentType(String contentType) {
		String lowerContentType = contentType.toLowerCase();
		return TABLE_CONTENT_TYPES_SET.contains(lowerContentType);
	}
	
	public static boolean isTextType(String contentType) {
		return contentType.toLowerCase().startsWith("text/");
	}
	
	public static boolean isCSV(String contentType) {
		return contentType != null && contentType.toLowerCase().startsWith("text/csv");
	}
	
	public static boolean isTAB(String contentType) {
		return contentType != null && contentType.toLowerCase().startsWith(WebConstants.TEXT_TAB_SEPARATED_VALUES);
	}
	

	/**
	 * Return a preview filehandle associated with this bundle (or null if unavailable)
	 * @param bundle
	 * @return
	 */
	public static PreviewFileHandle getPreviewFileHandle(EntityBundle bundle) {
		PreviewFileHandle fileHandle = null;
		if (bundle.getFileHandles() != null) {
			for (FileHandle fh : bundle.getFileHandles()) {
				if (fh instanceof PreviewFileHandle) {
					fileHandle = (PreviewFileHandle) fh;
					break;
				}
			}
		}
		return fileHandle;
	}

	/**
	 * Return the filehandle associated with this bundle (or null if unavailable)
	 * @param bundle
	 * @return
	 */
	public static FileHandle getFileHandle(EntityBundle bundle) {
		FileHandle fileHandle = null;
		if (bundle.getFileHandles() != null) {
			FileEntity entity = (FileEntity)bundle.getEntity();
			String targetId = entity.getDataFileHandleId();
			for (FileHandle fh : bundle.getFileHandles()) {
				if (fh.getId().equals(targetId)) {
					fileHandle = fh;
					break;
				}
			}
		}
		return fileHandle;
	}

	public interface SelectedHandler<T> {
		public void onSelected(T selected);		
	}
	
	public static Widget getShareSettingsDisplay(boolean isPublic, SynapseJSNIUtils synapseJSNIUtils) {
		final SimplePanel lc = new SimplePanel();
		lc.addStyleName(STYLE_DISPLAY_INLINE);
		String styleName = isPublic ? "public-acl-image" : "private-acl-image";
		String description = isPublic ? DisplayConstants.PUBLIC_ACL_ENTITY_PAGE : DisplayConstants.PRIVATE_ACL_ENTITY_PAGE;
		String tooltip = isPublic ? DisplayConstants.PUBLIC_ACL_DESCRIPTION : DisplayConstants.PRIVATE_ACL_DESCRIPTION;

		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<div class=\"" + styleName+ "\" style=\"display:inline; position:absolute\"></div>");
		shb.appendHtmlConstant("<span style=\"margin-left: 20px;\">"+description+"</span>");

		//form the html
		HTMLPanel htmlPanel = new HTMLPanel(shb.toSafeHtml());
		htmlPanel.addStyleName("inline-block");
		lc.add(DisplayUtils.addTooltip(htmlPanel, tooltip, Placement.BOTTOM));

		return lc;
	}
	
	public static void updateWidgetSelectionState(WidgetSelectionState state, String text, int cursorPos) {
		state.setWidgetSelected(false);
		state.setWidgetStartIndex(-1);
		state.setWidgetEndIndex(-1);
		state.setInnerWidgetText(null);
		
		if (cursorPos > -1) {
			//move back until I find a whitespace or the beginning
			int startWord = cursorPos-1;
			while(startWord > -1 && !Character.isSpace(text.charAt(startWord))) {
				startWord--;
			}
			startWord++;
			String possibleWidget = text.substring(startWord);
			if (possibleWidget.startsWith(WidgetConstants.WIDGET_START_MARKDOWN)) {
				//find the end
				int endWord = cursorPos;
				while(endWord < text.length() && !WidgetConstants.WIDGET_END_MARKDOWN.equals(String.valueOf(text.charAt(endWord)))) {
					endWord++;
				}
				//invalid widget specification if we went all the way to the end of the markdown
				if (endWord < text.length()) {
					//it's a widget
					//parse the type and descriptor
					endWord++;
					possibleWidget = text.substring(startWord, endWord);
					//set editable
					state.setWidgetSelected(true);
					state.setInnerWidgetText(possibleWidget.substring(WidgetConstants.WIDGET_START_MARKDOWN.length(), possibleWidget.length() - WidgetConstants.WIDGET_END_MARKDOWN.length()));
					state.setWidgetStartIndex(startWord);
					state.setWidgetEndIndex(endWord);
				}
			}
		}
	}
	
	/**
	 * Surround the selectedText with the given markdown.  Or, if the selected text is already surrounded by the markdown, then remove it.
	 * @param text
	 * @param markdown
	 * @param startPos
	 * @param selectionLength
	 * @return
	 */
	public static String surroundText(String text, String startTag, String endTag, boolean isMultiline, int startPos, int selectionLength) throws IllegalArgumentException {
		if (text != null && selectionLength > -1 && startPos >= 0 && startPos <= text.length() && isDefined(startTag)) {
			if (endTag == null) 
				endTag = "";
			int startTagLength = startTag.length();
			int endTagLength = endTag.length();

			int eolPos = text.indexOf('\n', startPos);
			if (eolPos < 0)
				eolPos = text.length();
			int endPos = startPos + selectionLength;
			
			if (eolPos < endPos && !isMultiline)
				throw new IllegalArgumentException(DisplayConstants.SINGLE_LINE_COMMAND_MESSAGE);
			
			String selectedText = text.substring(startPos, endPos);
			//check to see if this text is already surrounded by the markdown.
			int beforeSelectedTextPos = startPos - startTagLength;
			int afterSelectedTextPos = endPos + endTagLength;
			if (beforeSelectedTextPos > -1 && afterSelectedTextPos <= text.length()) {
					if (startTag.equals(text.substring(beforeSelectedTextPos, startPos)) && endTag.equals(text.substring(endPos, afterSelectedTextPos))) {
					//strip off markdown instead
					return text.substring(0, beforeSelectedTextPos) + selectedText + text.substring(afterSelectedTextPos);
				}
			}
			return text.substring(0, startPos) + startTag + selectedText + endTag + text.substring(endPos);
		}
		throw new IllegalArgumentException(DisplayConstants.INVALID_SELECTION);
	}
	
	public static boolean isDefined(String testString) {
		return testString != null && testString.trim().length() > 0;
	}
	
	public static FlowPanel createRowContainerFlowPanel() {
		FlowPanel row = new FlowPanel();
		row.setStyleName("row");
		return row;
	}


	public static enum ButtonType { DEFAULT, PRIMARY, SUCCESS, INFO, WARNING, DANGER, LINK }
	public static enum BootstrapAlertType { SUCCESS, INFO, WARNING, DANGER }
	
	public static com.google.gwt.user.client.ui.Button createButton(String title) {
		return createIconButton(title, ButtonType.DEFAULT, null);
	}
	
	public static com.google.gwt.user.client.ui.Button createButton(String title, ButtonType type) {
		return createIconButton(title, type, null);
	}
		
	public static com.google.gwt.user.client.ui.Button createIconButton(String title, ButtonType type, String iconClass) {		
		com.google.gwt.user.client.ui.Button btn = new com.google.gwt.user.client.ui.Button();
		relabelIconButton(btn, title, iconClass);
		btn.removeStyleName("gwt-Button");
		btn.addStyleName("btn btn-" + type.toString().toLowerCase());
		return btn;
	}
	
	public static void relabelIconButton(com.google.gwt.user.client.ui.Button btn, String title, String iconClass) {
		String style = iconClass == null ? "" : " class=\"glyphicon " + iconClass+ "\"" ;
		btn.setHTML(SafeHtmlUtils.fromSafeConstant("<span" + style +"></span> " + title));
	}
	
	public static String getIcon(String iconClass) {
		return "<span class=\"glyphicon " + iconClass + "\"></span>";
	}

	public static String getFontAwesomeIcon(String iconClass) {
		return "<span class=\"fa fa-" + iconClass + "\"></span>";
	}

	public static EntityHeader getProjectHeader(EntityPath entityPath) {
		if(entityPath == null) return null;
		for(EntityHeader eh : entityPath.getPath()) {
			if(Project.class.getName().equals(eh.getType())) {
				return eh;
			}
		}
		return null;
	}
	
	public static FlowPanel getMediaObject(String heading, String description, ClickHandler clickHandler, String pictureUri, boolean defaultPictureSinglePerson, int headingLevel) {
		FlowPanel panel = new FlowPanel();
		panel.addStyleName("media");
 		String linkStyle = "";
 		if (clickHandler != null)
 			linkStyle = "link";
 		HTML headingHtml = new HTML("<h"+headingLevel+" class=\"media-heading "+linkStyle+"\">" + SafeHtmlUtils.htmlEscape(heading) + "</h"+headingLevel+">");
 		if (clickHandler != null)
 			headingHtml.addClickHandler(clickHandler);
 
 		if (pictureUri != null) {
 			FitImage profilePicture = new FitImage(pictureUri, 64, 64);
 			profilePicture.addStyleName("pull-left media-object imageButton");
 			if (clickHandler != null)
 				profilePicture.addClickHandler(clickHandler);
 			panel.add(profilePicture);
 		} else {
 			//display default picture
 			String iconClass = defaultPictureSinglePerson ? "user" : "users";
 			String clickableButtonCssClass = clickHandler != null ? "imageButton" : "";
 			HTML profilePicture = new HTML(DisplayUtils.getFontAwesomeIcon(iconClass + " font-size-58 padding-2 " + clickableButtonCssClass + " userProfileImage lightGreyText margin-0-imp-before"));
 			profilePicture.addStyleName("pull-left media-object displayInline ");
 			if (clickHandler != null)
 				profilePicture.addClickHandler(clickHandler);
 			panel.add(profilePicture);
 		}
 		FlowPanel mediaBodyPanel = new FlowPanel();
 		mediaBodyPanel.addStyleName("media-body");
 		mediaBodyPanel.add(headingHtml);
 		if (description != null)
 			mediaBodyPanel.add(new HTML(SafeHtmlUtils.htmlEscape(description)));
 		panel.add(mediaBodyPanel);
 		return panel;
	}
	
	public static String getShareMessage(String displayName, String entityId, String hostUrl) {
		return displayName + DisplayConstants.SHARED_ON_SYNAPSE + ":\n"+hostUrl+"#!Synapse:"+entityId+"\n";
	}
	
	public static void getPublicPrincipalIds(UserAccountServiceAsync userAccountService, final AsyncCallback<PublicPrincipalIds> callback){
		if (publicPrincipalIds == null) {
			userAccountService.getPublicAndAuthenticatedGroupPrincipalIds(new AsyncCallback<PublicPrincipalIds>() {
				@Override
				public void onSuccess(PublicPrincipalIds result) {
					publicPrincipalIds = result;
					callback.onSuccess(result);
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		} else
			callback.onSuccess(publicPrincipalIds);
	}
	
	public static void hide(UIObject uiObject) {
		uiObject.setVisible(false);
	}
	
	public static void show(UIObject uiObject) {
		uiObject.setVisible(true);
	}
	
	public static void hide(com.google.gwt.dom.client.Element elem) {
		UIObject.setVisible(elem, false);
	}
	
	public static void show(com.google.gwt.dom.client.Element elem) {
		UIObject.setVisible(elem, true);
	}
	
	public static void showFormError(DivElement parentElement, DivElement messageElement) {
		parentElement.addClassName("has-error");
		DisplayUtils.show(messageElement);
	}
	 
	public static void hideFormError(DivElement parentElement, DivElement messageElement) {
		parentElement.removeClassName("has-error");
		DisplayUtils.hide(messageElement);
	}

	 public static String getInfoHtml(String safeHtmlMessage) {
		 return "<div class=\"alert alert-info\">"+safeHtmlMessage+"</div>";
	 }

	public static String getStackTrace(Throwable t) {
		StringBuilder stackTrace = new StringBuilder();
		if (t != null) {
			for (StackTraceElement element : t.getStackTrace()) {
				stackTrace.append(element + "\n");
			}
		}
		return stackTrace.toString();
	}
	
	/**
	 * This is to work around a Chrome rendering bug, where some containers do not properly calculate their relative widths (in the dynamic bootstrap grid layout) when they are initially added.
	 * The most visible of these cases is the Wiki Subpages panel (see SWC-1450). 
	 * @param e
	 */
	public static void clearElementWidth(Element e) {
		if ( e != null) {
			Style style = e.getStyle();
			if (style != null) {
				style.setWidth(1, Unit.PX);
				style.clearWidth();
			}
		}
	}
	
	/**
	  * just return the empty string if input string parameter s is null, otherwise returns s.
	  */
	public static String replaceWithEmptyStringIfNull(String s) {
		if (s == null)
			return "";
		else return s;
	}

	/**
	 * return true if the widget is in the visible part of the page
	 */
	public static boolean isInViewport(Widget widget) {
		return isInViewport(widget, 100);
	}
	
	/**
	 * return true if the widget is in the visible part of the page
	 * paddingBottom is the extra space (in px) to enlarge the viewport (in order to preemptively load the widget before scrolling into view).
	 */
	public static boolean isInViewport(Widget widget, int paddingBottom) {
		int docViewTop = Window.getScrollTop();
		int docViewBottom = docViewTop + Window.getClientHeight() + paddingBottom;
		int elemTop = widget.getAbsoluteTop();
		int elemBottom = elemTop + widget.getOffsetHeight();
		return ((elemBottom <= docViewBottom) && (elemTop >= docViewTop));
	}
}
