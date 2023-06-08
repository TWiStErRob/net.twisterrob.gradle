package net.twisterrob.gradle.graph.tasks

enum class TaskResult {
	/**
	 * @see org.gradle.api.execution.TaskExecutionListener.beforeExecute
	 * @see org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute
	 */
	Executing,

	/**
	 * @see org.gradle.api.tasks.TaskState.getExecuted
	 * @see org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute
	 */
	Completed,

	/**
	 * @see org.gradle.api.tasks.TaskState.getDidWork
	 * @see org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeActions
	 */
	NoWork,

	/**
	 * @see org.gradle.api.tasks.TaskState.getSkipped
	 * @see org.gradle.api.tasks.TaskState.getSkipMessage
	 * @see org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute
	 */
	Skipped,

	/**
	 * @see org.gradle.api.tasks.TaskState.getSkipped
	 * @see org.gradle.api.tasks.TaskState.getSkipMessage
	 * @see org.gradle.api.internal.tasks.TaskStateInternal.upToDate
	 */
	UpToDate,

	/**
	 * @see org.gradle.api.tasks.TaskState.getFailure
	 * @see org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute
	 */
	Failure,
}
