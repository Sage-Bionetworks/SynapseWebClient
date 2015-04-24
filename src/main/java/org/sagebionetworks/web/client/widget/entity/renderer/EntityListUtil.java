package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
import org.sagebionetworks.web.shared.WikiPageKey;
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
	private final static String NOTE_DELIMITER = ",";	
	private final static String LIST_DELIMITER = ";";

	public interface RowLoadedHandler {
		public void onLoaded(EntityGroupRecordDisplay entityGroupRecordDisplay);
	}

	public static void loadIndividualRowDetails(
			final SynapseClientAsync synapseClient, final SynapseJSNIUtils synapseJSNIUtils,
			final boolean isLoggedIn,
			List<EntityGroupRecord> records, final int rowIndex,
			final RowLoadedHandler handler) throws IllegalArgumentException {
		if(records == null || rowIndex >= records.size()) {
			throw new IllegalArgumentException();
		}
		final EntityGroupRecord record = records.get(rowIndex);
		if(record == null) return;
		final Reference ref = record.getEntityReference();
		if(ref == null) return;
				
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				try {				
					// Old behavior.
					handler.onLoaded(createRecordDisplay(isLoggedIn, bundle, record,
							synapseJSNIUtils, bundle.getEntity().getDescription()));
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				createFailureDisplay(caught, ref, handler);
			}
		};
		int mask = ENTITY;
		if(ref.getTargetVersionNumber() != null) {
			synapseClient.getEntityBundleForVersion(ref.getTargetId(), ref.getTargetVersionNumber(), mask, callback);
		} else {
			// failsafe
			synapseClient.getEntityBundle(ref.getTargetId(), mask, callback);
		}
	}
	
	/**
	 * Gets a plain text description from the wiki associated with the Entity of the given
	 * bundle. Creates a record display with that description.
	 * 
	 * Note: access modifier public for unit test
	 */
	public static void createDisplayWithWikiDescription(
			final SynapseClientAsync synapseClient, final SynapseJSNIUtils synapseJSNIUtils,
			final boolean isLoggedIn, final RowLoadedHandler handler,
			final EntityBundle bundle, final EntityGroupRecord record,
			final Reference ref) {
		String entityId = bundle.getEntity().getId();
		String objectType = ObjectType.ENTITY.toString();
		WikiPageKey key = new WikiPageKey(entityId, objectType, null);
		
		synapseClient.getPlainTextWikiPage(key, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String resultDesc) {
				try {
					handler.onLoaded(createRecordDisplay(isLoggedIn, bundle, record, synapseJSNIUtils, resultDesc));
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					// No wiki found. Put in blank description.
					try {
						handler.onLoaded(createRecordDisplay(isLoggedIn, bundle, record, synapseJSNIUtils, ""));
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}
				} else {
					createFailureDisplay(caught, ref, handler);
				}
			}
		});
	}

	public static String recordsToString(List<EntityGroupRecord> records) {		
		// add record to descriptor
		String recordStr = "";
		if(records == null) return recordStr;
		
		for(EntityGroupRecord record : records) {	
			Reference ref = record.getEntityReference();
			if(ref == null) continue; 		
			if(!recordStr.equals("")) recordStr += LIST_DELIMITER;
			recordStr += DisplayUtils.createEntityVersionString(ref.getTargetId(), ref.getTargetVersionNumber());
			String note = record.getNote();
			if(note != null && !note.equals("")) {
				recordStr += NOTE_DELIMITER + WidgetEncodingUtil.encodeValue(note);
			}

		}
		return recordStr;
	}

	public static List<EntityGroupRecord> parseRecords(String recordStr) {
		List<EntityGroupRecord> records = new ArrayList<EntityGroupRecord>();
		if(recordStr == null || "".equals(recordStr)) return records;
		String[] entries = recordStr.split(LIST_DELIMITER);
		for(String entry : entries) {
			String[] parts = entry.split(NOTE_DELIMITER);
			if(parts.length <= 0) continue;
			EntityGroupRecord record = new EntityGroupRecord();			
			if(parts[0] != null && !"".equals(parts[0])) {
				Reference ref = DisplayUtils.parseEntityVersionString(parts[0]);
				if(ref == null) continue;
				record.setEntityReference(ref);				
			}
			if(parts.length > 1) {
				record.setNote(WidgetEncodingUtil.decodeValue(parts[1]));
			}
			if(record.getEntityReference() != null)
				records.add(record);
		}
		
		return records;
	}

	

	/*
	 * Private methods
	 */
	private static EntityGroupRecordDisplay createRecordDisplay(
			boolean isLoggedIn, EntityBundle bundle,
			EntityGroupRecord record, SynapseJSNIUtils synapseJSNIUtils, 
			String description)
			throws JSONObjectAdapterException {
		Entity referencedEntity = bundle.getEntity();

		String nameLinkUrl;
		if(referencedEntity instanceof Versionable) {
			nameLinkUrl = DisplayUtils.getSynapseHistoryTokenNoHash(referencedEntity.getId(), ((Versionable)referencedEntity).getVersionNumber());
		} else {
			nameLinkUrl = DisplayUtils.getSynapseHistoryTokenNoHash(referencedEntity.getId());
		}

		// download
		String downloadUrl = null;
		if(!isLoggedIn) {				
			if(bundle.getEntity() instanceof FileEntity)
				downloadUrl = "#" + nameLinkUrl;
		}  else if(referencedEntity instanceof FileEntity) {					
			downloadUrl = DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), referencedEntity.getId(), ((FileEntity) referencedEntity).getVersionNumber(), false);
		}
		
		// version
		String version = "N/A";
		if(referencedEntity instanceof Versionable) {
			version = DisplayUtils.getVersionDisplay((Versionable)referencedEntity);
		}							
		
		// desc
		if (description == null) description = "";
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
				referencedEntity.getCreatedBy() == null ? "" : referencedEntity.getCreatedBy(),
				noteSafe);		
	}
	
	private static void createFailureDisplay(Throwable caught, Reference ref, final RowLoadedHandler handler) {
		EntityGroupRecordDisplay errorDisplay = getEmptyDisplay();
		errorDisplay.setEntityId(ref.getTargetId());
		String versionNumber = ref.getTargetVersionNumber() == null ? "" : ref.getTargetVersionNumber().toString();
		errorDisplay.setVersion(SafeHtmlUtils.fromSafeConstant(versionNumber));
		String msg = ref.getTargetId();
		if(ref.getTargetVersionNumber() != null) msg += ", Version " + versionNumber;
		if(caught instanceof UnauthorizedException || caught instanceof ForbiddenException) {
			errorDisplay.setName(SafeHtmlUtils.fromSafeConstant(DisplayConstants.TITLE_UNAUTHORIZED + ": " + msg));
		} else if (caught instanceof NotFoundException) {
			errorDisplay.setName(SafeHtmlUtils.fromSafeConstant(DisplayConstants.NOT_FOUND + ": " + msg));
		} else {
			errorDisplay.setName(SafeHtmlUtils.fromSafeConstant(DisplayConstants.ERROR_LOADING + ": " + msg));
		}
		handler.onLoaded(errorDisplay);
	}
	
	private static EntityGroupRecordDisplay getEmptyDisplay() {
		return new EntityGroupRecordDisplay(
				"",
				SafeHtmlUtils.EMPTY_SAFE_HTML,
				null, null, SafeHtmlUtils.EMPTY_SAFE_HTML, SafeHtmlUtils.EMPTY_SAFE_HTML, null, "", SafeHtmlUtils.EMPTY_SAFE_HTML);
	}
}
