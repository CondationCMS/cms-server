package com.condation.cms.api.feature.features;

import com.condation.cms.api.annotations.FeatureScope;
import com.condation.cms.api.feature.Feature;
import com.condation.cms.api.workflow.Workflow;

/**
 *
 * @author thorstenmarx
 */
@FeatureScope({FeatureScope.Scope.GLOBAL, FeatureScope.Scope.MODULE, FeatureScope.Scope.REQUEST})
public record WorkflowFeature(Workflow workflow) implements Feature {
	
}
