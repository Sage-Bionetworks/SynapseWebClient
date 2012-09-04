package org.sagebionetworks.web.client;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gwttime.time.DateTime;
import org.gwttime.time.format.ISODateTimeFormat;
import org.sagebionetworks.repo.model.Analysis;
import org.sagebionetworks.repo.model.Code;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ExpressionData;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.GenotypeData;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.PhenotypeData;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.RObject;
import org.sagebionetworks.repo.model.Step;
import org.sagebionetworks.repo.model.Study;
import org.sagebionetworks.repo.model.Summary;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.Alert;
import org.sagebionetworks.web.client.widget.Alert.AlertType;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.NodeType;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
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
import com.google.gwt.user.client.ui.Widget;

public class DisplayUtils {

	public static final String NEWS_FEED_URL = "https://sagesynapse.wordpress.com/feed/";
	public static final String WIKI_URL = "https://sagebionetworks.jira.com/wiki";
	public static final String BCC_CONTENT_PAGE_URL = "https://sagebionetworks.jira.com/wiki/plugins/viewsource/viewpagesrc.action?pageId=24084517";
	public static final String BCC_SUMMARY_CONTENT_PAGE_URL = "https://sagebionetworks.jira.com/wiki/plugins/viewstorage/viewpagestorage.action?pageId=24084489";

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
	
	public static final String R_CLIENT_DOWNLOAD_CODE = "source('http://sage.fhcrc.org/CRAN.R')<br/>pkgInstall(c(\"synapseClient\"))";
	
	private static final String ERROR_OBJ_REASON_KEY = "reason";
	public static final String ENTITY_PARENT_ID_KEY = "parentId";
	public static final String ENTITY_EULA_ID_KEY = "eulaId";
	public static final String ENTITY_PARAM_KEY = "entityId";
	public static final String USER_PROFILE_PARAM_KEY = "userId";
	public static final String TOKEN_ID_PARAM_KEY = "tokenId";
	public static final String WAIT_FOR_URL = "waitForUrl";
	public static final String ENTITY_CREATEDBYPRINCIPALID_KEY = "createdByPrincipalId";
	public static final String MAKE_ATTACHMENT_PARAM_KEY = "makeAttachment";
	public static final String SYNAPSE_ID_PREFIX = "syn";
	public static final String DEFAULT_RSTUDIO_URL = "http://localhost:8787";
	public static final String ETAG_KEY = "etag";
	
	public static final int FULL_ENTITY_PAGE_WIDTH = 940;
	public static final int FULL_ENTITY_PAGE_HEIGHT = 500;
	public static final int BIG_BUTTON_HEIGHT_PX = 36;
	
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


	/*
	 * Search
	 */
	public final static String SEARCH_KEY_NODE_TYPE = "node_type";
	public final static String SEARCH_KEY_SPECIES = "species";
	public final static String SEARCH_KEY_DISEASE = "disease";
	public final static String SEARCH_KEY_MODIFIED_ON = "modified_on";
	public final static String SEARCH_KEY_CREATED_ON = "created_on";
	public final static String SEARCH_KEY_TISSUE = "tissue";
	public final static String SEARCH_KEY_NUM_SAMPLES = "num_samples";
	public final static String SEARCH_KEY_CREATED_BY = "created_by";
	public final static List<String> FACETS_DISPLAY_ORDER = Arrays
			.asList(new String[] { SEARCH_KEY_NODE_TYPE, SEARCH_KEY_SPECIES,
					SEARCH_KEY_DISEASE, SEARCH_KEY_MODIFIED_ON,
					SEARCH_KEY_CREATED_ON, SEARCH_KEY_TISSUE,
					SEARCH_KEY_NUM_SAMPLES, SEARCH_KEY_CREATED_BY });
	public static final String UPLOAD_SUCCESS = "Upload Success";
	
