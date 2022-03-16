package edu.kit.kastel.eclipse.student.view.controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.eclipse.common.view.controllers.AbstractArtemisViewController;
import edu.kit.kastel.eclipse.student.view.activator.Activator;
import edu.kit.kastel.sdq.eclipse.grading.api.ArtemisClientException;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.Feedback;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.IExam;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.IExercise;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.IStudentExam;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.ResultsDTO;
import edu.kit.kastel.sdq.eclipse.grading.api.client.websocket.WebsocketCallback;
import edu.kit.kastel.sdq.eclipse.grading.api.controller.IStudentSystemwideController;
import edu.kit.kastel.sdq.eclipse.grading.api.controller.ISystemwideController;
import edu.kit.kastel.sdq.eclipse.grading.api.model.IAnnotation;

public class StudentViewController extends AbstractArtemisViewController {
	private IStudentSystemwideController systemwideController;

	public StudentViewController() {
		super();
		Activator.getDefault().createSystemWideController();
		systemwideController = Activator.getDefault().getSystemwideController();
		this.initializeControllersAndObserver();
	}

	public void startExercise() {
		systemwideController.loadExerciseForStudent();
	}

	public void onSubmitSolution() {
		systemwideController.submitSolution();
	}

	public void cleanWorkspace() {
		systemwideController.cleanWorkspace();
	}

	public Map<ResultsDTO, List<Feedback>> getFeedbackExcerise() {
		return systemwideController.getFeedbackExcerise();
	}

	public boolean canSubmit() {
		return !systemwideController.isSelectedExerciseExpired();
	}

	public boolean canClean() {
		return systemwideController.isSelectedExerciseInWorkspace();
	}

	public boolean connectToWebsocket(WebsocketCallback callBack) {
		return systemwideController.connectToWebsocket(callBack);
	}

	public boolean canFetchFeedback() {
		return true;
	}

	public IExercise getCurrentSelectedExercise() {
		return systemwideController.getCurrentSelectedExercise();
	}

	@Override
	public void setExerciseID(final String exerciseShortName) {
		try {
			systemwideController.setExerciseIdWithSelectedExam(exerciseShortName);
		} catch (ArtemisClientException e) {
			getAlertObserver().error(e.getMessage(), e);
		}
	}

	public void setExamToNull() {
		systemwideController.setExamToNull();
	}

	@Override
	public List<String> getExercisesShortNamesForExam(String examShortName) {
		return systemwideController.getExerciseShortNamesFromExam(examShortName).stream().map(IExercise::getShortName).collect(Collectors.toList());
	}

	public IExam setExam(String examName) {
		return systemwideController.setExam(examName);
	}

	public IStudentExam getCurrentlySelectedExam() {
		return systemwideController.getExam();
	}

	public IStudentExam startExam() {
		return systemwideController.startExam();
	}

	@Override
	protected ISystemwideController getSystemwideController() {
		return this.systemwideController;
	}

	public String getExamUrlForCurrentExam() {
		return systemwideController.getExamUrlForCurrentExam();
	}

	public void resetSelectedExercise() {
		systemwideController.resetSelectedExercise();
	}

	public boolean canResetExercise() {
		return systemwideController.isSelectedExerciseInWorkspace();
	}

	public void resetBackendState() {
		systemwideController.resetBackendState();
	}

	public Set<IAnnotation> getAnnotations() {
		return systemwideController.getAnnotations();
	}

	public String getCurrentProjectName() {
		return systemwideController.getCurrentProjectName();
	}
}
