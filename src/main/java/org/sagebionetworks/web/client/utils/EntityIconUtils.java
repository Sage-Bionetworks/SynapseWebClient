package org.sagebionetworks.web.client.utils;

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
import org.sagebionetworks.repo.model.Step;
import org.sagebionetworks.repo.model.Study;
import org.sagebionetworks.repo.model.Summary;
import org.sagebionetworks.web.client.DisplayUtils.IconSize;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.shared.EntityType;

import com.google.gwt.resources.client.ImageResource;

/**
 * Extracted from DisplayUtils
 * 
 *
 */
public class EntityIconUtils {

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
		} else if(FileEntity.class.getName().equals(className)) {
			// File
			if(iconSize == IconSize.PX16) icon = iconsImageBundle.synapseFile16();
			else if (iconSize == IconSize.PX24) icon = iconsImageBundle.synapseFile24();			
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
	
	public static ImageResource getSynapseIconForEntityType(EntityType type, IconSize iconSize, IconsImageBundle iconsImageBundle) {
		String className = type == null ? null : type.getClassName();		
		return getSynapseIconForEntityClassName(className, iconSize, iconsImageBundle);
	}

	public static ImageResource getSynapseIconForEntity(Entity entity, IconSize iconSize, IconsImageBundle iconsImageBundle) {
		String className = entity == null ? null : entity.getClass().getName();
		return getSynapseIconForEntityClassName(className, iconSize, iconsImageBundle);
	}
}
