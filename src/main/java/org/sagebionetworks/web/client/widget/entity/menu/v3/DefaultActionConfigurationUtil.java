package org.sagebionetworks.web.client.widget.entity.menu.v3;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.ActionConfiguration;

public class DefaultActionConfigurationUtil {

  private DefaultActionConfigurationUtil() {
    super();
  }

  private static Map<Action, ActionConfiguration> createMap() {
    /*
     * Instantiate the configuration with every action.
     * EntityActionController and other widgets will toggle the configuration of individual actions as needed.
     */
    List<ActionConfiguration> configurations = Arrays.asList(
      ActionConfiguration.create(
        Action.EDIT_ENTITYREF_COLLECTION_ITEMS,
        "Edit Items"
      ),
      ActionConfiguration.create(Action.VIEW_SHARING_SETTINGS, "Share"),
      ActionConfiguration.create(
        Action.EDIT_PROJECT_METADATA,
        "Edit Project Metadata"
      ),
      ActionConfiguration.create(
        Action.SHOW_PROJECT_STATS,
        "Show Project Statistics"
      ),
      ActionConfiguration.create(
        Action.EDIT_FILE_METADATA,
        "Edit File Metadata"
      ),
      ActionConfiguration.create(Action.CHANGE_ENTITY_NAME, "Rename"),
      ActionConfiguration.create(Action.EDIT_DEFINING_SQL, "Edit Defining SQL"),
      ActionConfiguration.create(Action.VIEW_DEFINING_SQL, "View Defining SQL"),
      ActionConfiguration.create(Action.SHOW_ANNOTATIONS, "Annotations"),
      ActionConfiguration.create(
        Action.UPLOAD_FILE,
        "Upload or Link to a File"
      ),
      ActionConfiguration.create(Action.CREATE_FOLDER, "Add New Folder"),
      ActionConfiguration.create(Action.SHOW_TABLE_SCHEMA, "Schema"),
      ActionConfiguration.create(Action.SHOW_VIEW_SCOPE, "Scope"),
      ActionConfiguration.create(
        Action.CHANGE_STORAGE_LOCATION,
        "Change Storage Location"
      ),
      ActionConfiguration.create(
        Action.UPLOAD_NEW_FILE,
        "Upload a New Version of File"
      ),
      ActionConfiguration.create(
        Action.CREATE_TABLE_VERSION,
        "Create a New Table/View Version"
      ),
      ActionConfiguration.create(
        Action.SHOW_VERSION_HISTORY,
        "Version History"
      ),
      ActionConfiguration.create(
        Action.PROJECT_DISPLAY,
        "Project Display Settings"
      ),
      ActionConfiguration.create(
        Action.UPLOAD_TABLE_DATA,
        "Upload Data to Table"
      ),
      ActionConfiguration.create(
        Action.TOGGLE_FULL_TEXT_SEARCH,
        "Enable Full Text Search"
      ),
      ActionConfiguration.create(Action.EDIT_WIKI_PAGE, "Edit Wiki Page"),
      ActionConfiguration.create(Action.VIEW_WIKI_SOURCE, "View Wiki Source"),
      ActionConfiguration.create(Action.ADD_WIKI_SUBPAGE, "Add Wiki Subpage"),
      ActionConfiguration.create(
        Action.REORDER_WIKI_SUBPAGES,
        "Edit Wiki Page Order"
      ),
      ActionConfiguration.create(Action.DELETE_WIKI_PAGE, "Delete Wiki Page"),
      ActionConfiguration.create(Action.CREATE_CHALLENGE, "Run Challenge"),
      ActionConfiguration.create(Action.DELETE_CHALLENGE, "Delete Challenge"),
      ActionConfiguration.create(
        Action.ADD_EVALUATION_QUEUE,
        "Add Evaluation Queue"
      ),
      ActionConfiguration.create(Action.EDIT_PROVENANCE, "Edit Provenance"),
      ActionConfiguration.create(
        Action.SUBMIT_TO_CHALLENGE,
        "Submit to Challenge"
      ),
      ActionConfiguration.create(Action.MOVE_ENTITY, "Move"),
      ActionConfiguration.create(
        Action.CREATE_OR_UPDATE_DOI,
        "Create or Update DOI"
      ),
      ActionConfiguration.create(Action.CREATE_LINK, "Save Link"),
      ActionConfiguration.create(Action.DELETE_ENTITY, "Delete"),
      ActionConfiguration.create(Action.UPLOAD_TABLE, "Upload a Table"),
      ActionConfiguration.create(Action.ADD_DATASET, "Add Dataset"),
      ActionConfiguration.create(
        Action.ADD_DATASET_COLLECTION,
        "Add Dataset Collection"
      ),
      ActionConfiguration.create(Action.ADD_TABLE, "Add Table"),
      ActionConfiguration.create(Action.ADD_FILE_VIEW, "Add File View"),
      ActionConfiguration.create(Action.ADD_PROJECT_VIEW, "Add Project View"),
      ActionConfiguration.create(
        Action.ADD_SUBMISSION_VIEW,
        "Add Submission View"
      ),
      ActionConfiguration.create(
        Action.ADD_MATERIALIZED_VIEW,
        "Add Materialized View"
      ),
      ActionConfiguration.create(Action.ADD_VIRTUAL_TABLE, "Add Virtual Table"),
      ActionConfiguration.create(Action.FOLLOW, "Follow"),
      ActionConfiguration.create(Action.CREATE_THREAD, "Create New Thread"),
      ActionConfiguration.create(
        Action.SHOW_DELETED_THREADS,
        "Show Deleted Threads"
      ),
      ActionConfiguration.create(Action.EDIT_THREAD, "Edit Thread"),
      ActionConfiguration.create(Action.PIN_THREAD, "Pin Thread"),
      ActionConfiguration.create(Action.DELETE_THREAD, "Delete Thread"),
      ActionConfiguration.create(Action.RESTORE_THREAD, "Restore Thread"),
      ActionConfiguration.create(
        Action.CREATE_EXTERNAL_DOCKER_REPO,
        "Add External Repository"
      ),
      ActionConfiguration.create(
        Action.APPROVE_USER_ACCESS,
        "Change User Access (ACT)"
      ),
      ActionConfiguration.create(
        Action.MANAGE_ACCESS_REQUIREMENTS,
        "Manage Access Requirements (ACT)"
      ),
      ActionConfiguration.create(
        Action.EDIT_TABLE_DATA,
        "Bulk Edit Table Cell Values"
      ),
      ActionConfiguration.create(
        Action.SHOW_PROGRAMMATIC_OPTIONS,
        "Programmatic Options"
      ),
      ActionConfiguration.create(
        Action.ADD_TO_DOWNLOAD_CART,
        "Add to Download Cart"
      ),
      ActionConfiguration.create(Action.DOWNLOAD_FILE, "Download File"),
      ActionConfiguration.create(Action.REPORT_VIOLATION, "Report Violation")
    );
    Map<Action, ActionConfiguration> configurationMap = new EnumMap<>(
      Action.class
    );
    configurations.forEach(config ->
      configurationMap.put(config.getAction(), config)
    );
    return configurationMap;
  }

  public static Map<Action, ActionConfiguration> getDefaultActionConfiguration() {
    return createMap();
  }
}
