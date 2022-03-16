package edu.kit.kastel.eclipse.common.view.observers;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;

import edu.kit.kastel.eclipse.common.view.utilities.AssessmentUtilities;
import edu.kit.kastel.sdq.eclipse.grading.api.controller.IAlertObserver;
import edu.kit.kastel.sdq.eclipse.grading.api.controller.IConfirmObserver;

/**
 * this class displays all messages from the backend in the view. It always
 * opens a dialog. An info, error or warning can be displayed.
 *
 */
public class ViewAlertObserver implements IAlertObserver, IConfirmObserver {

	private static final ILog log = Platform.getLog(ViewAlertObserver.class);

	@Override
	public void error(String errorMsg, Throwable cause) {
		log.error(errorMsg, cause);
		MessageDialog.openError(AssessmentUtilities.getWindowsShell(), "Error", errorMsg);
	}

	@Override
	public void info(String infoMsg) {
		log.info(infoMsg);
		MessageDialog.openInformation(AssessmentUtilities.getWindowsShell(), "Info", infoMsg);
	}

	@Override
	public void warn(String warningMsg) {
		log.warn(warningMsg);
		MessageDialog.openWarning(AssessmentUtilities.getWindowsShell(), "Warning", warningMsg);
	}

	@Override
	public boolean confirm(String msg) {
		return MessageDialog.openConfirm(AssessmentUtilities.getWindowsShell(), "Confirm", msg);
	}
}
