package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.widget.EntityListWidgetDescriptor;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This utility class holds common presenter logic for the EntityListWidget and EntityListConfigEditor
 * @author dburdick
 *
 */
public class EntityListUtil {
	
	private static final int MAX_DESCRIPTION_CHARS = 165;

	public interface RowLoadedHandler {
		public void onLoaded(EntityGroupRecordDisplay entityGroupRecordDisplay);
	}
	
	public static void loadIndividualRowDetails(
			SynapseClientAsync synapseClient,
			final NodeModelCreator nodeModelCreator, final boolean isLoggedIn,
			EntityListWidgetDescriptor descriptor, final int rowIndex,
			final RowLoadedHandler handler) throws IllegalArgumentException {
		if(descriptor == null || descriptor.getRecords() == null || rowIndex >= descriptor.getRecords().size()) {
			throw new IllegalArgumentException();
		}
		final EntityGroupRecord record = descriptor.getRecords().get(rowIndex);
		if(record == null) return;
		final Reference ref = record.getEntityReference();
		if(ref == null) return;
				
		AsyncCallback<EntityWrapper> callback = new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {								
					handler.onLoaded(createRecordDisplay(nodeModelCreator, isLoggedIn, record, result));									
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				EntityGroupRecordDisplay errorDisplay = getEmptyDisplay();
				errorDisplay.setEntityId(ref.getTargetId());
				errorDisplay.setVersion(SafeHtmlUtils.fromSafeConstant(ref.getTargetVersionNumber().toString()));
				String msg = ref.getTargetId();
				if(ref.getTargetVersionNumber() != null) msg += ", Version " + ref.getTargetVersionNumber();
				if(caught instanceof UnauthorizedException || caught instanceof ForbiddenException) {
					errorDisplay.setName(SafeHtmlUtils.fromSafeConstant(DisplayConstants.TITLE_UNAUTHORIZED + ": " + msg));
				} else if (caught instanceof NotFoundException) {
					errorDisplay.setName(SafeHtmlUtils.fromSafeConstant(DisplayConstants.NOT_FOUND + ": " + msg));
				} else {
					errorDisplay.setName(SafeHtmlUtils.fromSafeConstant(DisplayConstants.ERROR_LOADING + ": " + msg));
				}
				handler.onLoaded(errorDisplay);
			}
		};
		if(ref.getTargetVersionNumber() != null) {
			synapseClient.getEntityForVersion(ref.getTargetId(), ref.getTargetVersionNumber(), callback);
		} else {
			// failsafe
			synapseClient.getEntity(ref.getTargetId(), callback);
		}
	}
		

	/*
	 * Private methods
	 */
	private static EntityGroupRecordDisplay createRecordDisplay(
			NodeModelCreator nodeModelCreator, boolean isLoggedIn,
			EntityGroupRecord record, EntityWrapper result)
			throws JSONObjectAdapterException {
		Entity referencedEntity = nodeModelCreator.createEntity(result);
				
		String nameLinkUrl;
		if(referencedEntity instanceof Versionable) {
			nameLinkUrl = DisplayUtils.getSynapseHistoryTokenNoHash(referencedEntity.getId(), ((Versionable)referencedEntity).getVersionNumber());
		} else {
			nameLinkUrl = DisplayUtils.getSynapseHistoryTokenNoHash(referencedEntity.getId());
		}

		// download
		String downloadUrl = null;
		if(referencedEntity instanceof Locationable) {
			List<LocationData> locations = ((Locationable) referencedEntity).getLocations();
			if(locations != null && locations.size() > 0) {
				downloadUrl = locations.get(0).getPath();
			} else if(!isLoggedIn) {				
				downloadUrl = "#" + nameLinkUrl;
			}
		}
		
		// version
		String version = "N/A";
		if(referencedEntity instanceof Versionable) {
			version = DisplayUtils.getVersionDisplay((Versionable)referencedEntity);
		}							
		
		// desc
		String description = referencedEntity.getDescription() == null ? "" : referencedEntity.getDescription();
		description = description.replaceAll("\\n", " "); // keep to 3 lines by removing new lines
		if(description.length() > MAX_DESCRIPTION_CHARS) 
			description = description.substring(0, MAX_DESCRIPTION_CHARS) + " ...";
		SafeHtml descSafe =  new SafeHtmlBuilder().appendEscapedLines(description).toSafeHtml();  
		
		// note
		SafeHtml noteSafe = record.getNote() == null ? 
				SafeHtmlUtils.fromSafeConstant("")
				: new SafeHtmlBuilder().appendEscapedLines(record.getNote()).toSafeHtml();
				
		return new EntityGroupRecordDisplay(
				referencedEntity.getId(),
				SafeHtmlUtils.fromString(referencedEntity.getName()),
				nameLinkUrl,
				downloadUrl, descSafe,
				SafeHtmlUtils.fromString(version),
				referencedEntity.getModifiedOn(),
				referencedEntity.getCreatedBy() == null ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromString(referencedEntity.getCreatedBy()),
				noteSafe);		
	}

	
	private static EntityGroupRecordDisplay getEmptyDisplay() {
		return new EntityGroupRecordDisplay(
				"",
				SafeHtmlUtils.EMPTY_SAFE_HTML,
				null, null, SafeHtmlUtils.EMPTY_SAFE_HTML, SafeHtmlUtils.EMPTY_SAFE_HTML, null, SafeHtmlUtils.EMPTY_SAFE_HTML, SafeHtmlUtils.EMPTY_SAFE_HTML);
	}


}
