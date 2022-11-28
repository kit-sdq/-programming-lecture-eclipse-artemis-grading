/* Licensed under EPL-2.0 2022. */
package edu.kit.kastel.eclipse.common.api;

/**
 * Constant definitions for plug-in preferences
 */
public final class PreferenceConstants {

	public static final String GENERAL_ARTEMIS_URL = "artemisUrl";

	public static final String GENERAL_ADVANCED_ARTEMIS_USER = "artemisUser";
	public static final String GENERAL_ADVANCED_ARTEMIS_PASSWORD = "artemisPassword";
	public static final String GENERAL_ADVANCED_GIT_TOKEN = "gitToken";

	public static final String GRADING_ABSOLUTE_CONFIG_PATH = "absoluteConfigPath";
	public static final String GRADING_VIEW_BUTTONS_IN_COLUMN = "grading_buttons_in_column";
	public static final String GRADING_VIEW_PREFERS_LARGE_PENALTY_TEXT_PATH = "userPreferresLargePenaltyText";
	public static final String GRADING_VIEW_PREFERS_TEXT_WRAPPING_IN_PENALTY_TEXT_PATH = "userPrefersTextWrappingInPenaltyText";

	public static final String GENERAL_OVERRIDE_DEFAULT_PREFERENCES = "override_default_preferences";
	public static final String GENERAL_PREFERRED_LANGUAGE = "preferredLanguageSelector";

	public static final String SEARCH_IN_MISTAKE_MESSAGES = "searchInMistakeMessages";

	public static final String OPEN_FILES_ON_ASSESSMENT_START = "openFilesOnAssessmentStart";
	public static final String OPEN_FILES_ON_ASSESSMENT_START_ALL = "all";
	public static final String OPEN_FILES_ON_ASSESSMENT_START_MAIN = "main";
	public static final String OPEN_FILES_ON_ASSESSMENT_START_NONE = "none";

	private PreferenceConstants() {
		throw new IllegalAccessError();
	}
}
