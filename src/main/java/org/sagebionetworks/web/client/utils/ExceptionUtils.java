package org.sagebionetworks.web.client.utils;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtilsGWT;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.widget.Alert;
import org.sagebionetworks.web.client.widget.Alert.AlertType;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

public class ExceptionUtils {
	
	private static final String ERROR_OBJ_REASON_KEY = "reason";
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
	
	/**
	 * The preferred method for creating new global alerts.  For a
	 * default 'info' type alert, you can also use {@link showInfo(String, String)}
	 * @param alert
	 */
	public static void displayGlobalAlert(Alert alert) {
		Element container = DOM.getElementById(DisplayUtils.ALERT_CONTAINER_ID);
		DOM.insertChild(container, alert.getElement(), 0);
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
}
