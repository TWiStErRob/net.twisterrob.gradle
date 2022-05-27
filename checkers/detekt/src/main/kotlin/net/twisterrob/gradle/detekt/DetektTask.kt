package net.twisterrob.gradle.detekt

import groovy.lang.Closure
import groovy.lang.DelegatesTo
import io.gitlab.arturbosch.detekt.Detekt
import net.twisterrob.gradle.common.ALL_VARIANTS_NAME
import net.twisterrob.gradle.common.TargetChecker
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.Input
import javax.inject.Inject

open class DetektTask @Inject constructor(
	objects: ObjectFactory
) : Detekt(objects), TargetChecker, Reporting<DetektReports> {

	@Input
	override var checkTargetName: String = ALL_VARIANTS_NAME

	private val _reports: DetektReports

	init {
		group = JavaBasePlugin.VERIFICATION_GROUP
		_reports = objects.newInstance(DetektReportsImpl::class.java, this)
		classpath.from(project.files())
//		isShowViolations = false
	}

	override fun getReports() =
		_reports

	override fun reports(
		@DelegatesTo(value = DetektReports::class, strategy = Closure.DELEGATE_FIRST) closure: Closure<*>
	): DetektReports {
		@Suppress("DEPRECATION")
		return reports(org.gradle.util.ClosureBackedAction<DetektReports>(closure))
	}

	override fun reports(configureAction: Action<in DetektReports>): DetektReports {
		configureAction.execute(_reports)
		return getReports()
	}
}
