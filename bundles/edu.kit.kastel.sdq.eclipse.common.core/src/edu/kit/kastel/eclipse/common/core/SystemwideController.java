/* Licensed under EPL-2.0 2022. */
package edu.kit.kastel.eclipse.common.core;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.kit.kastel.eclipse.common.api.artemis.IProjectFileNamingStrategy;
import edu.kit.kastel.eclipse.common.api.artemis.mapping.ICourse;
import edu.kit.kastel.eclipse.common.api.artemis.mapping.IExercise;
import edu.kit.kastel.eclipse.common.api.controller.AbstractController;
import edu.kit.kastel.eclipse.common.api.controller.IArtemisController;
import edu.kit.kastel.eclipse.common.api.controller.IExerciseArtemisController;
import edu.kit.kastel.eclipse.common.api.controller.ISystemwideController;
import edu.kit.kastel.eclipse.common.core.artemis.naming.ProjectFileNamingStrategies;

public abstract class SystemwideController extends AbstractController implements ISystemwideController {
	protected ICourse course;
	protected IExercise exercise;
	protected IPreferenceStore preferenceStore;
	protected IProjectFileNamingStrategy projectFileNamingStrategy;
	protected IExerciseArtemisController exerciseController;

	protected SystemwideController(IPreferenceStore preferenceStore) {
		this.projectFileNamingStrategy = ProjectFileNamingStrategies.DEFAULT.get();
		var loginController = createController(preferenceStore);
		exerciseController = new ExerciseArtemisController(loginController.getUserLogin(), preferenceStore);
	}

	protected abstract IArtemisController createController(IPreferenceStore preferenceStore);

	protected abstract void refreshArtemisController(IPreferenceStore preferenceStore);

	protected boolean nullCheckMembersAndNotify(boolean checkCourseId, boolean checkExerciseId) {
		String alert = "[";
		boolean somethingNull = false;
		if (checkCourseId && this.course == null) {
			alert += "Course is not set ";
			somethingNull = true;
		}
		if (checkExerciseId && this.exercise == null) {
			alert += "Exercise is not set ";
			somethingNull = true;
		}
		if (somethingNull) {
			alert += "]";
			this.warn(alert);
		}
		return somethingNull;
	}
}
