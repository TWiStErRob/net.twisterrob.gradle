package net.twisterrob.gradle.graph.tasks;

enum TaskResult {
	/**
	 * @see org.gradle.api.execution.TaskExecutionListener#beforeExecute(org.gradle.api.Task)
	 * @see org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter#execute
	 */
	executing,
	/**
	 * @see org.gradle.api.tasks.TaskState#getExecuted()
	 * @see org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter#execute
	 */
	completed,
	/**
	 * @see org.gradle.api.tasks.TaskState#getDidWork()
	 * @see org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter#executeActions
	 */
	nowork,
	/**
	 * @see org.gradle.api.tasks.TaskState#getSkipped()
	 * @see org.gradle.api.tasks.TaskState#getSkipMessage()
	 * @see org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter#execute
	 */
	skipped,
	/**
	 * @see org.gradle.api.tasks.TaskState#getSkipped()
	 * @see org.gradle.api.tasks.TaskState#getSkipMessage()
	 * @see org.gradle.api.internal.tasks.TaskStateInternal#upToDate
	 */
	uptodate,
	/**
	 * @see org.gradle.api.tasks.TaskState#getFailure()
	 * @see org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter#execute
	 */
	failure
}