	public static final String[] ENTITY_TYPE_DISPLAY_ORDER = new String[] {
			Folder.class.getName(), Study.class.getName(), Data.class.getName(),
			Code.class.getName(), Link.class.getName(), 
			Analysis.class.getName(), Step.class.getName(), 
			RObject.class.getName(), PhenotypeData.class.getName(), 
			ExpressionData.class.getName(),	GenotypeData.class.getName() };
	
	
	public static SearchQuery getDefaultSearchQuery() {		
		SearchQuery query = new SearchQuery();
		// start with a blank, valid query
		query.setQueryTerm(Arrays.asList(new String[] {""}));		
		query.setReturnFields(Arrays.asList(new String[] {"name","description","id", "node_type_r", "created_by_r", "created_on", "modified_by_r", "modified_on", "path"}));
		
		// exclude links
		List<KeyValue> bq = new ArrayList<KeyValue>();
		KeyValue kv = new KeyValue();
		kv.setKey("node_type");
		kv.setValue("link");
		kv.setNot(true);
		bq.add(kv);
		query.setBooleanQuery(bq);
		
		query.setFacet(FACETS_DISPLAY_ORDER);
		
		return query;
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
	
	public static String uppercaseFirstLetter(String display) {
		return display.substring(0, 1).toUpperCase() + display.substring(1);		
	}
	
	public static SafeHtml getRClientEntityLoad(String id) {
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("# " + DisplayConstants.LABEL_R_CLIENT_GET_ENTITY + " <br/>")  
		.appendEscaped(id).appendHtmlConstant(" &lt;- getEntity('").appendEscaped(id).appendHtmlConstant("')")
		.appendHtmlConstant("<br/><br/># " + DisplayConstants.LABEL_R_CLIENT_LOAD_ENTITY + " <br/>")
		.appendEscaped(id).appendHtmlConstant(" &lt;- loadEntity('")
		.appendEscaped(id).appendHtmlConstant("')");
		return shb.toSafeHtml();
	}	
	
	public static String convertDateToString(Date toFormat) {
		if(toFormat == null) throw new IllegalArgumentException("Date cannot be null");
		DateTime dt = new DateTime(toFormat.getTime());
		return ISODateTimeFormat.dateTime().print(dt);
	}
	
	public static String converDataToPrettyString(Date toFormat) {
		if(toFormat == null) throw new IllegalArgumentException("Date cannot be null");
		DateTime dt = new DateTime(toFormat.getTime());
		return ISODateTimeFormat.dateTime().print(dt);		
	}
	
	/**
	 * Converts a date to just a date.
     * @return  yyyy-MM-dd
	 * @return
	 */
	public static String converDateaToSimpleString(Date toFormat) {
		if(toFormat == null) throw new IllegalArgumentException("Date cannot be null");
		DateTime dt = new DateTime(toFormat.getTime());
		return ISODateTimeFormat.date().print(dt);		
	}
 
	public static Date convertStringToDate(String toFormat) {
		if(toFormat == null) throw new IllegalArgumentException("Date cannot be null");
		DateTime dt = ISODateTimeFormat.dateTime().parseDateTime(toFormat);
		return dt.toDate();
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
		return getPlaceString(Synapse.class) + ":" + place.toToken();
	}
	
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

	
	/*
	 * Private methods
	 */
	private static String getPlaceString(Class<Synapse> place) {
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
		HorizontalPanel hp = new HorizontalPanel();
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hp.setPixelSize(FULL_ENTITY_PAGE_WIDTH, FULL_ENTITY_PAGE_HEIGHT);
		Widget w = new HTML(SafeHtmlUtils.fromSafeConstant(
				DisplayUtils.getIconHtml(sageImageBundle.loading31()) + " Loading..."));	
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
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseCode16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseCode24();			
		} else if(Data.class.getName().equals(className) ||
				ExpressionData.class.getName().equals(className) ||
				GenotypeData.class.getName().equals(className) ||
				PhenotypeData.class.getName().equals(className)) {
			// Data
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseData16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseData24();			
		} else if(Folder.class.getName().equals(className)) {
			// Folder
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseFolder16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseFolder24();			
//		} else if(Model.class.getName().equals(className)) {
			// Model
//			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseModel16();
//			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseModel24();			
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
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseStudy16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseStudy24();			
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
		return createAttachmentUrl(baseURl, entityId, tokenId, fileName, DisplayUtils.ENTITY_PARAM_KEY);
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
		return createAttachmentUrl(baseURl, userId, tokenId, fileName, DisplayUtils.USER_PROFILE_PARAM_KEY);
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
		builder.append("&"+DisplayUtils.TOKEN_ID_PARAM_KEY+"=");
		builder.append(tokenId);
		builder.append("&"+DisplayUtils.WAIT_FOR_URL+"=true");
		return builder.toString();
	}
	
	
	/**
	 * Does this entity have attachment previews?
	 * @param entity
	 * @return
	 */
	public static boolean hasAttachmentPreviews(Entity entity){
		if(entity == null) return false;
		if(entity.getAttachments() == null) return false;
		if(entity.getAttachments().size() < 1) return false;
		for(AttachmentData data: entity.getAttachments()){
			if(data.getPreviewId() != null) return true;
		}
		return false;
	}
	
	/**
	 * Does this entity have attachmet previews?
	 * @param entity
	 * @return
	 */
	public static boolean hasChildrenOrPreview(EntityBundle bundle){
		if(bundle == null) return true;
		if(bundle.getEntity() == null) return true;
		Long count = bundle.getChildCount();
		if(count == null) return true;
		return count > 0;
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
	 * Adds a twitter bootstrap tooltip to the given widget
	 *
	 * CAUTION - If not used with a non-block level element like
	 * an anchor, img, or span the results will probably not be
	 * quite what you want.  Read the twitter bootstrap documentation
	 * for the options that you can specify in optionsMap
	 *
	 * @param util the JSNIUtils class (or mock)
	 * @param widget the widget to attach the tooltip to
	 * @param optionsMap a map containing the options for the tooltip and the text
	 */
	public static void addTooltip(final SynapseJSNIUtils util, Widget widget, Map<String, String> optionsMap) {
		final Element el = widget.getElement();

		String id = isNullOrEmpty(el.getId()) ? "sbn-tooltip-"+(tooltipCount++) : el.getId(); 
		optionsMap.put("id", id);
		optionsMap.put("rel", "tooltip");

		if (el.getNodeType() == 1 && ! isPresent(el.getNodeName(), CORE_ATTR_INVALID_ELEMENTS)) {
			// If nodeName is a tag and not in the INVALID_ELEMENTS list then apply the appropriate transformation
			
			applyAttributes(el, optionsMap);

			widget.addAttachHandler( new AttachEvent.Handler() {
				@Override
				public void onAttachOrDetach(AttachEvent event) {
					if (event.isAttached()) {
						util.bindBootstrapTooltip(el.getId());
					}
				}
			});
		}
	}

	/**
	 * Adds a popover to a target widget
	 * 
	 * Same warnings apply as to {@link #addTooltip(SynapseJSNIUtils, Widget, String) addTooltip}
	 */
	public static void addPopover(final SynapseJSNIUtils util, Widget widget, Map<String, String> optionsMap) {
		final Element el = widget.getElement();

		String id = isNullOrEmpty(el.getId()) ? "sbn-popover-"+(popoverCount++) : el.getId();
		optionsMap.put("id", id);
		optionsMap.put("rel", "popover");

		if (el.getNodeType() == 1 && ! isPresent(el.getNodeName(), CORE_ATTR_INVALID_ELEMENTS)) {
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

	private static void applyAttributes(final Element el,
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

}
