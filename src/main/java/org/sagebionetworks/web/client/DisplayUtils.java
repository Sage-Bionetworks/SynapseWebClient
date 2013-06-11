package org.sagebionetworks.web.client;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sagebionetworks.gwt.client.schema.adapter.DateUtils;
import org.sagebionetworks.repo.model.Analysis;
import org.sagebionetworks.repo.model.Code;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
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
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
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
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.Alert;
import org.sagebionetworks.web.client.widget.Alert.AlertType;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.NodeType;
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
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class DisplayUtils {

	public static final String HELP_EMAIL_ADDRESS = "synapseInfo@sagebase.org";
	public static final String HELP_EMAIL_ADDRESS_LINK = "<a href=\""+ DisplayUtils.HELP_EMAIL_ADDRESS +"\" class=\"link\">contact us</a>";
	public static final String NEWS_FEED_URL = "https://sagesynapse.wordpress.com/feed/";
	public static final String SUPPORT_RECENT_ACTIVITY_URL = "http://support.sagebase.org/sagebase?view=recent";
	public static final String WIKI_URL = "https://sagebionetworks.jira.com/wiki";
	public static final String USER_GUIDE_ID = "syn1669771";
	
	public static final String BCC_SUMMARY_CONTENT_PAGE_ID = "24084489";
	public static final String DATA_ACCESS_LEVELS_CONTENT_PAGE_ID = "21168199";
	
	public static final String CHALLENGE_OVERVIEW_CONTENT_PROVIDER_ID = "challengeOverviewContent";
	public static final String BCC_SUMMARY_PROVIDER_ID = "bccSummaryContent";
	public static final String DATA_ACCESS_LEVELS_PROVIDER_ID = "dataAccessLevelsContent";
	public static final String NEWS_FEED_PROVIDER_ID = "newsFeed";
	
	
	public static final String FASTPASS_LOGIN_COOKIE_VALUE = "fastpass-logging-in";
	public static final String FASTPASS_SIGNOVER_URL = "http://support.sagebase.org/fastpass/finish_signover?company=sagebase&fastpass=";
	public static final String WIKI_CONTENT_URL = "https://sagebionetworks.jira.com/wiki/rest/prototype/1/content/";
	public static final String WIKI_PAGE_SOURCE_CONTENT_URL = "https://sagebionetworks.jira.com/wiki/plugins/viewsource/viewpagesrc.action";
	public static final String WIKI_SOURCE_DELIMITER = "<p>&nbsp;</p>";
	
	public static final String SUPPORT_URL = "support.sagebase.org";
	
	private static final String ALERT_CONTAINER_ID = "alertContainer";
	private static final String REGEX_CLEAN_ANNOTATION_KEY = "^[a-z,A-Z,0-9,_,.]+";
	private static final String REGEX_CLEAN_ENTITY_NAME = "^[a-z,A-Z,0-9,_,., ,\\-,\\+,(,)]+";
	public static final String REPO_ENTITY_NAME_KEY = "name";
	public static final String WHITE_SPACE = "&nbsp;";
	public static final String BREADCRUMB_SEP = "&nbsp;&raquo;&nbsp;";
	
	public static final String NODE_DESCRIPTION_KEY = "description";
	public static final String LAYER_COLUMN_DESCRIPTION_KEY_PREFIX = "colDesc_";
	public static final String LAYER_COLUMN_UNITS_KEY_PREFIX = "colUnits_";
	
	public static final String MIME_TYPE_JPEG = "image/jpeg";
	public static final String MIME_TYPE_PNG = "image/png";
	public static final String MIME_TYPE_GIF = "image/gif";
	
	public static final String DEFAULT_PLACE_TOKEN = "0";
	
	public static final String R_CLIENT_DOWNLOAD_CODE = "source('http://depot.sagebase.org/CRAN.R')<br/>pkgInstall(c(\"synapseClient\"))";
	public static final String PYTHON_CLIENT_DOWNLOAD_CODE = "# From Terminal Prompt:<br/>pip install synapseclient<br/><br/># or<br/>easy_install synapseclient";
	

	
	private static final String ERROR_OBJ_REASON_KEY = "reason";
	public static final String SYNAPSE_ID_PREFIX = "syn";
	public static final String DEFAULT_RSTUDIO_URL = "http://localhost:8787";
	public static final int FULL_ENTITY_PAGE_WIDTH = 940;
	public static final int FULL_ENTITY_PAGE_HEIGHT = 500;
	public static final int BIG_BUTTON_HEIGHT_PX = 36;
	private static final int MARKDOWN_WIDTH_WIDE_PX = 940;
	private static final int MARKDOWN_WIDTH_NARROW_PX = 660;

	
	public static final Character[] ESCAPE_CHARACTERS = new Character[] { '.','{','}','(',')','+','-' };
	public static final HashSet<Character> ESCAPE_CHARACTERS_SET = new HashSet<Character>(Arrays.asList(ESCAPE_CHARACTERS));
	
	public static final String[] IMAGE_CONTENT_TYPES = new String[] {"image/bmp","image/pjpeg","image/jpeg","image/jpg", "image/jpe","image/gif","image/png"};
	public static final HashSet<String> IMAGE_CONTENT_TYPES_SET = new HashSet<String>(Arrays.asList(IMAGE_CONTENT_TYPES));
	
	public static final double BASE = 1024, KB = BASE, MB = KB*BASE, GB = MB*BASE, TB = GB*BASE;
	
	/**
	 * Sometimes we are forced to use a table to center an image in a fixed space. 
	 * This is the third option from: http://stackoverflow.com/questions/388180/how-to-make-an-image-center-vertically-horizontally-inside-a-bigger-div
	 * It should only be used when the first two options are not an option.
	 * Place your image between the start and end.
	 */
	public static final String IMAGE_CENTERING_TABLE_START = "<table width=\"100%\" height=\"100%\" align=\"center\" valign=\"center\"><tr><td>";
	public static final String IMAGE_CENTERING_TABLE_END = "</td></tr></table>";
	
	/*
	 * Style names
	 */
	public static final String STYLE_NAME_GXT_GREY_BACKGROUND = "gxtGreyBackground";
	public static final String STYLE_CODE_CONTENT = "codeContent";
	public static final String STYLE_SMALL_GREY_TEXT = "smallGreyText";
	public static final String HOMESEARCH_BOX_STYLE_NAME = "homesearchbox";
	public static final String STYLE_SMALL_SEARCHBOX = "smallsearchbox";
	public static final String STYLE_OTHER_SEARCHBOX = "othersearchbox";
	public static final String STYLE_BREAK_WORD = "break-word";
	public static final String STYLE_WHITE_BACKGROUND = "whiteBackground";
	public static final String STYLE_DISPLAY_INLINE = "inline-block";
	public static final String STYLE_BLACK_TEXT = "blackText";
	
	public static final String UPLOAD_SUCCESS = "Upload Success";
	
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
	 * Returns a properly aligned name and e-mail address for a given UserGroupHeader
	 * @param principal
	 * @return
	 */
	public static String getUserNameEmailHtml(UserGroupHeader principal) {
		if (principal == null) return "";
		String name = principal.getDisplayName() == null ? "" : principal.getDisplayName();
		String email = principal.getEmail() == null ? "" : principal.getEmail();
		return DisplayUtilsGWT.TEMPLATES.nameAndEmail(name, email).asString();
	}
	
	/**
	 * Returns a properly aligned name and description for a special user or group
	 * @param name of user or group
	 * @return
	 */
	public static String getUserNameEmailHtml(String name, String description) {
		return DisplayUtilsGWT.TEMPLATES.nameAndEmail(name, description).asString();
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
	 * Handles the exception. Resturn true if the user has been alerted to the exception already
	 * @param ex
	 * @param placeChanger
	 * @return true if the user has been prompted
	 */
	public static boolean handleServiceException(Throwable ex, PlaceChanger placeChanger, UserSessionData currentUser) {
		if(ex instanceof UnauthorizedException) {
			// send user to login page						
			showInfo("Session Timeout", "Your session has timed out. Please login again.");
			placeChanger.goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			return true;
		} else if(ex instanceof ForbiddenException) {			
			if(currentUser == null) {				
				MessageBox.info(DisplayConstants.ERROR_LOGIN_REQUIRED, DisplayConstants.ERROR_LOGIN_REQUIRED, null);
				placeChanger.goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			} else {
				MessageBox.info(DisplayConstants.TITLE_UNAUTHORIZED, DisplayConstants.ERROR_FAILURE_PRIVLEDGES, null);
			}
			return true;
		} else if(ex instanceof BadRequestException) {
			String reason = ex.getMessage();			
			String message = DisplayConstants.ERROR_BAD_REQUEST_MESSAGE;
			if(reason.matches(".*entity with the name: .+ already exites.*")) {
				message = DisplayConstants.ERROR_DUPLICATE_ENTITY_MESSAGE;
			}			
			MessageBox.info("Error", message, null);
			return true;
		} else if(ex instanceof NotFoundException) {
			MessageBox.info("Not Found", DisplayConstants.ERROR_NOT_FOUND, null);
			placeChanger.goTo(new Home(DisplayUtils.DEFAULT_PLACE_TOKEN));
			return true;
		} else if(ex instanceof ReadOnlyModeException) {
			MessageBox.info(DisplayConstants.READ_ONLY_MODE, DisplayConstants.SYNAPSE_IN_READ_ONLY_MODE, null);
			return true;
		} else if(ex instanceof SynapseDownException) {
			placeChanger.goTo(new Down(DisplayUtils.DEFAULT_PLACE_TOKEN));
			return true;
		}
		
		// For other exceptions, allow the consumer to send a good message to the user
		return false;
	}
	
	/**
	 * Handle JSONObjectAdapterException.  This will occur when the client is pointing to an incompatible repo version. 
	 * @param ex
	 * @param placeChanger
	 */
	public static boolean handleJSONAdapterException(JSONObjectAdapterException ex, PlaceChanger placeChanger, UserSessionData currentUser) {
		MessageBox.info("Incompatible Client Version", DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION, null);
		placeChanger.goTo(new Home(DisplayUtils.DEFAULT_PLACE_TOKEN));
		return true;
	}

	
	/*
	 * Button Saving 
	 */
	public static void changeButtonToSaving(Button button, SageImageBundle sageImageBundle) {
		button.setText(DisplayConstants.BUTTON_SAVING);
		button.setIcon(AbstractImagePrototype.create(sageImageBundle.loading16()));
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
		return DisplayUtils.getIconHtml(sageImageBundle.loading16()) + " " + message + "...";
	}


	/**
	 * Shows an info message to the user in the "Global Alert area".
	 * For more precise control over how the message appears,
	 * use the {@link displayGlobalAlert(Alert)} method.
	 * @param title
	 * @param message
	 */
	public static void showInfo(String title, String message) {
		Alert alert = new Alert(title, message);
		alert.setAlertType(AlertType.Info);
		alert.setTimeout(4000);
		displayGlobalAlert(alert);
	}
	
	public static void showErrorMessage(String message) {
		MessageBox.info(DisplayConstants.TITLE_ERROR, message, null);
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
	
	public static String getWarningHtml(String title, String warningText) {
		return "<div class=\"alert alert-block\"><strong>"+ title + "</strong><br/> " + warningText + "</div>";
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
		Synapse place = new Synapse(entityId, versionNumber);
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
		String fullPlaceName = place.getName();		
		fullPlaceName = fullPlaceName.replaceAll(".+\\.", "");
		return fullPlaceName;
	}
	
	private static String getWikiPlaceString(Class<Wiki> place) {
		String fullPlaceName = place.getName();		
		fullPlaceName = fullPlaceName.replaceAll(".+\\.", "");
		return fullPlaceName;
	}
	
	private static String getSearchPlaceString(Class<Search> place) {
		String fullPlaceName = place.getName();		
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
	public static HorizontalPanel createFullWidthLoadingPanel(SageImageBundle sageImageBundle) {
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
	public static HorizontalPanel createFullWidthLoadingPanel(SageImageBundle sageImageBundle, String message) {
		HorizontalPanel hp = new HorizontalPanel();
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hp.setPixelSize(FULL_ENTITY_PAGE_WIDTH, FULL_ENTITY_PAGE_HEIGHT);
		Widget w = new HTML(SafeHtmlUtils.fromSafeConstant(
				DisplayUtils.getIconHtml(sageImageBundle.loading31()) +" "+ message));	
		hp.add(w);
		return hp;
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
	public static void addTooltip(final SynapseJSNIUtils util, Widget widget, String tooltipText, TOOLTIP_POSITION pos){
		Map<String, String> optionsMap = new HashMap<String, String>();
		optionsMap.put("title", tooltipText);
		optionsMap.put("data-placement", pos.toString().toLowerCase());
		optionsMap.put("data-animation", "false");
		optionsMap.put("data-html", "true");
		addTooltip(util, widget, optionsMap);
	}
		
	private static void addTooltip(final SynapseJSNIUtils util, Widget widget, Map<String, String> optionsMap) {
		final Element el = widget.getElement();

		String id = isNullOrEmpty(el.getId()) ? "sbn-tooltip-"+(tooltipCount++) : el.getId();
		el.setId(id);
		optionsMap.put("id", id);
		optionsMap.put("rel", "tooltip");

		if (el.getNodeType() == 1 && !isPresent(el.getNodeName(), CORE_ATTR_INVALID_ELEMENTS)) {
			// If nodeName is a tag and not in the INVALID_ELEMENTS list then apply the appropriate transformation
			
			applyAttributes(el, optionsMap);

			widget.addAttachHandler( new AttachEvent.Handler() {
				@Override
				public void onAttachOrDetach(AttachEvent event) {
					if (event.isAttached()) {
						util.bindBootstrapTooltip(el.getId());
					} else {
						util.hideBootstrapTooltip(el.getId());
					}
				}
			});
		}
	}

	public static void addClickPopover(final SynapseJSNIUtils util, Widget widget, String title, String content, TOOLTIP_POSITION pos) {
		Map<String, String> optionsMap = new HashMap<String, String>();
		optionsMap.put("data-html", "true");
		optionsMap.put("data-animation", "true");
		optionsMap.put("title", title);
		optionsMap.put("data-placement", pos.toString().toLowerCase());
		optionsMap.put("trigger", "click");		
		addPopover(util, widget, content, optionsMap);
	}

	public static void addHoverPopover(final SynapseJSNIUtils util, Widget widget, String title, String content, TOOLTIP_POSITION pos) {
		Map<String, String> optionsMap = new HashMap<String, String>();
		optionsMap.put("data-html", "true");
		optionsMap.put("data-animation", "true");
		optionsMap.put("title", title);
		optionsMap.put("data-placement", pos.toString().toLowerCase());
		optionsMap.put("data-trigger", "hover");		
		addPopover(util, widget, content, optionsMap);
	}

	
	/**
	 * Adds a popover to a target widget
	 * 
	 * Same warnings apply as to {@link #addTooltip(SynapseJSNIUtils, Widget, String) addTooltip}
	 */
	public static void addPopover(final SynapseJSNIUtils util, Widget widget, String content, Map<String, String> optionsMap) {
		final Element el = widget.getElement();
		el.setAttribute("data-content", content);

		String id = isNullOrEmpty(el.getId()) ? "sbn-popover-"+(popoverCount++) : el.getId();
		optionsMap.put("id", id);
		optionsMap.put("rel", "popover");

		if (el.getNodeType() == 1 && !isPresent(el.getNodeName(), CORE_ATTR_INVALID_ELEMENTS)) {
			// If nodeName is a tag and not in the INVALID_ELEMENTS list then apply the appropriate transformation

			applyAttributes(el, optionsMap);

			widget.addAttachHandler( new AttachEvent.Handler() {
				@Override
				public void onAttachOrDetach(AttachEvent event) {
					if (event.isAttached()) {
						util.bindBootstrapPopover(el.getId());
					}
				}
			});
		}
	}

	public static void applyAttributes(final Element el,
			Map<String, String> optionsMap) {
		for (Entry<String, String> option : optionsMap.entrySet()) {
			DOM.setElementAttribute(el, option.getKey(), option.getValue());
		}
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
    	var window = $wnd.open(url, name, features);
    	return window;
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
		return html.replaceAll("=\"/wiki", "=\""+DisplayUtils.WIKI_URL);
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
				.fromSafeConstant("<div class=\"span-24\"><p class=\"error left colored\">404</p><h1>"
						+ DisplayConstants.PAGE_NOT_FOUND
						+ "</h1>"
						+ "<p>"
						+ DisplayConstants.PAGE_NOT_FOUND_DESC + "</p></div>");
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
				.fromSafeConstant("<div class=\"span-24\"><p class=\"error left colored\">403</p><h1>"
						+ DisplayConstants.UNAUTHORIZED
						+ "</h1>"
						+ "<p>"
						+ DisplayConstants.UNAUTHORIZED_DESC + "</p></div>");
	}
	
	/**
	 * 'Upload File' button
	 * @param entity 
	 * @param entityType 
	 */
	public static Widget getUploadButton(final EntityBundle entityBundle,
			EntityType entityType, final Uploader uploader,
			IconsImageBundle iconsImageBundle, EntityUpdatedHandler handler) {
		Button uploadButton = new Button(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, AbstractImagePrototype.create(iconsImageBundle.NavigateUp16()));
		uploadButton.setHeight(25);
		final Window window = new Window();  
		window.addButton(new Button(DisplayConstants.BUTTON_CANCEL, new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
		}));
		uploader.clearHandlers();
		// add user defined handler
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
		
		uploadButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				window.removeAll();
				window.setSize(uploader.getDisplayWidth(), uploader.getDisplayHeight());
				window.setPlain(true);
				window.setModal(true);		
				window.setHeading(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK);
				window.setLayout(new FitLayout());			
				window.add(uploader.asWidget(entityBundle.getEntity(), entityBundle.getAccessRequirements()), new MarginData(5));
				window.show();
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
		return cookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY) != null;
	}

	public static void setTestWebsite(boolean testWebsite, CookieProvider cookies) {
		if (testWebsite && !isInTestWebsite(cookies)) {
			//set the cookie
			cookies.setCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY, "true");
		} else{
			cookies.removeCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY);
		}
	}
	
	public static final String SYNAPSE_TEST_WEBSITE_COOKIE_KEY = "SynapseTestWebsite";	

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

			//if preview, then avoid cache
			String nocacheParam = preview ? "&nocache=" + new Date().getTime()  : "";
		return baseFileHandleUrl + "?" +
				WebConstants.WIKI_OWNER_ID_PARAM_KEY + "=" + wikiKey.getOwnerObjectId() + "&" +
				WebConstants.WIKI_OWNER_TYPE_PARAM_KEY + "=" + wikiKey.getOwnerObjectType() + "&"+
				WebConstants.WIKI_FILENAME_PARAM_KEY + "=" + fileName + "&" +
					WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY + "=" + Boolean.toString(preview) +
					wikiIdParam + nocacheParam;
	}
		
	public static String createFileEntityUrl(String baseFileHandleUrl, String entityId, Long versionNumber, boolean preview){
		return createFileEntityUrl(baseFileHandleUrl, entityId, versionNumber, preview, false);
	}

	/**
	 * Create the url to a FileEntity filehandle.
	 * @param baseURl
	 * @param entityid
	 * @return
	 */
	public static String createFileEntityUrl(String baseFileHandleUrl, String entityId, Long versionNumber, boolean preview, boolean proxy){
		String versionParam = versionNumber == null ? "" : "&" + WebConstants.ENTITY_VERSION_PARAM_KEY + "=" + versionNumber.toString();
		//if preview, then avoid cache
		String nocacheParam = preview ? "&nocache=" + new Date().getTime()  : "";
		return baseFileHandleUrl + "?" +
				WebConstants.ENTITY_PARAM_KEY + "=" + entityId + "&" +
				WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY + "=" + Boolean.toString(preview) + "&" +
				WebConstants.PROXY_PARAM_KEY + "=" + Boolean.toString(proxy) +
				versionParam + nocacheParam;
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
		return (entity instanceof FileEntity || entity instanceof Folder || entity instanceof Project); 
	}
		
	public static boolean isRecognizedImageContentType(String contentType) {
		String lowerContentType = contentType.toLowerCase();
		return IMAGE_CONTENT_TYPES_SET.contains(lowerContentType);
	}
	
	public static boolean isTextType(String contentType) {
		return contentType.toLowerCase().startsWith("text/");
	}
	
	public static boolean isCSV(String contentType) {
		return contentType.toLowerCase().startsWith("text/csv");
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
		window.setSize(entityFinder.getViewWidth(), entityFinder.getViewHeight());
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

	public static Widget getShareSettingsDisplay(String prefix, boolean isPublic, SynapseJSNIUtils synapseJSNIUtils) {
		if(prefix == null) prefix = "";
		final SimplePanel lc = new SimplePanel();
		lc.addStyleName(STYLE_DISPLAY_INLINE);
		String styleName = isPublic ? "public-acl-image" : "private-acl-image";
		String description = isPublic ? DisplayConstants.PUBLIC_ACL_ENTITY_PAGE : DisplayConstants.PRIVATE_ACL_ENTITY_PAGE;
		String tooltip = isPublic ? DisplayConstants.PUBLIC_ACL_DESCRIPTION : DisplayConstants.PRIVATE_ACL_DESCRIPTION;

		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant(prefix + "<div class=\"" + styleName+ "\" style=\"display:inline; position:absolute\"></div>");
		shb.appendHtmlConstant("<span style=\"margin-left: 20px;\">"+description+"</span>");

		//form the html
		HTMLPanel htmlPanel = new HTMLPanel(shb.toSafeHtml());
		htmlPanel.addStyleName("inline-block");
		DisplayUtils.addTooltip(synapseJSNIUtils, htmlPanel, tooltip, TOOLTIP_POSITION.BOTTOM);
		lc.add(htmlPanel);

		return lc;
	}
		
}
