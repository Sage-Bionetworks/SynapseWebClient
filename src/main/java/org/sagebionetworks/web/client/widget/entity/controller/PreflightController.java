package org.sagebionetworks.web.client.widget.entity.controller;


/**
 * Controller is an abstraction for all of preflight checks a user must pass before doing certain operations.
 * This includes access restrictions and user certification.
 * 
 * @author jhill
 *
 */
public interface PreflightController extends UploadController, DownloadController, EntityCRUDController {
		

}
