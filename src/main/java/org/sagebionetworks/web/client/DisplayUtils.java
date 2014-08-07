package org.sagebionetworks.web.client;


import static org.sagebionetworks.web.client.ClientProperties.ALERT_CONTAINER_ID;
import static org.sagebionetworks.web.client.ClientProperties.DEFAULT_PLACE_TOKEN;
import static org.sagebionetworks.web.client.ClientProperties.ERROR_OBJ_REASON_KEY;
import static org.sagebionetworks.web.client.ClientProperties.ESCAPE_CHARACTERS_SET;
import static org.sagebionetworks.web.client.ClientProperties.FULL_ENTITY_TOP_MARGIN_PX;
import static org.sagebionetworks.web.client.ClientProperties.GB;
import static org.sagebionetworks.web.client.ClientProperties.IMAGE_CONTENT_TYPES_SET;
import static org.sagebionetworks.web.client.ClientProperties.KB;
import static org.sagebionetworks.web.client.ClientProperties.MB;
import static org.sagebionetworks.web.client.ClientProperties.REGEX_CLEAN_ANNOTATION_KEY;
import static org.sagebionetworks.web.client.ClientProperties.REGEX_CLEAN_ENTITY_NAME;
import static org.sagebionetworks.web.client.ClientProperties.STYLE_DISPLAY_INLINE;
import static org.sagebionetworks.web.client.ClientProperties.TB;
import static org.sagebionetworks.web.client.ClientProperties.WHITE_SPACE;
import static org.sagebionetworks.web.client.ClientProperties.WIKI_URL;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.sagebionetworks.gwt.client.schema.adapter.DateUtils;
import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Analysis;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Code;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.ExpressionData;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.GenotypeData;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Page;
import org.sagebionetworks.repo.model.PhenotypeData;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.RObject;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Step;
import org.sagebionetworks.repo.model.Study;
import org.sagebionetworks.repo.model.Summary;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.FORMAT;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.Help;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Alert;
import org.sagebionetworks.web.client.widget.Alert.AlertType;
import org.sagebionetworks.web.client.widget.FitImage;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.WidgetSelectionState;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.table.TableCellFileHandle;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.NodeType;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.place.shared.Place;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class DisplayUtils {
	private static Logger displayUtilsLogger = Logger.getLogger(DisplayUtils.class.getName());
	public static PublicPrincipalIds publicPrincipalIds = null;
	public static enum MessagePopup {  
        INFO,
        WARNING,
        QUESTION
	}
	public static final String[] ENTITY_TYPE_DISPLAY_ORDER = new String[] {
			Folder.class.getName(), Study.class.getName(), Data.class.getName(),
			Code.class.getName(), Link.class.getName(), 
			Analysis.class.getName(), Step.class.getName(), 
			RObject.class.getName(), PhenotypeData.class.getName(), 
			ExpressionData.class.getName(),	GenotypeData.class.getName() };
	
	/**
	 * Returns a properly aligned icon from an ImageResource
	 * @param icon
	 * @return
	 */
	public static String getIconHtml(ImageResource icon) {
		if(icon == null) return null;		
		return "<span class=\"iconSpan\">" + AbstractImagePrototype.create(icon).getHTML() + "</span>";
	}
	
	/**
	 * Returns a properly aligned icon from an ImageResource
	 * @param icon
	 * @return
	 */
	public static String getIconThumbnailHtml(ImageResource icon) {
		if(icon == null) return null;		
		return "<span class=\"thumbnail-image-container\">" + AbstractImagePrototype.create(icon).getHTML() + "</span>";
	}
	
	/**
	 * Returns a properly aligned name and description for a special user or group
	 * @param name of user or group
	 * @return
	 */
	public static String getUserNameDescriptionHtml(String name, String description) {
		return DisplayUtilsGWT.TEMPLATES.nameAndUsername(name, description).asString();
	}
	
	
	/**
	 * Returns html for a thumbnail image.
	 * 
	 * @param url
	 * @return
	 */
	public static String getThumbnailPicHtml(String url) {
		if(url == null) return null;
		return DisplayUtilsGWT.TEMPLATES.profilePicture(url).asString();
	}

	
	/**
	 * Converts all hrefs to gwt anchors, and handles the anchors by sending them to a new window.
	 * @param panel
	 */
	public static void sendAllLinksToNewWindow(HTMLPanel panel){
		NodeList<com.google.gwt.dom.client.Element> anchors = panel.getElement().getElementsByTagName("a");
		for ( int i = 0 ; i < anchors.getLength() ; i++ ) {
			com.google.gwt.dom.client.Element a = anchors.getItem(i);
		    JSONObject jsonValue = new JSONObject(a);
		    JSONValue hrefJSONValue = jsonValue.get("href");
		    if (hrefJSONValue != null){
		    	final String href = hrefJSONValue.toString().replaceAll("\"", "");
			    String innerText = a.getInnerText();
			    Anchor link = new Anchor();
			    link.setStylePrimaryName("link");
			    link.setText(innerText);
			    
			    link.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						com.google.gwt.user.client.Window.open(href, "_blank", "");
					}
				});
			    panel.addAndReplaceElement(link, a);
		    }
		}
	}
	
	/**
	 * Add a row to the provided FlexTable.
	 * 
	 * @param key
	 * @param value
	 * @param table
	 */
	public static void addRowToTable(int row, String key, String value,
			FlexTable table) {
		addRowToTable(row, key, value, "boldRight", table);
		table.setHTML(row, 1, value);
	}

	public static void addRowToTable(int row, String key, String value,
			String styleName, FlexTable table) {
		table.setHTML(row, 0, key);
		table.getCellFormatter().addStyleName(row, 0, styleName);
		table.setHTML(row, 1, value);
	}
	
	public static void addRowToTable(int row, String label, Anchor key, String value,
			String styleName, FlexTable table) {
		table.setHTML(row, 0, label);
		table.getCellFormatter().addStyleName(row, 0, styleName);
		table.setWidget(row, 1, key);
		table.setHTML(row, 2, value);
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
	
	/**
	 * Use an EntityWrapper instead and check for an exception there
	 * @param obj
	 * @throws RestServiceException
	 */
	@Deprecated
	public static void checkForErrors(JSONObject obj) throws RestServiceException {
		if(obj == null) return;
		if(obj.containsKey("error")) {
			JSONObject errorObj = obj.get("error").isObject();
			if(errorObj.containsKey("statusCode")) {
				JSONNumber codeObj = errorObj.get("statusCode").isNumber();
				if(codeObj != null) {
					int code = ((Double)codeObj.doubleValue()).intValue();
					if(code == 401) { // UNAUTHORIZED
						throw new UnauthorizedException();
					} else if(code == 403) { // FORBIDDEN
						throw new ForbiddenException();
					} else if (code == 404) { // NOT FOUND
						throw new NotFoundException();
					} else if (code == 400) { // Bad Request
						String message = "";
						if(obj.containsKey(ERROR_OBJ_REASON_KEY)) {
							message = obj.get(ERROR_OBJ_REASON_KEY).isString().stringValue();							
						}
						throw new BadRequestException(message);
					} else {
						throw new UnknownErrorException("Unknown Service error. code: " + code);
					}
				}
			}
		}
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
	public static boolean handleServiceException(Throwable ex, GlobalApplicationState globalApplicationState, boolean isLoggedIn, SynapseView view) {
		//send exception to the javascript console
		if (displayUtilsLogger != null && ex != null)
			displayUtilsLogger.log(Level.SEVERE, ex.getMessage());
		if(ex instanceof ReadOnlyModeException) {
			view.showErrorMessage(DisplayConstants.SYNAPSE_IN_READ_ONLY_MODE);
			return true;
		} else if(ex instanceof SynapseDownException) {
			globalApplicationState.getPlaceChanger().goTo(new Down(DEFAULT_PLACE_TOKEN));
			return true;
		} else if(ex instanceof UnauthorizedException) {
			// send user to login page						
			showInfo(DisplayConstants.SESSION_TIMEOUT, DisplayConstants.SESSION_HAS_TIMED_OUT);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			return true;
		} else if(ex instanceof ForbiddenException) {			
			if(!isLoggedIn) {				
				view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
				globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			} else {
				view.showErrorMessage(DisplayConstants.ERROR_FAILURE_PRIVLEDGES);
			}
			return true;
		} else if(ex instanceof BadRequestException) {
			//exception handling on the backend now throws the reason into the exception message.  Easy!
			showErrorMessage(ex, globalApplicationState.getJiraURLHelper(), isLoggedIn, ex.getMessage());
			return true;
		} else if(ex instanceof NotFoundException) {
			view.showErrorMessage(DisplayConstants.ERROR_NOT_FOUND);
			globalApplicationState.getPlaceChanger().goTo(new Home(DEFAULT_PLACE_TOKEN));
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
	 * Handle JSONObjectAdapterException.  This will occur when the client is pointing to an incompatible repo version. 
	 * @param ex
	 * @param placeChanger
	 */
	public static boolean handleJSONAdapterException(JSONObjectAdapterException ex, PlaceChanger placeChanger, UserSessionData currentUser) {
		DisplayUtils.showInfoDialog("Incompatible Client Version", DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION, null);
		placeChanger.goTo(new Home(DEFAULT_PLACE_TOKEN));
		return true;
	}

	
	/*
	 * Button Saving 
	 */
	public static void changeButtonToSaving(Button button, SageImageBundle sageImageBundle) {
		button.setText(DisplayConstants.BUTTON_SAVING);
		button.setIcon(AbstractImagePrototype.create(sageImageBundle.loading16()));
	}
	
	/*
	 * Button Saving 
	 */
	public static void changeButtonToSaving(com.google.gwt.user.client.ui.Button button) {
		button.addStyleName("disabled");
		button.setHTML(SafeHtmlUtils.fromSafeConstant(DisplayConstants.BUTTON_SAVING + "..."));
	}
	
	/**
	 * Check if an Annotation key is valid with the repository service
	 * @param key
	 * @return
	 */
	public static boolean validateAnnotationKey(String key) {
		if(key.matches(REGEX_CLEAN_ANNOTATION_KEY)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if an Entity (Node) name is valid with the repository service
	 * @param key
	 * @return
	 */
	public static boolean validateEntityName(String key) {
		if(key.matches(REGEX_CLEAN_ENTITY_NAME)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Cleans any invalid name characters from a string  
	 * @param str
	 * @return
	 */
	public static String getOffendingCharacterForEntityName(String key) {
		return getOffendingCharacter(key, REGEX_CLEAN_ENTITY_NAME);
	}

	/**
	 * Cleans any invalid name characters from a string  
	 * @param str
	 * @return
	 */
	public static String getOffendingCharacterForAnnotationKey(String key) {
		return getOffendingCharacter(key, REGEX_CLEAN_ANNOTATION_KEY);
	}	
		
	/**
	 * Returns a ContentPanel used to show a component is loading in the view
	 * @param sageImageBundle
	 * @return
	 */
	public static ContentPanel getLoadingWidget(SageImageBundle sageImageBundle) {
		ContentPanel cp = new ContentPanel();
		cp.setHeaderVisible(false);
		cp.setCollapsible(true);
		cp.setLayout(new CenterLayout());								
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
	 * For more precise control over how the message appears,
	 * use the {@link displayGlobalAlert(Alert)} method.
	 * @param title
	 * @param message
	 */
	public static void showInfo(String title, String message) {	
		Alert alert = new Alert(title, message, false);
		alert.setAlertType(AlertType.Info);
		alert.setTimeout(4000);
		displayGlobalAlert(alert);
	}

	/**
	 * Shows an warning message to the user in the "Global Alert area".
	 * For more precise control over how the message appears,
	 * use the {@link displayGlobalAlert(Alert)} method.
	 * @param title
	 * @param message
	 */
	public static void showError(String title, String message, Integer timeout) {
		Alert alert = new Alert(title, message, true);
		alert.setAlertType(AlertType.Error);
		if(timeout != null) {
			alert.setTimeout(timeout);
		}
		displayGlobalAlert(alert);
	}
	
	public static void showErrorMessage(String message) {
		showPopup("", message, MessagePopup.WARNING, null, null);
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
		final Dialog d = new Dialog();
		d.addStyleName("padding-5");

		final String errorMessage = friendlyErrorMessage == null ? t.getMessage() : friendlyErrorMessage;
		HTML errorContent = new HTML(
				getIcon("glyphicon-exclamation-sign margin-right-10 font-size-22 alert-danger")
				+ errorMessage);
		FlowPanel dialogContent = new FlowPanel();
		dialogContent.addStyleName("margin-10");
		dialogContent.add(errorContent);

		// create text area for steps
		FlowPanel formGroup = new FlowPanel();
		formGroup.addStyleName("form-group margin-top-10");
		formGroup.add(new HTML("<label>Describe the problem (optional)</label>"));
		final TextArea textArea = new TextArea();
		textArea.addStyleName("form-control");
		textArea.getElement().setAttribute("placeholder","Steps to reproduce the error");
		textArea.getElement().setAttribute("rows", "4");
		formGroup.add(textArea);
		dialogContent.add(formGroup);

		d.add(dialogContent);

		d.setAutoHeight(true);
		d.setHideOnButtonClick(true);
		d.setWidth(400);
		d.setPlain(true);
		d.setModal(true);
		d.setLayout(new FitLayout());
		d.setButtonAlign(HorizontalAlignment.RIGHT);
		d.setHeading("Synapse Error");
		d.yesText = DisplayConstants.SEND_BUG_REPORT;
		d.noText = DisplayConstants.DO_NOT_SEND_BUG_REPORT;
		d.setButtons(Dialog.YESNO);
		com.extjs.gxt.ui.client.widget.button.Button yesButton = d.getButtonById(Dialog.YES);
		yesButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				jiraHelper.createIssueOnBackend(textArea.getValue(), t,
						errorMessage, new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
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
		boolean isSecondaryButton = secondaryButtonCallback != null;
		
		if (isSecondaryButton) {
			Bootbox.confirm(builder.toSafeHtml().asString(), new ConfirmCallback() {
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
			Bootbox.alert(builder.toSafeHtml().asString(), new AlertCallback() {
				@Override
				public void callback() {
					if (primaryButtonCallback != null)
						primaryButtonCallback.invoke();
				}
			});
		}
	}
	
	public static void center(Window window) {
		int left = (com.google.gwt.user.client.Window.getClientWidth() - window.getOffsetWidth()) / 2;
		window.setPosition(left, 150);
		scrollToTop();
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
	

	
	/**
	 * Returns the NodeType for this entity class. 
	 * TODO : This should be removed when we move to using the Synapse Java Client
	 * @param entity
	 * @return
	 */
	public static NodeType getNodeTypeForEntity(Entity entity) {
		// 	DATASET, LAYER, PROJECT, EULA, AGREEMENT, ENTITY, ANALYSIS, STEP
		if(entity instanceof org.sagebionetworks.repo.model.Study) {
			return NodeType.STUDY;
		} else if(entity instanceof org.sagebionetworks.repo.model.Data) {
			return NodeType.DATA;
		} else if(entity instanceof org.sagebionetworks.repo.model.Project) {
			return NodeType.PROJECT;
		} else if(entity instanceof org.sagebionetworks.repo.model.Analysis) {
			return NodeType.ANALYSIS;
		} else if(entity instanceof org.sagebionetworks.repo.model.Step) {
			return NodeType.STEP;
		} else if(entity instanceof org.sagebionetworks.repo.model.Code) {
			return NodeType.CODE;
		} else if(entity instanceof org.sagebionetworks.repo.model.Link) {
			return NodeType.LINK;
		} 
		return null;	
	}
	
	public static String getEntityTypeDisplay(ObjectSchema schema) {
		String title = schema.getTitle();
		if(title == null){
			title = "<Title missing for Entity: "+schema.getId()+">";
		}
		return title;
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
		return "<div class=\"alert alert-"+type.toString().toLowerCase()+"\"><span class=\"boldText\">"+ title + "</span> " + text + "</div>";
	}
	
	public static String getAlertHtmlSpan(String title, String text, BootstrapAlertType type) {
		return "<span class=\"alert alert-"+type.toString().toLowerCase()+"\"><span class=\"boldText\">"+ title + "</span> " + text + "</span>";
	}
	
	public static String getBadgeHtml(String i) {
		return "<span class=\"badge\">"+i+"</span>";
	}

	
	public static String uppercaseFirstLetter(String display) {
		return display.substring(0, 1).toUpperCase() + display.substring(1);		
	}
		
	/**
	 * YYYY-MM-DD HH:mm:ss
	 * @param toFormat
	 * @return
	 */
	public static String converDataToPrettyString(Date toFormat) {
		if(toFormat == null) throw new IllegalArgumentException("Date cannot be null");
		return DateUtils.convertDateToString(FORMAT.DATE_TIME, toFormat).replaceAll("T", " ");
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
 	
	public static String getSynapseWikiHistoryToken(String ownerId, String objectType, String wikiPageId) {
		Wiki place = new Wiki(ownerId, objectType, wikiPageId);
		return "#!" + getWikiPlaceString(Wiki.class) + ":" + place.toToken();
	}
	
	public static String getTeamHistoryToken(String teamId) {
		Team place = new Team(teamId);
		return "#!" + getTeamPlaceString(Team.class) + ":" + place.toToken();
	}
	
	public static String getTeamSearchHistoryToken(String searchTerm) {
		TeamSearch place = new TeamSearch(searchTerm);
		return "#!" + getTeamSearchPlaceString(TeamSearch.class) + ":" + place.toToken();
	}
	
	public static String getTeamSearchHistoryToken(String searchTerm, Integer start) {
		TeamSearch place = new TeamSearch(searchTerm, start);
		return "#!" + getTeamSearchPlaceString(TeamSearch.class) + ":" + place.toToken();
	}
	
	public static String getTrashHistoryToken(String token, Integer start) {
		Trash place = new Trash(token, start);
		return "#!" + getTrashPlaceString(Trash.class) + ":" + place.toToken();
	}

	public static String getLoginPlaceHistoryToken(String token) {
		LoginPlace place = new LoginPlace(token);
		return "#!" + getLoginPlaceString(LoginPlace.class) + ":" + place.toToken();
	}

	public static String getHelpPlaceHistoryToken(String token) {
		Help place = new Help(token);
		return "#!" + getHelpPlaceString(Help.class) + ":" + place.toToken();
	}

	
	public static String getSearchHistoryToken(String searchQuery) {
		Search place = new Search(searchQuery);
		return "#!" + getSearchPlaceString(Search.class) + ":" + place.toToken();
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
	
	private static String getWikiPlaceString(Class<Wiki> place) {
		return getPlaceString(place.getName());		
	}
	
	public static LayoutContainer wrap(Widget widget) {
		LayoutContainer lc = new LayoutContainer();
		lc.addStyleName("col-md-12");
		lc.add(widget);
		return lc;
	}
	
	public static SimplePanel wrapInDiv(Widget widget) {
		SimplePanel lc = new SimplePanel();
		lc.setWidget(widget);
		return lc;
	}
	private static String getTeamPlaceString(Class<Team> place) {
		return getPlaceString(place.getName());		
	}

	private static String getTeamSearchPlaceString(Class<TeamSearch> place) {
		return getPlaceString(place.getName());		
	}
	
	private static String getTrashPlaceString(Class<Trash> place) {
		return getPlaceString(place.getName());		
	}

	private static String getLoginPlaceString(Class<LoginPlace> place) {
		return getPlaceString(place.getName());		
	}
	private static String getHelpPlaceString(Class<Help> place) {
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
	 * Returns the offending character given a regex string
	 * @param key
	 * @param regex
	 * @return
	 */
	private static String getOffendingCharacter(String key, String regex) {
		String suffix = key.replaceFirst(regex, "");
		if(suffix != null && suffix.length() > 0) {
			return suffix.substring(0,1);
		}
		return null;		
	}

	public static String createEntityLink(String id, String version,
			String display) {
		return "<a href=\"" + DisplayUtils.getSynapseHistoryToken(id) + "\">" + display + "</a>";
	}
	
	public static enum IconSize { PX16, PX24 };
	
	public static ImageResource getSynapseIconForEntityType(EntityType type, IconSize iconSize, IconsImageBundle iconsImageBundle) {
		String className = type == null ? null : type.getClassName();		
		return getSynapseIconForEntityClassName(className, iconSize, iconsImageBundle);
	}

	public static ImageResource getSynapseIconForEntity(Entity entity, IconSize iconSize, IconsImageBundle iconsImageBundle) {
		String className = entity == null ? null : entity.getClass().getName();
		return getSynapseIconForEntityClassName(className, iconSize, iconsImageBundle);
	}

	/**
	 * Create a loading window.
	 * 
	 * @param sageImageBundle
	 * @param message
	 * @return
	 */
	public static Window createLoadingWindow(SageImageBundle sageImageBundle, String message) {
		Window window = new Window();
		window.setModal(true);		
		window.setHeight(114);
		window.setWidth(221);		
		window.setBorders(false);
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant(DisplayUtils.getIconHtml(sageImageBundle.loading31()));
		shb.appendEscaped(message);
		window.add(new Html(shb.toSafeHtml().asString()), new MarginData(20, 0, 0, 45));		
		window.setBodyStyleName("whiteBackground");
		return window;
	}
	
	/**
	 * Create a loading panel with a centered spinner.
	 * 
	 * @param sageImageBundle
	 * @param width
	 * @param height
	 * @return
	 */
	public static Widget createFullWidthLoadingPanel(SageImageBundle sageImageBundle) {
		return createFullWidthLoadingPanel(sageImageBundle, " Loading...");
	}

	/**
	 * Create a loading panel with a centered spinner.
	 * 
	 * @param sageImageBundle
	 * @param width
	 * @param height
	 * @return
	 */
	public static Widget createFullWidthLoadingPanel(SageImageBundle sageImageBundle, String message) {
		Widget w = new HTML(SafeHtmlUtils.fromSafeConstant(
				DisplayUtils.getIconHtml(sageImageBundle.loading31()) +" "+ message));	
		LayoutContainer panel = new LayoutContainer();
		panel.add(w, new MarginData(FULL_ENTITY_TOP_MARGIN_PX, 0, FULL_ENTITY_TOP_MARGIN_PX, 0));
		panel.addStyleName("center");				
		return panel;
	}
	
	public static ImageResource getSynapseIconForEntityClassName(String className, IconSize iconSize, IconsImageBundle iconsImageBundle) {
		ImageResource icon = null;
		if(Link.class.getName().equals(className)) {
			icon = iconsImageBundle.synapseLink16();
		} else if(Analysis.class.getName().equals(className)) {
			// Analysis
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseAnalysis16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseAnalysis24();			
		} else if(Code.class.getName().equals(className)) {
			// Code
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseFile16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseFile24();			
		} else if(Data.class.getName().equals(className) ||
				ExpressionData.class.getName().equals(className) ||
				GenotypeData.class.getName().equals(className) ||
				PhenotypeData.class.getName().equals(className)) {
			// Data
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseFile16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseFile24();			
		} else if(Folder.class.getName().equals(className)) {
			// Folder
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseFolder16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseFolder24();			
		} else if(FileEntity.class.getName().equals(className)) {
			// File
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseFile16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseFile24();			
		} else if(Project.class.getName().equals(className)) {
			// Project
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseProject16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseProject24();			
		} else if(RObject.class.getName().equals(className)) {
			// RObject
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseRObject16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseRObject24();			
		} else if(Summary.class.getName().equals(className)) {
			// Summary
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseSummary16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseSummary24();			
		} else if(Step.class.getName().equals(className)) {
			// Step
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseStep16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseStep24();			
		} else if(Study.class.getName().equals(className)) {
			// Study
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseFolder16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseFolder24();
		} else if(Page.class.getName().equals(className)) {
			// Page
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapsePage16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapsePage24();			
		} else if(TableEntity.class.getName().equals(className)) {
			// TableEntity
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseData16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseData24();			
		} else {
			// default to Model
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseModel16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseModel24();			
		}
		return icon;
	}

	/**
	 * Maps mime types to icons.
	 */
	private static Map<String, String> attachmentMap = new HashMap<String, String>();
	public static String UNKNOWN_ICON 				= "220";
	public static String DEFAULT_PDF_ICON 			= "222";
	public static String DEFAULT_IMAGE_ICON			= "242";
	public static String DEFAULT_TEXT_ICON 			= "224";
	public static String DEFAULT_COMPRESSED_ICON	= "226";
	static{
		attachmentMap.put("pdf", DEFAULT_PDF_ICON);
		attachmentMap.put("txt", DEFAULT_TEXT_ICON);
		attachmentMap.put("doc", DEFAULT_TEXT_ICON);
		attachmentMap.put("doc", DEFAULT_TEXT_ICON);
		attachmentMap.put("docx", DEFAULT_TEXT_ICON);
		attachmentMap.put("docx", DEFAULT_TEXT_ICON);
		attachmentMap.put("zip", DEFAULT_COMPRESSED_ICON);
		attachmentMap.put("tar", DEFAULT_COMPRESSED_ICON);
		attachmentMap.put("gz", DEFAULT_COMPRESSED_ICON);
		attachmentMap.put("rar", DEFAULT_COMPRESSED_ICON);
		attachmentMap.put("png", DEFAULT_IMAGE_ICON);
		attachmentMap.put("gif", DEFAULT_IMAGE_ICON);
		attachmentMap.put("jpg", DEFAULT_IMAGE_ICON);
		attachmentMap.put("jpeg", DEFAULT_IMAGE_ICON);
		attachmentMap.put("bmp", DEFAULT_IMAGE_ICON);
		attachmentMap.put("wbmp", DEFAULT_IMAGE_ICON);
	}
	
	/**
	 * Get the icon to be used with a given file type.
	 */
	public static String getAttachmentIcon(String fileName){
		if(fileName == null) return UNKNOWN_ICON;
		String mimeType = getMimeType(fileName);
		if(mimeType == null) return UNKNOWN_ICON;
		String icon = attachmentMap.get(mimeType.toLowerCase());
		if(icon == null) return UNKNOWN_ICON;
		return icon;
	}
	
	/**
	 * Get the mime type from a file name.
	 * @param fileName
	 * @return
	 */
	public static String getMimeType(String fileName){
		if(fileName == null) return null;
		int index = fileName.lastIndexOf('.');
		if(index < 0) return null;
		if(index+1 >=  fileName.length()) return null;
		return fileName.substring(index+1, fileName.length());
	}
	
	/**
	 * Replace all white space
	 * @param string
	 * @return
	 */
	public static String replaceWhiteSpace(String string){
		if(string == null) return null;
		string = string.replaceAll(" ", WHITE_SPACE);
		return string;
	}
	
	/**
	 * Create the url to an attachment image.
	 * @param baseURl
	 * @param entityId
	 * @param tokenId
	 * @param fileName
	 * @return
	 */
	public static String createAttachmentUrl(String baseURl, String entityId, String tokenId, String fileName){
		return createAttachmentUrl(baseURl, entityId, tokenId, fileName, WebConstants.ENTITY_PARAM_KEY);
	}
	

	/**
	 * Create the url to a profile attachment image.
	 * @param baseURl
	 * @param userId
	 * @param tokenId
	 * @param fileName
	 * @return
	 */
	public static String createUserProfileAttachmentUrl(String baseURl, String userId, String tokenId, String fileName){
		return createAttachmentUrl(baseURl, userId, tokenId, fileName, WebConstants.USER_PROFILE_PARAM_KEY);
	}

	public static String createUserProfilePicUrl(String baseURl, String userId){
		return createAttachmentUrl(baseURl, userId, "", null, WebConstants.USER_PROFILE_PARAM_KEY);
	}

	/**
	 * Create the url to an attachment image.
	 * @param baseURl
	 * @param id
	 * @param tokenId
	 * @param fileName
	 * @return
	 */
	public static String createAttachmentUrl(String baseURl, String id, String tokenId, String fileName, String paramKey){
		StringBuilder builder = new StringBuilder();
		builder.append(baseURl);
		builder.append("?"+paramKey+"=");
		builder.append(id);
		builder.append("&"+WebConstants.TOKEN_ID_PARAM_KEY+"=");
		builder.append(tokenId);
		builder.append("&"+WebConstants.WAIT_FOR_URL+"=true");
		//and do not cache
		builder.append(getParamForNoCaching());
		return builder.toString();
	}
	
	/**
	 * Does this entity have attachmet previews?
	 * @param entity
	 * @return
	 */
	public static boolean hasChildrenOrPreview(EntityBundle bundle){
		if(bundle == null) return true;
		if(bundle.getEntity() == null) return true;
		Boolean hasChildern = bundle.getHasChildren();
		if(hasChildern == null) return true;
		return hasChildern;
	}

	public static ArrayList<EntityType> orderForDisplay(List<EntityType> children) {
		ArrayList<EntityType> ordered = new ArrayList<EntityType>();
		
		if(children != null) {
			// fill map
			Map<String,EntityType> classToTypeMap = new HashMap<String, EntityType>();
			for(EntityType child : children) {
				classToTypeMap.put(child.getClassName(), child);
			}
			 
			// add child tabs in order
			for(String className : DisplayUtils.ENTITY_TYPE_DISPLAY_ORDER) {
				if(classToTypeMap.containsKey(className)) {
					EntityType child = classToTypeMap.get(className);
					classToTypeMap.remove(className);
					ordered.add(child);
				}
			}

			// add any remaining tabs that weren't covered by the display order
			for(String className : classToTypeMap.keySet()) {
				EntityType child = classToTypeMap.get(className);
				ordered.add(child);
			}							
		}
		
		return ordered;
	}
	
	public static Popover addPopover(Widget widget, String message) {
		Popover popover = new Popover(widget);
		popover.setPlacement(Placement.AUTO);
		popover.setIsHtml(true);
		popover.setContent(message);
		return popover;
	}
	
	public static Tooltip addToolTip(final Component widget, String message) {
		return addTooltip(widget, message, Placement.AUTO);
	}
	
	/**
	 * A list of tags that core attributes like 'title' cannot be applied to.
	 * This prevents them from having methods like addToolTip applied to them
	 */
	public static final String[] CORE_ATTR_INVALID_ELEMENTS = {"base", "head", "html", "meta",
															   "param", "script", "style", "title"};
	/**
	 * A counter variable for assigning unqiue id's to tool-tippified elements
	 */
	private static int tooltipCount= 0;
	private static int popoverCount= 0;
	
	public static Tooltip addTooltip(Widget widget, String tooltipText){
		return addTooltip(widget, tooltipText, Placement.AUTO);
	}
	
	/**
	 * Adds a twitter bootstrap tooltip to the given widget using the standard Synapse configuration
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
		t.setWidget(widget);
		t.setPlacement(pos);
		t.setText(tooltipText);
		t.setIsHtml(true);
		t.setIsAnimated(false);
		t.setTrigger(Trigger.HOVER);
		return t;
	}

	/**
	* Adds a popover to a target widget
	*/
	public static void addClickPopover(Widget widget, String title, String content, Placement placement) {
		Popover popover = new Popover();
		popover.setIsHtml(true);
		popover.setIsAnimated(true);
		popover.setTitle(title);
		popover.setPlacement(placement);
		popover.setTrigger(Trigger.CLICK);
		popover.setWidget(widget);
		popover.setContent(content);
	}

    /*
     * Private methods
     */

	private static boolean isNullOrEmpty(final String string) {
		return string == null || string.isEmpty();
	}

	private static boolean isPresent(String needle, String[] haystack) {
		for (String el : haystack) {
			if (needle.equalsIgnoreCase(el)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The preferred method for creating new global alerts.  For a
	 * default 'info' type alert, you can also use {@link showInfo(String, String)}
	 * @param alert
	 */
	public static void displayGlobalAlert(Alert alert) {
		Element container = DOM.getElementById(ALERT_CONTAINER_ID);
		DOM.insertChild(container, alert.getElement(), 0);
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

	public static native void setWindowTarget(JavaScriptObject window, String target)/*-{
    	window.location = target;
		}-*/;
	/**
	 * links in the wiki pages that reference other wiki pages don't include the domain.  this method adds the domain.
	 * @param html
	 * @return
	 */
	public static String fixWikiLinks(String html) {
		//adjust all wiki links so that they include the wiki domain
		return html.replaceAll("=\"/wiki", "=\""+WIKI_URL);
	}
	
	/**
	 * if you have plain text in the form www.youtube.com/embed/<videoid> (for example, www.youtube.com/embed/xSfd5mkkmGM), this method will convert the first occurrence of that text to an 
	 * embedded iframe.
	 * @return
	 */
	public static String fixEmbeddedYouTube(String html){
		int startYouTubeLinkIndex = html.indexOf("www.youtube.com/embed");
		while (startYouTubeLinkIndex > -1){
			int endYoutubeLinkIndex = html.indexOf("<", startYouTubeLinkIndex);
			StringBuilder sb = new StringBuilder();
			sb.append(html.substring(0, startYouTubeLinkIndex));
			sb.append("<iframe width=\"300\" height=\"169\" src=\"https://" + html.substring(startYouTubeLinkIndex, endYoutubeLinkIndex) + "\" frameborder=\"0\" allowfullscreen=\"true\"></iframe>");
			int t = sb.length();
			sb.append(html.substring(endYoutubeLinkIndex));
			html = sb.toString();
			//search after t (for the next embed)
			startYouTubeLinkIndex = html.indexOf("www.youtube.com/embed", t); 
		}
		return html;
	}
	
	public static String getYouTubeVideoUrl(String videoId) {
		return "http://www.youtube.com/watch?v=" + videoId;
	}
	
	public static String getYouTubeVideoId(String videoUrl) {
		String videoId = null;
		//parse out the video id from the url
		int start = videoUrl.indexOf("v=");
		if (start > -1) {
			int end = videoUrl.indexOf("&", start);
			if (end == -1)
				end = videoUrl.length();
			videoId = videoUrl.substring(start + "v=".length(), end);
		}
		if (videoId == null || videoId.trim().length() == 0) {
			throw new IllegalArgumentException("Could not determine the video ID from the given URL.");
		}
		return videoId;
	}
	
	public static Anchor createIconLink(AbstractImagePrototype icon, ClickHandler clickHandler) {
		Anchor anchor = new Anchor();
		anchor.setHTML(icon.getHTML());
		anchor.addClickHandler(clickHandler);
		return anchor;
	}

	public static String getVersionDisplay(Reference ref) {
		if (ref == null) return null;
		return getVersionDisplay(ref.getTargetId(), ref.getTargetVersionNumber());
	}
	
	public static String getVersionDisplay(String id, Long versionNumber) {
		String version = id;
		if(versionNumber != null) {
			version += " (#" + versionNumber + ")";
		}
		return version;		
	}
	
	public static SafeHtml get404Html() {
		return SafeHtmlUtils
				.fromSafeConstant("<div class=\"row\"><div class=\"col-xs-12\"><p class=\"margin-left-15 error left colored\">404</p><h1 class=\"margin-top-60-imp\">"
						+ DisplayConstants.PAGE_NOT_FOUND
						+ "</h1>"
						+ "<p>"
						+ DisplayConstants.PAGE_NOT_FOUND_DESC + "</p></div></div>");
	}
	
	public static String getWidgetMD(String attachmentName) {
		if (attachmentName == null)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append(WidgetConstants.WIDGET_START_MARKDOWN);
		sb.append(attachmentName);
		sb.append("}");
		return sb.toString();
	}
	
	public static SafeHtml get403Html() {
		return SafeHtmlUtils
				.fromSafeConstant("<div class=\"row\"><div class=\"col-xs-12\"><p class=\"margin-left-15 error left colored\">403</p><h1 class=\"margin-top-60-imp\">"
						+ DisplayConstants.FORBIDDEN
						+ "</h1>"
						+ "<p>"
						+ DisplayConstants.UNAUTHORIZED_DESC + "</p></div></div>");
	}
	
	/**
	 * 'Upload File' button for an entity
	 * @param entity 
	 * @param entityType 
	 */
	public static Widget getUploadButton(final EntityBundle entityBundle,
			EntityType entityType, final Uploader uploader,
			IconsImageBundle iconsImageBundle, EntityUpdatedHandler handler) {
		com.google.gwt.user.client.ui.Button uploadButton = DisplayUtils.createIconButton(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, ButtonType.DEFAULT, "glyphicon-arrow-up");
		return configureUploadWidget(uploadButton, uploader, iconsImageBundle, null, entityBundle, handler, entityType, DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK);
	}

	/**
	 * 'Upload File' button something other than an entity (for example the team icon)
	 */
	public static Widget getUploadButton(final CallbackP<String> fileHandleIdCallback, final Uploader uploader,	IconsImageBundle iconsImageBundle, String buttonText, ButtonType buttonType) {
		com.google.gwt.user.client.ui.Button uploadButton = DisplayUtils.createIconButton(buttonText, buttonType, null);
		return configureUploadWidget(uploadButton, uploader, iconsImageBundle, fileHandleIdCallback, null, null, null, buttonText);
	}
	
	/**
	 * 'Upload File' button
	 * @param entity 
	 * @param entityType 
	 */
	private static Widget configureUploadWidget(final FocusWidget uploadButton, final Uploader uploader,
			IconsImageBundle iconsImageBundle, final CallbackP<String> fileHandleIdCallback, final EntityBundle entityBundle, EntityUpdatedHandler handler, EntityType entityType, final String buttonText) {
		final Window window = new Window();  
		
		uploader.clearHandlers();
		// add user defined handler
		if (handler != null)
			uploader.addPersistSuccessHandler(handler);
		
		// add handlers for closing the window
		uploader.addPersistSuccessHandler(new EntityUpdatedHandler() {			
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				window.hide();
			}
		});
		uploader.addCancelHandler(new CancelHandler() {				
			@Override
			public void onCancel(CancelEvent event) {
				window.hide();
			}
		});
		uploadButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				window.removeAll();
				window.setPlain(true);
				window.setModal(true);		
				window.setHeading(buttonText);
				window.setLayout(new FitLayout());
				List<AccessRequirement> ars = null;
				Entity entity = null;
				boolean isEntity = true;
				
				if (entityBundle != null) {
					//is entity
					ars = entityBundle.getAccessRequirements();
					entity = entityBundle.getEntity();
				} else {
					//is something else that just wants a file handle id
					isEntity = false;
				}
				
				window.add(uploader.asWidget(entity, null,ars, fileHandleIdCallback,isEntity), new MarginData(5));
				window.show();
				window.setSize(uploader.getDisplayWidth(), uploader.getDisplayHeight());
			}
		});
		
		return uploadButton;
	}

	/**
	 * Provides same functionality as java.util.Pattern.quote().
	 * @param pattern
	 * @return
	 */
	public static String quotePattern(String pattern) {
		StringBuilder output = new StringBuilder();
	    for (int i = 0; i < pattern.length(); i++) {
	      if (ESCAPE_CHARACTERS_SET.contains(pattern.charAt(i)))
	    	output.append("\\");
	      output.append(pattern.charAt(i));
	    }
	    return output.toString();
	  }
	
	public static void updateTextArea(TextArea textArea, String newValue) {
		textArea.setValue(newValue);
		DomEvent.fireNativeEvent(Document.get().createChangeEvent(), textArea);
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
			boolean preview, Long wikiVersion) {
		String attachmentUrl = createWikiAttachmentUrl(baseFileHandleUrl, wikiKey, fileName, preview);
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
	public static String createWikiAttachmentUrl(String baseFileHandleUrl, WikiPageKey wikiKey, String fileName, boolean preview){
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
				WebConstants.WIKI_FILENAME_PARAM_KEY + "=" + fileName + "&" +
					WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY + "=" + Boolean.toString(preview) +
					wikiIdParam;
	}
		
	public static String createFileEntityUrl(String baseFileHandleUrl, String entityId, Long versionNumber, boolean preview){
		return createFileEntityUrl(baseFileHandleUrl, entityId, versionNumber, preview, false);
	}
	
	public static String getParamForNoCaching() {
		return WebConstants.NOCACHE_PARAM + new Date().getTime();
	}
	
	/**
	 * Create a url that points to the FileHandleServlet.
	 * WARNING: A GET request to this url will cause the file contents to be downloaded on the Servlet and sent back in the response.
	 * USE TO REQUEST SMALL FILES ONLY and CACHE THE RESULTS
	 * @param baseURl
	 * @param encodedRedirectUrl
	 * @return
	 */
	public static String createRedirectUrl(String baseFileHandleUrl, String encodedRedirectUrl){
		return baseFileHandleUrl + "?" + WebConstants.PROXY_PARAM_KEY + "=" + Boolean.TRUE +"&" + 
				WebConstants.REDIRECT_URL_KEY + "=" + encodedRedirectUrl;
	}
	
	/**
	 * Create the url to a FileEntity filehandle.
	 * @param baseURl
	 * @param entityid
	 * @return
	 */
	public static String createFileEntityUrl(String baseFileHandleUrl, String entityId, Long versionNumber, boolean preview, boolean proxy){
		String versionParam = versionNumber == null ? "" : "&" + WebConstants.ENTITY_VERSION_PARAM_KEY + "=" + versionNumber.toString();
		return baseFileHandleUrl + "?" +
				WebConstants.ENTITY_PARAM_KEY + "=" + entityId + "&" +
				WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY + "=" + Boolean.toString(preview) + "&" +
				WebConstants.PROXY_PARAM_KEY + "=" + Boolean.toString(proxy) +
				versionParam;
	}

	/**
	 * Create the url to a Table cell file handle.
	 * @param baseFileHandleUrl
	 * @param details
	 * @param preview
	 * @param proxy
	 * @return
	 */
	public static String createTableCellFileEntityUrl(String baseFileHandleUrl, TableCellFileHandle details, boolean preview, boolean proxy){		
		return baseFileHandleUrl + "?" +
				WebConstants.ENTITY_PARAM_KEY + "=" + details.getTableId() + "&" +
				WebConstants.TABLE_COLUMN_ID + "=" + details.getColumnId() + "&" +
				WebConstants.TABLE_ROW_ID + "=" + details.getRowId() + "&" +
				WebConstants.TABLE_ROW_VERSION_NUMBER + "=" + details.getVersionNumber() + "&" +
				WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY + "=" + Boolean.toString(preview) + "&" +
				WebConstants.PROXY_PARAM_KEY + "=" + Boolean.toString(proxy);
	}
	
	/**
	 * Create the url to a Team icon filehandle.
	 * @param baseURl
	 * @param teamId
	 * @return
	 */
	public static String createTeamIconUrl(String baseFileHandleUrl, String teamId){
		return baseFileHandleUrl + "?" +
				WebConstants.TEAM_PARAM_KEY + "=" + teamId;
	}


	public static String createEntityVersionString(Reference ref) {
		return createEntityVersionString(ref.getTargetId(), ref.getTargetVersionNumber());
	}
	
	public static String createEntityVersionString(String id, Long version) {
		if(version != null)
			return id+WebConstants.ENTITY_VERSION_STRING+version;
		else 
			return id;		
	}
	public static Reference parseEntityVersionString(String entityVersion) {
		String[] parts = entityVersion.split(WebConstants.ENTITY_VERSION_STRING);
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
	
	public static boolean isWikiSupportedType(Entity entity) {
		return (entity instanceof FileEntity || entity instanceof Folder || entity instanceof Project || entity instanceof TableEntity); 
	}
		
	public static boolean isRecognizedImageContentType(String contentType) {
		String lowerContentType = contentType.toLowerCase();
		return IMAGE_CONTENT_TYPES_SET.contains(lowerContentType);
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
	
	public static void configureAndShowEntityFinderWindow(final EntityFinder entityFinder, final Window window, final SelectedHandler<Reference> handler) {  				
		window.setPlain(true);
		window.setModal(true);
		window.setHeading(DisplayConstants.FIND_ENTITIES);
		window.setLayout(new FitLayout());
		window.add(entityFinder.asWidget(), new FitData(4));				
		window.addButton(new Button(DisplayConstants.SELECT, new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Reference selected = entityFinder.getSelectedEntity();
				handler.onSelected(selected);
			}
		}));
		window.addButton(new Button(DisplayConstants.BUTTON_CANCEL, new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
		}));
		window.setButtonAlign(HorizontalAlignment.RIGHT);
		window.show();
		window.setSize(entityFinder.getViewWidth(), entityFinder.getViewHeight());
		entityFinder.refresh();
	}

	public static void loadTableSorters(final HTMLPanel panel, SynapseJSNIUtils synapseJSNIUtils) {
		String id = WidgetConstants.MARKDOWN_TABLE_ID_PREFIX;
		int i = 0;
		Element table = panel.getElementById(id + i);
		while (table != null) {
			synapseJSNIUtils.tablesorter(id+i);
			i++;
			table = panel.getElementById(id + i);
		}
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
		DisplayUtils.addTooltip(htmlPanel, tooltip, Placement.BOTTOM);
		lc.add(htmlPanel);

		return lc;
	}
	
	public static Long getVersion(Entity entity) {
		Long version = null;
		if (entity != null && entity instanceof Versionable)
			version = ((Versionable) entity).getVersionNumber();
		return version;
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
	
	public static void addAnnotation(Annotations annos, String name, ANNOTATION_TYPE type) {
		// Add a new annotation
		if(ANNOTATION_TYPE.STRING == type){
			annos.addAnnotation(name, "");
		}else if(ANNOTATION_TYPE.DOUBLE == type){
			annos.addAnnotation(name, 0.0);
		}else if(ANNOTATION_TYPE.LONG == type){
			annos.addAnnotation(name, 0l);
		}else if(ANNOTATION_TYPE.DATE == type){
			annos.addAnnotation(name, new Date());
		}else{
			throw new IllegalArgumentException("Unknown type: "+type);
		}
	}
	
	public static void surroundWidgetWithParens(Panel container, Widget widget) {
		Text paren = new Text("(");
		paren.addStyleName("inline-block margin-left-5");
		container.add(paren);

		widget.addStyleName("inline-block");
		container.add(widget);

		paren = new Text(")");
		paren.addStyleName("inline-block margin-right-10");
		container.add(paren);
	}

	public static void showSharingDialog(final AccessControlListEditor accessControlListEditor, boolean canChangePermission, final Callback callback) {
		final Dialog window = new Dialog();
		// configure layout
		int windowHeight = canChangePermission ? 552 : 282;
		window.setSize(560, windowHeight);
		window.setPlain(true);
		window.setModal(true);
		window.setHeading(DisplayConstants.TITLE_SHARING_PANEL);
		window.setLayout(new FitLayout());
		window.add(accessControlListEditor.asWidget(), new FitData(4));			    
	    
		// configure buttons
		if (canChangePermission) {
			window.okText = "Save";
			window.cancelText = "Cancel";
			window.setButtons(Dialog.OKCANCEL);
		} else {
			window.cancelText = "Close";
			window.setButtons(Dialog.CANCEL);
		}
		window.setButtonAlign(HorizontalAlignment.RIGHT);
	    window.setHideOnButtonClick(false);
		window.setResizable(true);
		
		if (canChangePermission) {
			// "Apply" button
			// TODO: Disable the "Apply" button if ACLEditor has no unsaved changes
			Button applyButton = window.getButtonById(Dialog.OK);
			applyButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					// confirm close action if there are unsaved changes
					if (accessControlListEditor.hasUnsavedChanges()) {
						accessControlListEditor.pushChangesToSynapse(false, new AsyncCallback<EntityWrapper>() {
							@Override
							public void onSuccess(EntityWrapper result) {
								callback.invoke();
							}
							@Override
							public void onFailure(Throwable caught) {
								//failure notification is handled by the acl editor view.
							}
						});
					}
					window.hide();
				}
		    });
		}
		
		// "Close" button				
		Button closeButton = window.getButtonById(Dialog.CANCEL);
	    closeButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
	    });
		
		window.show();
	}

	public static LayoutContainer createRowContainer() {
		LayoutContainer row;
		row = new LayoutContainer();
		row.setStyleName("row");
		return row;
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

	public static String getFontelloIcon(String iconClass) {
		return "<span class=\"icon-" + iconClass + "\"></span>";
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
			HTML profilePicture = new HTML(DisplayUtils.getFontelloIcon(iconClass + " font-size-58 padding-2 imageButton userProfileImage lightGreyText margin-0-imp-before"));
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
	
	public static SimpleComboBox<String> createSimpleComboBox(List<String> values, String defaultValue){
		final SimpleComboBox<String> cb = new SimpleComboBox<String>();
		cb.add(values);
		cb.setSimpleValue(defaultValue);
		cb.setTypeAhead(false);
		cb.setEditable(false);
		cb.setForceSelection(true);
		cb.setTriggerAction(TriggerAction.ALL);
		return cb;
	}
	
	public static HTML getNewLabel(boolean superScript) {		
		final HTML label = new HTML(DisplayConstants.NEW);
		label.addStyleName("label label-info margin-left-5");
		if(superScript) label.addStyleName("tabLabel");
		Timer t = new Timer() {
		      @Override
		      public void run() {
					label.setVisible(false);
		      }
		    };
		t.schedule(30000); // hide after 30 seconds
	    return label;
	}
	
	public static void setPlaceholder(Widget w, String placeholder) {
		w.getElement().setAttribute("placeholder", placeholder);
	}
	
	public static InlineHTML createFormHelpText(String text) {
		InlineHTML label = new InlineHTML(text);
		label.addStyleName("help-block");
		return label;
	}

	public static String getShareMessage(String displayName, String entityId, String hostUrl) {
		return displayName + DisplayConstants.SHARED_ON_SYNAPSE + ":\n"+hostUrl+"#!Synapse:"+entityId+"\n\n"+DisplayConstants.TURN_OFF_NOTIFICATIONS+hostUrl+"#!Settings:0";
		//alternatively, could use the gwt I18n Messages class client side
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
	
	public static String getPreviewSuffix(Boolean isPreview) {
		return isPreview ? WidgetConstants.DIV_ID_PREVIEW_SUFFIX : "";
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

	public static String getTableRowViewAreaToken(String id) {
		return "row/" + id;
	}

	public static String getTableRowViewAreaToken(String id, String version) {
		String str = "row/" + id;
		if (version != null)
			str += "/rowversion/" + version;
		return str;
	}
	
	public static void goToLastPlace(GlobalApplicationState globalApplicationState) {
		Place forwardPlace = globalApplicationState.getLastPlace();
		if(forwardPlace == null) {
			forwardPlace = new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
		}
		globalApplicationState.getPlaceChanger().goTo(forwardPlace);
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
	
	public static void configureShowHide(final InlineLabel label, final LayoutContainer content) {
		label.setText(DisplayConstants.SHOW_LC);
		label.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (content.isVisible()) {
					content.setVisible(false);
					label.setText(DisplayConstants.SHOW_LC);
				} else {
					content.setVisible(true);
					label.setText(DisplayConstants.HIDE_LC);
				}
				content.layout(true);
			}
		});
	}
}
