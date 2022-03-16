package edu.kit.kastel.sdq.eclipse.grading.core;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

import edu.kit.kastel.sdq.eclipse.grading.api.ArtemisClientException;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.Feedback;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.ICourse;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.IExam;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.IExercise;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.IStudentExam;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.ParticipationDTO;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.ResultsDTO;
import edu.kit.kastel.sdq.eclipse.grading.api.client.websocket.ArtemisWebsocketException;
import edu.kit.kastel.sdq.eclipse.grading.api.client.websocket.IWebsocketClient;
import edu.kit.kastel.sdq.eclipse.grading.api.client.websocket.WebsocketCallback;
import edu.kit.kastel.sdq.eclipse.grading.api.controller.IStudentArtemisController;
import edu.kit.kastel.sdq.eclipse.grading.api.controller.IWebsocketController;
import edu.kit.kastel.sdq.eclipse.grading.client.mappings.exam.ArtemisStudentExam;
import edu.kit.kastel.sdq.eclipse.grading.client.websocket.ArtemisFeedbackWebsocket;
import edu.kit.kastel.sdq.eclipse.grading.core.messages.Messages;

public class StudentArtemisController extends ArtemisController implements IStudentArtemisController, IWebsocketController {
	private static final ILog log = Platform.getLog(ArtemisFeedbackWebsocket.class);
	private final IWebsocketClient websocketClient;

	protected StudentArtemisController(String host, String username, String password) {
		super(host, username, password);
		this.websocketClient = new ArtemisFeedbackWebsocket(host);
	}

	@Override
	public IStudentExam getExercisesFromStudentExam(final String examTitle) {
		return this.getExercisesFromExamOrStartExam(examTitle, this.getCourses());
	}

	@Override
	public List<String> getExerciseShortNamesFromExam(final String examTitle) {
		return this.getExercisesFromStudentExam(examTitle).getExercises().stream().map(IExercise::getShortName).collect(Collectors.toList());
	}

	@Override
	public IStudentExam startExam(ICourse course, IExam exam) {
		try {
			if (this.confirm(Messages.STUDENT_ARTMIS_CONTROLLER_CONFIRM_START_EXAM)) {
				IStudentExam studentExam = this.clientManager.getExamArtemisClient().startExam(course, exam);
				checkIfExamIsValid(studentExam);
				return studentExam;
			}
		} catch (ArtemisClientException e) {
			this.error("Error, can not start the exam: " + exam.getTitle() + Messages.STUDENT_ARTMIS_CONTROLLER_EXAM_NO_SIGN_IN, e); //$NON-NLS-1$
		}
		return new ArtemisStudentExam();
	}

	private void checkIfExamIsValid(IStudentExam exam) {
		if (exam.isEnded()) {
			String errorMsg = "";
			errorMsg += Messages.STUDENT_ARTMIS_CONTROLLER_EXAM_OVER;
			if (!exam.isSubmitted()) {
				errorMsg += Messages.STUDENT_ARTMIS_CONTROLLER_EXAM_NOT_SUBMITTED;
			}
			this.warn(errorMsg);
		}
	}

	@Override
	public Optional<ParticipationDTO> getParticipation(ICourse course, IExercise exercise) {
		Optional<ParticipationDTO> participation = getParticipationForExercise(course, exercise);

		if (participation.isEmpty()) {
			try {
				participation = Optional.of(clientManager.getParticipationArtemisClient().startParticipationForExercise(course, exercise));
			} catch (ArtemisClientException | ConnectException e) {
				return Optional.empty();
			}
		}
		return participation;
	}

	@Override
	public Map<ResultsDTO, List<Feedback>> getFeedbackForExercise(ICourse course, IExercise exercise) {
		Optional<ParticipationDTO> participationOpt = getParticipationForExercise(course, exercise);
		if (participationOpt.isEmpty()) {
			return new HashMap<>();
		}

		ParticipationDTO participationWithResults;
		try {
			participationWithResults = this.clientManager.getParticipationArtemisClient()
					.getParticipationWithLatestResultForExercise(participationOpt.get().getParticipationID());
		} catch (ArtemisClientException e) {
			this.error(
					"Can't load results for selected exercise " + exercise.getShortName() + ".\n No results found. Please check if a solution was submitted.",
					e);
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
					feedbacks = this.clientManager.getFeedbackArtemisClient().getFeedbackForResult(participationOpt.get(), result);
				} catch (ArtemisClientException e) {
					e.printStackTrace();
					break;
				}
				resultFeedbackMap.put(result, Arrays.asList(feedbacks));
			} else {
				resultFeedbackMap.put(result, new ArrayList<>());
			}
		}

		if (resultFeedbackMap.isEmpty()) {
			this.error("Can't load any feedback for selected exercise " + exercise.getShortName()
					+ ".\n No feedback found. Please check if a solution was submitted.", null);
		}

		return resultFeedbackMap;
	}

	@Override
	protected List<ICourse> fetchCourses() {
		if (!this.clientManager.isReady()) {
			return List.of();
		}
		try {
			return this.clientManager.getCourseArtemisClient().getCoursesForDashboard();
		} catch (final ArtemisClientException e) {
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
			return this.clientManager.getExamArtemisClient().findExamForSummary(foundEntry.getKey(), foundEntry.getValue());
		} catch (ArtemisClientException e) {
			this.error(Messages.STUDENT_ARTMIS_CONTROLLER_EXAM_INFO, e);
		}
		return this.startExam(foundEntry.getKey(), foundEntry.getValue());
	}

	private Optional<ParticipationDTO> getParticipationForExercise(ICourse course, IExercise exercise) {
		try {
			return Optional.of(clientManager.getParticipationArtemisClient().getParticipationForExercise(course, exercise));
		} catch (ArtemisClientException e) {
			return Optional.empty();
		}
	}

	@Override
	public boolean connectToWebsocket(WebsocketCallback callback) {
		try {
			this.websocketClient.connect(callback, this.clientManager.getAuthenticationClient().getRawToken());
			return true;
		} catch (ArtemisWebsocketException e) {
			log.error("Can not connect to websocket.");
			return false;
		}
	}
}