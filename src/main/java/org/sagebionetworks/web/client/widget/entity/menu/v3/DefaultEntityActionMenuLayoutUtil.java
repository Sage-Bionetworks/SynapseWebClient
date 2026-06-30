package org.sagebionetworks.web.client.widget.entity.menu.v3;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.web.client.jsinterop.ActionViewProps;
import org.sagebionetworks.web.client.jsinterop.SxProps;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuLayout;
import org.sagebionetworks.web.client.place.Synapse;

public class DefaultEntityActionMenuLayoutUtil {

  private DefaultEntityActionMenuLayoutUtil() {
    super();
  }

  private static final String TOOLS_SUFFIX = " Tools";
  private static final String ICON_COLOR = "#1C1B1F";

  static SxProps entityMenuButtonSx = SxProps
    .create()
    .setBorderColor("#9EAAB7")
    .setColor("#4D535A");

  static SxProps entityMenuEndIconSx = SxProps.create().setColor(ICON_COLOR);

  static SxProps deleteTextStyle = SxProps
    .create()
    .setColor("error.main")
    .setFontWeight(700L);

  public static EntityActionMenuLayout getLayout(
    Synapse.EntityArea entityArea
  ) {
    EntityActionMenuLayout layout = EntityActionMenuLayout.create();
    layout.setMenuButtonSx(entityMenuButtonSx);
    layout.setPrimaryMenuEndIconSx(entityMenuEndIconSx);
    layout.setPrimaryMenuEndIcon("verticalEllipsis");

    switch (entityArea) {
      case WIKI:
        layout.setPrimaryMenuText("Tools");
        layout.setButtonActions(
          Arrays.asList(
            ActionViewProps
              .create(Action.EDIT_WIKI_PAGE)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.VIEW_WIKI_SOURCE)
              .setVariant("outlined")
          )
        );
        layout.setPrimaryMenuActions(
          Arrays.asList(
            Arrays.asList(
              ActionViewProps.create(Action.ADD_WIKI_SUBPAGE),
              ActionViewProps.create(Action.REORDER_WIKI_SUBPAGES)
            ),
            Collections.singletonList(
              ActionViewProps.create(
                Action.DELETE_WIKI_PAGE,
                null,
                deleteTextStyle,
                null
              )
            )
          )
        );
        break;
      case FILES:
        layout.setButtonActions(
          Arrays.asList(
            ActionViewProps.create(Action.UPLOAD_FILE).setVariant("outlined"),
            ActionViewProps.create(Action.CREATE_FOLDER).setVariant("outlined")
          )
        );
        layout.setPrimaryMenuActions(Collections.emptyList());
        layout.setDownloadMenuActions(
          Collections.singletonList(
            Arrays.asList(
              ActionViewProps.create(Action.ADD_TO_DOWNLOAD_CART),
              ActionViewProps.create(Action.SHOW_PROGRAMMATIC_OPTIONS)
            )
          )
        );
        break;
      case DATASETS:
        layout.setPrimaryMenuText("New...");
        layout.setPrimaryMenuEndIcon("expandMore");
        layout.setButtonActions(Collections.emptyList());
        layout.setPrimaryMenuActions(
          Collections.singletonList(
            Arrays.asList(
              ActionViewProps.create(Action.ADD_DATASET),
              ActionViewProps.create(Action.ADD_DATASET_COLLECTION)
            )
          )
        );
        break;
      case TABLES:
        layout.setPrimaryMenuText("New...");
        layout.setPrimaryMenuEndIcon("expandMore");
        layout.setButtonActions(
          Collections.singletonList(
            ActionViewProps.create(Action.UPLOAD_TABLE).setVariant("outlined")
          )
        );
        layout.setPrimaryMenuActions(
          Collections.singletonList(
            Arrays.asList(ActionViewProps.create(Action.ADD_TABLE))
          )
        );
        break;
      case CHALLENGE:
        layout.setPrimaryMenuText(TOOLS_SUFFIX);
        layout.setButtonActions(Collections.emptyList());
        layout.setPrimaryMenuActions(
          Collections.singletonList(
            Arrays.asList(
              ActionViewProps.create(Action.ADD_EVALUATION_QUEUE),
              ActionViewProps.create(Action.DELETE_CHALLENGE)
            )
          )
        );
        break;
      case DISCUSSION:
        layout.setPrimaryMenuText(TOOLS_SUFFIX);
        layout.setButtonActions(Collections.emptyList());
        layout.setPrimaryMenuActions(
          Collections.singletonList(
            Collections.singletonList(
              ActionViewProps.create(Action.SHOW_DELETED_THREADS)
            )
          )
        );

        break;
      case DOCKER:
        layout.setPrimaryMenuText("Docker Repository Tools");
        layout.setButtonActions(Collections.emptyList());
        layout.setPrimaryMenuActions(
          Collections.singletonList(
            Collections.singletonList(
              ActionViewProps.create(Action.CREATE_EXTERNAL_DOCKER_REPO)
            )
          )
        );
        break;
    }
    return layout;
  }

  public static EntityActionMenuLayout getLayout(EntityType entityType) {
    EntityActionMenuLayout layout = EntityActionMenuLayout.create();
    layout.setPrimaryMenuEndIcon("verticalEllipsis");
    layout.setMenuButtonSx(entityMenuButtonSx);
    layout.setPrimaryMenuEndIconSx(entityMenuEndIconSx);

    SxProps reportViolationIconStyle = SxProps.create().setColor("error.main");
    List<ActionViewProps> reportViolationMenuGroup = Collections.singletonList(
      ActionViewProps.create(
        Action.REPORT_VIOLATION,
        "flag",
        null,
        reportViolationIconStyle
      )
    );

    List<ActionViewProps> actMenuGroup = Arrays.asList(
      ActionViewProps.create(Action.APPROVE_USER_ACCESS),
      ActionViewProps.create(Action.MANAGE_ACCESS_REQUIREMENTS)
    );

    switch (entityType) {
      case project:
        layout.setPrimaryMenuEndIcon("expandMore");
        layout.setMenuButtonSx(null);
        layout.setPrimaryMenuEndIconSx(null);
        layout.setPrimaryMenuText(
          EntityTypeUtils.getDisplayName(entityType) + TOOLS_SUFFIX
        );
        layout.setPrimaryMenuActions(
          Arrays.asList(
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_SHARING_SETTINGS),
              ActionViewProps.create(Action.EDIT_PROJECT_METADATA),
              ActionViewProps.create(Action.SHOW_ANNOTATIONS),
              ActionViewProps.create(Action.SHOW_PROJECT_STATS),
              ActionViewProps.create(Action.CHANGE_STORAGE_LOCATION),
              ActionViewProps.create(Action.CREATE_CHALLENGE)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.CREATE_OR_UPDATE_DOI),
              ActionViewProps.create(Action.CREATE_LINK),
              ActionViewProps.create(Action.SHARE_THIS_PAGE)
            ),
            Collections.singletonList(
              ActionViewProps.create(
                Action.DELETE_ENTITY,
                null,
                deleteTextStyle,
                null
              )
            ),
            reportViolationMenuGroup,
            actMenuGroup
          )
        );
        break;
      case folder:
        layout.setPrimaryMenuText(TOOLS_SUFFIX);
        layout.setButtonActions(
          Arrays.asList(
            ActionViewProps
              .create(Action.SHARE_THIS_PAGE, "share")
              .setVariant("text")
              .setIconSx(SxProps.create().setColor(ICON_COLOR)),
            ActionViewProps.create(Action.CREATE_FOLDER).setVariant("outlined"),
            ActionViewProps.create(Action.UPLOAD_FILE).setVariant("outlined"),
            ActionViewProps
              .create(Action.SHOW_ANNOTATIONS)
              .setVariant("outlined")
          )
        );
        layout.setDownloadMenuActions(
          Collections.singletonList(
            Arrays.asList(
              ActionViewProps.create(Action.ADD_TO_DOWNLOAD_CART),
              ActionViewProps.create(Action.SHOW_PROGRAMMATIC_OPTIONS)
            )
          )
        );
        layout.setPrimaryMenuActions(
          Arrays.asList(
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_SHARING_SETTINGS),
              ActionViewProps.create(Action.CHANGE_ENTITY_NAME),
              ActionViewProps.create(Action.EDIT_PROVENANCE),
              ActionViewProps.create(Action.CHANGE_STORAGE_LOCATION)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_WIKI_SOURCE),
              ActionViewProps.create(Action.EDIT_WIKI_PAGE)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.CREATE_OR_UPDATE_DOI),
              ActionViewProps.create(Action.CREATE_LINK)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.MOVE_ENTITY),
              ActionViewProps.create(Action.CHANGE_ENTITY_NAME),
              ActionViewProps.create(
                Action.DELETE_ENTITY,
                null,
                deleteTextStyle,
                null
              )
            ),
            reportViolationMenuGroup,
            actMenuGroup
          )
        );
        break;
      case file:
      case recordset:
        layout.setPrimaryMenuText(TOOLS_SUFFIX);
        layout.setButtonActions(
          Arrays.asList(
            ActionViewProps
              .create(Action.SHARE_THIS_PAGE, "share")
              .setVariant("text")
              .setIconSx(SxProps.create().setColor(ICON_COLOR)),
            ActionViewProps
              .create(Action.UPLOAD_NEW_FILE)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.SHOW_ANNOTATIONS)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.CREATE_NEW_GRID)
              .setVariant("outlined")
          )
        );
        layout.setDownloadMenuActions(
          Arrays.asList(
            Arrays.asList(
              ActionViewProps.create(Action.DOWNLOAD_FILE),
              ActionViewProps.create(
                Action.OPEN_EXTERNAL_FILE,
                "openInNewWindow"
              )
            ),
            Arrays.asList(
              ActionViewProps.create(Action.ADD_TO_DOWNLOAD_CART),
              ActionViewProps.create(Action.SHOW_PROGRAMMATIC_OPTIONS)
            )
          )
        );
        layout.setPrimaryMenuActions(
          Arrays.asList(
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_SHARING_SETTINGS),
              ActionViewProps.create(Action.SHOW_VERSION_HISTORY),
              ActionViewProps.create(Action.EDIT_FILE_METADATA),
              ActionViewProps.create(Action.EDIT_PROVENANCE),
              ActionViewProps.create(Action.SUBMIT_TO_CHALLENGE)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_WIKI_SOURCE),
              ActionViewProps.create(Action.EDIT_WIKI_PAGE)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.CREATE_OR_UPDATE_DOI),
              ActionViewProps.create(Action.CREATE_LINK)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.MOVE_ENTITY),
              ActionViewProps.create(
                Action.DELETE_ENTITY,
                null,
                deleteTextStyle,
                null
              )
            ),
            reportViolationMenuGroup,
            actMenuGroup
          )
        );
        break;
      case table:
        layout.setPrimaryMenuText(TOOLS_SUFFIX);
        layout.setButtonActions(
          Arrays.asList(
            ActionViewProps
              .create(Action.SHARE_THIS_PAGE, "share")
              .setVariant("text")
              .setIconSx(SxProps.create().setColor(ICON_COLOR)),
            ActionViewProps
              .create(Action.EDIT_TABLE_DATA)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.SHOW_ANNOTATIONS)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.SHOW_TABLE_SCHEMA)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.CREATE_NEW_GRID)
              .setVariant("outlined")
          )
        );
        // Note that download actions for tables are currently inlined in the QueryWrapperPlotNav
        layout.setPrimaryMenuActions(
          Arrays.asList(
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_SHARING_SETTINGS),
              ActionViewProps.create(Action.EDIT_PROVENANCE),
              ActionViewProps.create(Action.UPLOAD_TABLE_DATA),
              ActionViewProps.create(Action.TOGGLE_FULL_TEXT_SEARCH)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.CREATE_TABLE_VERSION),
              ActionViewProps.create(Action.SHOW_VERSION_HISTORY)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_WIKI_SOURCE),
              ActionViewProps.create(Action.EDIT_WIKI_PAGE)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.CREATE_OR_UPDATE_DOI),
              ActionViewProps.create(Action.CREATE_LINK)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.MOVE_ENTITY),
              ActionViewProps.create(Action.CHANGE_ENTITY_NAME),
              ActionViewProps.create(
                Action.DELETE_ENTITY,
                null,
                deleteTextStyle,
                null
              )
            ),
            reportViolationMenuGroup,
            actMenuGroup
          )
        );
        break;
      case link:
        // Links don't have an entity page.
        break;
      case entityview:
      case submissionview:
        layout.setPrimaryMenuText(TOOLS_SUFFIX);
        layout.setButtonActions(
          Arrays.asList(
            ActionViewProps
              .create(Action.SHARE_THIS_PAGE, "share")
              .setVariant("text")
              .setIconSx(SxProps.create().setColor(ICON_COLOR)),
            ActionViewProps
              .create(Action.EDIT_TABLE_DATA)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.SHOW_ANNOTATIONS)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.SHOW_TABLE_SCHEMA)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.CREATE_NEW_GRID)
              .setVariant("outlined")
          )
        );
        // Note that download actions for tables are currently inlined in the QueryWrapperPlotNav
        layout.setPrimaryMenuActions(
          Arrays.asList(
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_SHARING_SETTINGS),
              ActionViewProps.create(Action.EDIT_PROVENANCE),
              ActionViewProps.create(Action.UPLOAD_TABLE_DATA),
              ActionViewProps.create(Action.TOGGLE_FULL_TEXT_SEARCH)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.CREATE_TABLE_VERSION),
              ActionViewProps.create(Action.SHOW_VERSION_HISTORY),
              ActionViewProps.create(Action.SHOW_VIEW_SCOPE)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_WIKI_SOURCE),
              ActionViewProps.create(Action.EDIT_WIKI_PAGE)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.CREATE_OR_UPDATE_DOI),
              ActionViewProps.create(Action.CREATE_LINK)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.MOVE_ENTITY),
              ActionViewProps.create(Action.CHANGE_ENTITY_NAME),
              ActionViewProps.create(
                Action.DELETE_ENTITY,
                null,
                deleteTextStyle,
                null
              )
            ),
            reportViolationMenuGroup,
            actMenuGroup
          )
        );
        break;
      case dockerrepo:
        layout.setPrimaryMenuText(TOOLS_SUFFIX);
        layout.setButtonActions(
          Arrays.asList(
            ActionViewProps
              .create(Action.SHARE_THIS_PAGE, "share")
              .setVariant("text")
              .setIconSx(SxProps.create().setColor(ICON_COLOR)),
            ActionViewProps
              .create(Action.SHOW_ANNOTATIONS)
              .setVariant("outlined")
          )
        );
        layout.setPrimaryMenuActions(
          Arrays.asList(
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_SHARING_SETTINGS),
              ActionViewProps.create(Action.EDIT_PROVENANCE),
              ActionViewProps.create(Action.SUBMIT_TO_CHALLENGE),
              ActionViewProps.create(Action.ADD_DOCKER_COMMIT)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_WIKI_SOURCE),
              ActionViewProps.create(Action.EDIT_WIKI_PAGE)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.CREATE_OR_UPDATE_DOI),
              ActionViewProps.create(Action.CREATE_LINK)
            ),
            Collections.singletonList(
              ActionViewProps.create(
                Action.DELETE_ENTITY,
                null,
                deleteTextStyle,
                null
              )
            ),
            reportViolationMenuGroup,
            actMenuGroup
          )
        );
        break;
      // Note that download actions for tables are currently inlined in the QueryWrapperPlotNav
      case dataset:
      case datasetcollection:
        layout.setPrimaryMenuText(TOOLS_SUFFIX);
        layout.setButtonActions(
          Arrays.asList(
            ActionViewProps
              .create(Action.SHARE_THIS_PAGE, "share")
              .setVariant("text")
              .setIconSx(SxProps.create().setColor(ICON_COLOR)),
            ActionViewProps
              .create(Action.EDIT_ENTITYREF_COLLECTION_ITEMS)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.CREATE_TABLE_VERSION)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.SHOW_ANNOTATIONS)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.SHOW_TABLE_SCHEMA)
              .setVariant("outlined")
          )
        );
        // Note that download actions for tables are currently inlined in the QueryWrapperPlotNav
        layout.setPrimaryMenuActions(
          Arrays.asList(
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_SHARING_SETTINGS),
              ActionViewProps.create(Action.SHOW_VERSION_HISTORY),
              ActionViewProps.create(Action.EDIT_PROVENANCE),
              ActionViewProps.create(Action.TOGGLE_FULL_TEXT_SEARCH)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_WIKI_SOURCE),
              ActionViewProps.create(Action.EDIT_WIKI_PAGE)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.CREATE_OR_UPDATE_DOI),
              ActionViewProps.create(Action.CREATE_LINK)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.MOVE_ENTITY),
              ActionViewProps.create(Action.CHANGE_ENTITY_NAME),
              ActionViewProps.create(
                Action.DELETE_ENTITY,
                null,
                deleteTextStyle,
                null
              )
            ),
            reportViolationMenuGroup,
            actMenuGroup
          )
        );
        break;
      case materializedview:
      case virtualtable:
        layout.setPrimaryMenuText(TOOLS_SUFFIX);
        layout.setButtonActions(
          Arrays.asList(
            ActionViewProps
              .create(Action.SHARE_THIS_PAGE, "share")
              .setVariant("text")
              .setIconSx(SxProps.create().setColor(ICON_COLOR)),
            ActionViewProps
              .create(Action.EDIT_DEFINING_SQL)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.VIEW_DEFINING_SQL)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.SHOW_ANNOTATIONS)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.SHOW_TABLE_SCHEMA)
              .setVariant("outlined")
          )
        );
        // Note that download actions for tables are currently inlined in the QueryWrapperPlotNav
        layout.setPrimaryMenuActions(
          Arrays.asList(
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_SHARING_SETTINGS),
              ActionViewProps.create(Action.EDIT_PROVENANCE),
              ActionViewProps.create(Action.TOGGLE_FULL_TEXT_SEARCH)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_WIKI_SOURCE),
              ActionViewProps.create(Action.EDIT_WIKI_PAGE)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.CREATE_OR_UPDATE_DOI),
              ActionViewProps.create(Action.CREATE_LINK)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.MOVE_ENTITY),
              ActionViewProps.create(Action.CHANGE_ENTITY_NAME),
              ActionViewProps.create(
                Action.DELETE_ENTITY,
                null,
                deleteTextStyle,
                null
              )
            ),
            reportViolationMenuGroup,
            actMenuGroup
          )
        );
        break;
      case searchindex:
        layout.setPrimaryMenuText(TOOLS_SUFFIX);
        layout.setButtonActions(
          Arrays.asList(
            ActionViewProps
              .create(Action.SHARE_THIS_PAGE, "share")
              .setVariant("text")
              .setIconSx(SxProps.create().setColor(ICON_COLOR)),
            ActionViewProps
              .create(Action.EDIT_DEFINING_SQL)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.VIEW_DEFINING_SQL)
              .setVariant("outlined"),
            ActionViewProps
              .create(Action.SHOW_ANNOTATIONS)
              .setVariant("outlined")
          )
        );
        layout.setPrimaryMenuActions(
          Arrays.asList(
            Arrays.asList(
              ActionViewProps.create(Action.VIEW_SHARING_SETTINGS),
              ActionViewProps.create(Action.EDIT_PROVENANCE)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.CREATE_OR_UPDATE_DOI),
              ActionViewProps.create(Action.CREATE_LINK)
            ),
            Arrays.asList(
              ActionViewProps.create(Action.MOVE_ENTITY),
              ActionViewProps.create(Action.CHANGE_ENTITY_NAME),
              ActionViewProps.create(
                Action.DELETE_ENTITY,
                null,
                deleteTextStyle,
                null
              )
            ),
            reportViolationMenuGroup,
            actMenuGroup
          )
        );
        break;
    }
    return layout;
  }
}
