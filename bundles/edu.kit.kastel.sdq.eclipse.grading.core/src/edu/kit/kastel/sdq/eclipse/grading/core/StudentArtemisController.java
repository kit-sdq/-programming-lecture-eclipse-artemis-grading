package edu.kit.kastel.sdq.eclipse.grading.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import edu.kit.kastel.sdq.eclipse.grading.api.ArtemisClientException;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.Feedback;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.ICourse;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.IExam;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.IExercise;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.IStudentExam;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.ParticipationDTO;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.ResultsDTO;
import edu.kit.kastel.sdq.eclipse.grading.api.controller.IStudentArtemisController;
import edu.kit.kastel.sdq.eclipse.grading.client.mappings.exam.ArtemisStudentExam;

public class StudentArtemisController extends ArtemisController implements IStudentArtemisController {

	protected StudentArtemisController(String host, String username, String password) {
		super(host, username, password);
	}
	
	@Override
	public List<ICourse> getCourses() {
		if(this.courses == null)
			this.courses = fetchCourses();
		return this.courses;
	}
	
	@Override
	public IStudentExam getExercisesFromStudentExam(final String examTitle) {
		return this.getExercisesFromExamOrStartExam(examTitle, this.getCourses());
	}
	
	@Override
	public List<String> getExerciseShortNamesFromExam(final String examTitle) {
		return this.getExercisesFromStudentExam(examTitle).getExercises().stream().map(IExercise::getShortName)
				.collect(Collectors.toList());
	}
	
	@Override
	public IStudentExam startExam(ICourse course, IExam exam) {
		try {
			return this.clientManager.getExamArtemisClient().conductExam(course, exam);
		} catch (ArtemisClientException e) {
			this.error("Error, can not start the exam: " + exam.getTitle(), e);
			return new ArtemisStudentExam();
		}
	}
	
	@Override
	public Optional<ParticipationDTO> getParticipation(ICourse course, IExercise exercise) {
		Optional<ParticipationDTO> participation = getParticipationForExercise(course, exercise);

		if (participation.isEmpty()) {
			try {
				participation = Optional.of(
						clientManager.getParticipationArtemisClient().startParticipationForExercise(course, exercise));
			} catch (ArtemisClientException e) {
				return Optional.empty();
			}
		}
		return participation;
	}
	
	@Override
	public Map<ResultsDTO, List<Feedback>> getFeedbackExcerise(ICourse course, IExercise exercise) {
		Optional<ParticipationDTO> participationOpt = getParticipationForExercise(course, exercise);
		if (participationOpt.isEmpty()) {
			return new HashMap<>();
		}

		ParticipationDTO participationWithResults;
		try {
			participationWithResults = this.clientManager.getParticipationArtemisClient()
					.getParticipationWithLatestResultForExercise(participationOpt.get().getParticipationID());
		} catch (ArtemisClientException e) {
			this.error("Can't load results for selected exercise " + exercise.getShortName() //
					+ ".\n No results found. Please check if a solution was submitted.", e);
			return new HashMap<>();
		}

		if (participationWithResults.getResults() == null) {
			return new HashMap<>();
		}

		Map<ResultsDTO, List<Feedback>> resultFeedbackMap = new HashMap<>();

		for (var result : participationWithResults.getResults()) {
			if (result.hasFeedback) {
				Feedback[] feedbacks = {};
				try {
					feedbacks = this.clientManager.getFeedbackArtemisClient()
							.getFeedbackForResult(participationOpt.get(), result);
				} catch (ArtemisClientException e) {
					e.printStackTrace();
					break;
				}
				resultFeedbackMap.put(result, Arrays.asList(feedbacks));
			}
		}

		if (resultFeedbackMap.isEmpty()) {
			this.error("Can't load any feedback for selected exercise " + exercise.getShortName() //
					+ ".\n No feedback found. Please check if a solution was submitted.", null);
		}

		return resultFeedbackMap;
	}
	
	@Override
	public List<ICourse> fetchCourses() {
		if (!this.clientManager.isReady()) {
			return List.of();
		}
		try {
			return this.clientManager.getCourseArtemisClient().getCoursesForDashboard();
		} catch (final Exception e) {
			this.error(e.getMessage(), e);
			return List.of();
		}
	}

	
	private IStudentExam getExercisesFromExamOrStartExam(final String examTitle, List<ICourse> courses) {
		Entry<ICourse, IExam> foundEntry = filterGetExamObjectFromLoadedCourses(examTitle, courses);
		if (foundEntry == null) {
			this.error("No exam found for examTitle=" + examTitle, null);
			return new ArtemisStudentExam();
		}
		try {
			return this.clientManager.getExamArtemisClient().findExamForSummary(foundEntry.getKey(),
					foundEntry.getValue());
		} catch (ArtemisClientException e) {
			this.error("The exam has not been submitted yet. \n"
					+ "You can only view results after the exam was submitted. \n"
					+ "To submit the exam you have to submit the exam in the Artemis webclient!. It is not possible in Eclipse!. \n"
					+ "To load exercises for the exam in to your local workspace you have to start the exam first! \n"
					+ "After starting the exam you can load exercises in the workspace und submit solutions \n "
					+ "After submitting solutions you can view results in the Result-Tab.", e);
		}
		if (this.confirm("Do you want to start the exam now?")) {
			return this.startExam(foundEntry.getKey(), foundEntry.getValue());
		}
		return new ArtemisStudentExam();
	}
	
	private Optional<ParticipationDTO> getParticipationForExercise(ICourse course, IExercise exercise) {
		try {
			return Optional
					.of(clientManager.getParticipationArtemisClient().getParticipationForExercise(course, exercise));
		} catch (ArtemisClientException e) {
			return Optional.empty();
		}
	}
}