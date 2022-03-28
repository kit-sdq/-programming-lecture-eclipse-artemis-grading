/* Licensed under EPL-2.0 2022. */
package edu.kit.kastel.eclipse.student.view.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.kit.kastel.eclipse.student.view.activator.Activator;
import edu.kit.kastel.sdq.eclipse.grading.api.PreferenceConstants;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 * <p>
 */

public class ArtemisStudentPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ArtemisStudentPreferencesPage() {
		super(FieldEditorPreferencePage.GRID);
		this.setPreferenceStore(Activator.getDefault().getPreferenceStore());
		this.setDescription("Set preferences for Artemis Student");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI
	 * blocks needed to manipulate various types of preferences. Each field editor
	 * knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {

		StringFieldEditor artemisUrl = new StringFieldEditor(PreferenceConstants.ARTEMIS_URL, "Artemis URL: ", this.getFieldEditorParent());

		StringFieldEditor artemisUser = new StringFieldEditor(PreferenceConstants.ARTEMIS_USER, "Artemis username: ", this.getFieldEditorParent());

		StringFieldEditor artemisPassword = new StringFieldEditor(PreferenceConstants.ARTEMIS_PASSWORD, "Artemis password: ", this.getFieldEditorParent());

		artemisPassword.getTextControl(this.getFieldEditorParent()).setEchoChar('*');

		this.addField(artemisUrl);
		this.addField(artemisUser);
		this.addField(artemisPassword);

	}

	@Override
	public void init(IWorkbench workbench) {
		// NOP
	}

}