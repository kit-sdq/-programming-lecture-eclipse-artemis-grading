/* Licensed under EPL-2.0 2022-2023. */
package edu.kit.kastel.eclipse.common.core.artemis;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.ui.internal.UpdateMavenProjectJob;

import edu.kit.kastel.eclipse.common.api.controller.ISubmissionLifecycleCallback;

@SuppressWarnings("restriction")
public class WorkspaceUtil {
	private static final ILog LOG = Platform.getLog(WorkspaceUtil.class);

	/**
	 * Create a new eclipse project given a projectName which corresponds to an
	 * EXISTING project in the workspace. Natures are Maven and Java
	 *
	 * @param buildCallbacks Are called when the triggered build has completed
	 */
	public static void createEclipseProject(final File projectDirectory, List<ISubmissionLifecycleCallback> buildCallbacks) throws CoreException {
		createEclipseProject(projectDirectory.getName(), buildCallbacks);
	}

	/**
	 * Create a new eclipse project given a projectName which corresponds to an
	 * EXISTING project in the workspace. Natures are Maven and Java
	 *
	 * @param buildCallbacks Are called when the triggered build has completed
	 */
	public static void createEclipseProject(final String projectName, List<ISubmissionLifecycleCallback> buildCallbacks) throws CoreException {
		final IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);

		final String[] natures = { JavaCore.NATURE_ID, IMavenConstants.NATURE_ID };
		description.setNatureIds(natures);
		description.setBuildSpec(new ICommand[] { //
				createBuildCommand("org.eclipse.jdt.core.javabuilder"), //
				createBuildCommand("org.eclipse.m2e.core.maven2Builder") //
		});

		// and save it
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		project.create(null);
		project.open(null);
		project.setDescription(description, null);

		var job = new UpdateMavenProjectJob(List.of(project));
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent e) {
				LOG.info("Maven update & build completed. Notifying " + buildCallbacks.size() + " listeners");
				buildCallbacks.forEach(c -> c.onPhaseCompleted(project));
			}
		});
		job.schedule();
	}

	private static ICommand createBuildCommand(String name) {
		BuildCommand command = new BuildCommand();
		command.setBuilderName(name);
		return command;
	}

	public static void deleteDirectoryRecursively(final Path directory) throws IOException {
		Files.walkFileTree(directory, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Delete a given eclipse project via eclipse functionality and, thereafter, on
	 * the file system if anything is left.
	 */
	public static void deleteEclipseProject(final String projectName) throws CoreException, IOException {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project == null || !project.exists()) {
			// doesnt exist ==> nothing to be done
			return;
		}
		File projectLocation = project.getLocation().toFile();
		project.delete(true, null);

		if (projectLocation.exists()) {
			deleteDirectoryRecursively(projectLocation.toPath());
		}
	}

	/**
	 *
	 * @return the current workspace as a file.
	 */
	public static File getWorkspaceFile() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();

	}

	private WorkspaceUtil() {
	}
}
