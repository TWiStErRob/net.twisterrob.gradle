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
	 * @see org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid
	 */
	NoWork,

	/**
	 * @see org.gradle.api.tasks.TaskState.getSkipped
	 * @see org.gradle.api.tasks.TaskState.getSkipMessage
	 * @see org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute
	 * @see org.gradle.api.internal.tasks.TaskExecutionOutcome.SKIPPED
	 */
	Skipped,

	/**
	 * @see org.gradle.api.tasks.TaskState.getUpToDate
	 * @see org.gradle.api.internal.tasks.TaskExecutionOutcome.UP_TO_DATE
	 */
	UpToDate,

	/**
	 * @see org.gradle.api.tasks.TaskState.getNoSource
	 * @see org.gradle.api.internal.tasks.TaskExecutionOutcome.NO_SOURCE
	 */
	NoSource,

	/**
	 * @see org.gradle.api.tasks.TaskState.getSkipped
	 * @see org.gradle.api.tasks.TaskState.getSkipMessage
	 * @see org.gradle.api.internal.tasks.TaskExecutionOutcome.FROM_CACHE
	 */
	FromCache,

	/**
	 * @see org.gradle.api.tasks.TaskState.getFailure
	 * @see org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute
	 */
	Failure,
}
