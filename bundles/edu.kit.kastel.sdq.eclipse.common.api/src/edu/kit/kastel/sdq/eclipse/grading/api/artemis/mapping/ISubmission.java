package edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping;

import java.io.Serializable;

public interface ISubmission extends Serializable {

	String getParticipantIdentifier();

	String getParticipantName();

	String getRepositoryUrl();

	int getSubmissionId();

	/**
	 *
	 * @return whether this submission has an assessment known to artemis which is
	 *         "saved" or "submitted".
	 */
	boolean hasSavedAssessment();

	/**
	 *
	 * @return whether this submission has an assessment known to artemis which is
	 *         "submitted".
	 */
	boolean hasSubmittedAssessment();

	int getCorrectionRound();
}
