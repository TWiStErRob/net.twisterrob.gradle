package net.twisterrob.gradle.common

import net.twisterrob.gradle.common.AGPVersion.ReleaseType
import net.twisterrob.gradle.common.AGPVersion.ReleaseType.Alpha
import net.twisterrob.gradle.common.AGPVersion.ReleaseType.Beta
import net.twisterrob.gradle.common.AGPVersion.ReleaseType.Candidate
import net.twisterrob.gradle.common.AGPVersion.ReleaseType.Stable
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.comparesEqualTo
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.lessThan
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource

class AGPVersionTest {

	companion object {

		/**
		 * List of AGP versions from https://maven.google.com/web/index.html#com.android.tools.build:gradle.
		 */
		@JvmStatic
		fun mavenGoogle(): List<Arguments> =
			listOf(
				Arguments.of("7.2.0-alpha01", AGPVersion(7, 2, Alpha, 1)),
				Arguments.of("7.1.0-beta01", AGPVersion(7, 1, Beta, 1)),
				Arguments.of("7.1.0-alpha13", AGPVersion(7, 1, Alpha, 13)),
				Arguments.of("7.1.0-alpha12", AGPVersion(7, 1, Alpha, 12)),
				Arguments.of("7.1.0-alpha11", AGPVersion(7, 1, Alpha, 11)),
				Arguments.of("7.1.0-alpha10", AGPVersion(7, 1, Alpha, 10)),
				Arguments.of("7.1.0-alpha09", AGPVersion(7, 1, Alpha, 9)),
				Arguments.of("7.1.0-alpha08", AGPVersion(7, 1, Alpha, 8)),
				Arguments.of("7.1.0-alpha07", AGPVersion(7, 1, Alpha, 7)),
				Arguments.of("7.1.0-alpha06", AGPVersion(7, 1, Alpha, 6)),
				Arguments.of("7.1.0-alpha05", AGPVersion(7, 1, Alpha, 5)),
				Arguments.of("7.1.0-alpha04", AGPVersion(7, 1, Alpha, 4)),
				Arguments.of("7.1.0-alpha03", AGPVersion(7, 1, Alpha, 3)),
				Arguments.of("7.1.0-alpha02", AGPVersion(7, 1, Alpha, 2)),
				Arguments.of("7.1.0-alpha01", AGPVersion(7, 1, Alpha, 1)),
				Arguments.of("7.0.3", AGPVersion(7, 0, Stable, 3)),
				Arguments.of("7.0.2", AGPVersion(7, 0, Stable, 2)),
				Arguments.of("7.0.1", AGPVersion(7, 0, Stable, 1)),
				Arguments.of("7.0.0", AGPVersion(7, 0, Stable, 0)),
				Arguments.of("7.0.0-rc01", AGPVersion(7, 0, Candidate, 1)),
				Arguments.of("7.0.0-beta05", AGPVersion(7, 0, Beta, 5)),
				Arguments.of("7.0.0-beta04", AGPVersion(7, 0, Beta, 4)),
				Arguments.of("7.0.0-beta03", AGPVersion(7, 0, Beta, 3)),
				Arguments.of("7.0.0-beta02", AGPVersion(7, 0, Beta, 2)),
				Arguments.of("7.0.0-beta01", AGPVersion(7, 0, Beta, 1)),
				Arguments.of("7.0.0-alpha15", AGPVersion(7, 0, Alpha, 15)),
				Arguments.of("7.0.0-alpha14", AGPVersion(7, 0, Alpha, 14)),
				Arguments.of("7.0.0-alpha13", AGPVersion(7, 0, Alpha, 13)),
				Arguments.of("7.0.0-alpha12", AGPVersion(7, 0, Alpha, 12)),
				Arguments.of("7.0.0-alpha11", AGPVersion(7, 0, Alpha, 11)),
				Arguments.of("7.0.0-alpha10", AGPVersion(7, 0, Alpha, 10)),
				Arguments.of("7.0.0-alpha09", AGPVersion(7, 0, Alpha, 9)),
				Arguments.of("7.0.0-alpha08", AGPVersion(7, 0, Alpha, 8)),
				Arguments.of("7.0.0-alpha07", AGPVersion(7, 0, Alpha, 7)),
				Arguments.of("7.0.0-alpha06", AGPVersion(7, 0, Alpha, 6)),
				Arguments.of("7.0.0-alpha05", AGPVersion(7, 0, Alpha, 5)),
				Arguments.of("7.0.0-alpha04", AGPVersion(7, 0, Alpha, 4)),
				Arguments.of("7.0.0-alpha03", AGPVersion(7, 0, Alpha, 3)),
				Arguments.of("7.0.0-alpha02", AGPVersion(7, 0, Alpha, 2)),
				Arguments.of("7.0.0-alpha01", AGPVersion(7, 0, Alpha, 1)),
				Arguments.of("4.2.2", AGPVersion(4, 2, Stable, 2)),
				Arguments.of("4.2.1", AGPVersion(4, 2, Stable, 1)),
				Arguments.of("4.2.0", AGPVersion(4, 2, Stable, 0)),
				Arguments.of("4.2.0-rc01", AGPVersion(4, 2, Candidate, 1)),
				Arguments.of("4.2.0-beta06", AGPVersion(4, 2, Beta, 6)),
				Arguments.of("4.2.0-beta05", AGPVersion(4, 2, Beta, 5)),
				Arguments.of("4.2.0-beta04", AGPVersion(4, 2, Beta, 4)),
				Arguments.of("4.2.0-beta03", AGPVersion(4, 2, Beta, 3)),
				Arguments.of("4.2.0-beta02", AGPVersion(4, 2, Beta, 2)),
				Arguments.of("4.2.0-beta01", AGPVersion(4, 2, Beta, 1)),
				Arguments.of("4.2.0-alpha16", AGPVersion(4, 2, Alpha, 16)),
				Arguments.of("4.2.0-alpha15", AGPVersion(4, 2, Alpha, 15)),
				Arguments.of("4.2.0-alpha14", AGPVersion(4, 2, Alpha, 14)),
				Arguments.of("4.2.0-alpha13", AGPVersion(4, 2, Alpha, 13)),
				Arguments.of("4.2.0-alpha12", AGPVersion(4, 2, Alpha, 12)),
				Arguments.of("4.2.0-alpha11", AGPVersion(4, 2, Alpha, 11)),
				Arguments.of("4.2.0-alpha10", AGPVersion(4, 2, Alpha, 10)),
				Arguments.of("4.2.0-alpha09", AGPVersion(4, 2, Alpha, 9)),
				Arguments.of("4.2.0-alpha08", AGPVersion(4, 2, Alpha, 8)),
				Arguments.of("4.2.0-alpha07", AGPVersion(4, 2, Alpha, 7)),
				Arguments.of("4.2.0-alpha06", AGPVersion(4, 2, Alpha, 6)),
				Arguments.of("4.2.0-alpha05", AGPVersion(4, 2, Alpha, 5)),
				Arguments.of("4.2.0-alpha04", AGPVersion(4, 2, Alpha, 4)),
				Arguments.of("4.2.0-alpha03", AGPVersion(4, 2, Alpha, 3)),
				Arguments.of("4.2.0-alpha02", AGPVersion(4, 2, Alpha, 2)),
				Arguments.of("4.2.0-alpha01", AGPVersion(4, 2, Alpha, 1)),
				Arguments.of("4.1.3", AGPVersion(4, 1, Stable, 3)),
				Arguments.of("4.1.2", AGPVersion(4, 1, Stable, 2)),
				Arguments.of("4.1.1", AGPVersion(4, 1, Stable, 1)),
				Arguments.of("4.1.0", AGPVersion(4, 1, Stable, 0)),
				Arguments.of("4.1.0-rc03", AGPVersion(4, 1, Candidate, 3)),
				Arguments.of("4.1.0-rc02", AGPVersion(4, 1, Candidate, 2)),
				Arguments.of("4.1.0-rc01", AGPVersion(4, 1, Candidate, 1)),
				Arguments.of("4.1.0-beta05", AGPVersion(4, 1, Beta, 5)),
				Arguments.of("4.1.0-beta04", AGPVersion(4, 1, Beta, 4)),
				Arguments.of("4.1.0-beta03", AGPVersion(4, 1, Beta, 3)),
				Arguments.of("4.1.0-beta02", AGPVersion(4, 1, Beta, 2)),
				Arguments.of("4.1.0-beta01", AGPVersion(4, 1, Beta, 1)),
				Arguments.of("4.1.0-alpha10", AGPVersion(4, 1, Alpha, 10)),
				Arguments.of("4.1.0-alpha09", AGPVersion(4, 1, Alpha, 9)),
				Arguments.of("4.1.0-alpha08", AGPVersion(4, 1, Alpha, 8)),
				Arguments.of("4.1.0-alpha07", AGPVersion(4, 1, Alpha, 7)),
				Arguments.of("4.1.0-alpha06", AGPVersion(4, 1, Alpha, 6)),
				Arguments.of("4.1.0-alpha05", AGPVersion(4, 1, Alpha, 5)),
				Arguments.of("4.1.0-alpha04", AGPVersion(4, 1, Alpha, 4)),
				Arguments.of("4.1.0-alpha03", AGPVersion(4, 1, Alpha, 3)),
				Arguments.of("4.1.0-alpha02", AGPVersion(4, 1, Alpha, 2)),
				Arguments.of("4.1.0-alpha01", AGPVersion(4, 1, Alpha, 1)),
				Arguments.of("4.0.2", AGPVersion(4, 0, Stable, 2)),
				Arguments.of("4.0.1", AGPVersion(4, 0, Stable, 1)),
				Arguments.of("4.0.0", AGPVersion(4, 0, Stable, 0)),
				Arguments.of("4.0.0-rc01", AGPVersion(4, 0, Candidate, 1)),
				Arguments.of("4.0.0-beta05", AGPVersion(4, 0, Beta, 5)),
				Arguments.of("4.0.0-beta04", AGPVersion(4, 0, Beta, 4)),
				Arguments.of("4.0.0-beta03", AGPVersion(4, 0, Beta, 3)),
				Arguments.of("4.0.0-beta02", AGPVersion(4, 0, Beta, 2)),
				Arguments.of("4.0.0-beta01", AGPVersion(4, 0, Beta, 1)),
				Arguments.of("4.0.0-alpha09", AGPVersion(4, 0, Alpha, 9)),
				Arguments.of("4.0.0-alpha08", AGPVersion(4, 0, Alpha, 8)),
				Arguments.of("4.0.0-alpha07", AGPVersion(4, 0, Alpha, 7)),
				Arguments.of("4.0.0-alpha06", AGPVersion(4, 0, Alpha, 6)),
				Arguments.of("4.0.0-alpha05", AGPVersion(4, 0, Alpha, 5)),
				Arguments.of("4.0.0-alpha04", AGPVersion(4, 0, Alpha, 4)),
				Arguments.of("4.0.0-alpha03", AGPVersion(4, 0, Alpha, 3)),
				Arguments.of("4.0.0-alpha02", AGPVersion(4, 0, Alpha, 2)),
				Arguments.of("4.0.0-alpha01", AGPVersion(4, 0, Alpha, 1)),
				Arguments.of("3.6.4", AGPVersion(3, 6, Stable, 4)),
				Arguments.of("3.6.3", AGPVersion(3, 6, Stable, 3)),
				Arguments.of("3.6.2", AGPVersion(3, 6, Stable, 2)),
				Arguments.of("3.6.1", AGPVersion(3, 6, Stable, 1)),
				Arguments.of("3.6.0", AGPVersion(3, 6, Stable, 0)),
				Arguments.of("3.6.0-rc03", AGPVersion(3, 6, Candidate, 3)),
				Arguments.of("3.6.0-rc02", AGPVersion(3, 6, Candidate, 2)),
				Arguments.of("3.6.0-rc01", AGPVersion(3, 6, Candidate, 1)),
				Arguments.of("3.6.0-beta05", AGPVersion(3, 6, Beta, 5)),
				Arguments.of("3.6.0-beta04", AGPVersion(3, 6, Beta, 4)),
				Arguments.of("3.6.0-beta03", AGPVersion(3, 6, Beta, 3)),
				Arguments.of("3.6.0-beta02", AGPVersion(3, 6, Beta, 2)),
				Arguments.of("3.6.0-beta01", AGPVersion(3, 6, Beta, 1)),
				Arguments.of("3.6.0-alpha12", AGPVersion(3, 6, Alpha, 12)),
				Arguments.of("3.6.0-alpha11", AGPVersion(3, 6, Alpha, 11)),
				Arguments.of("3.6.0-alpha10", AGPVersion(3, 6, Alpha, 10)),
				Arguments.of("3.6.0-alpha09", AGPVersion(3, 6, Alpha, 9)),
				Arguments.of("3.6.0-alpha08", AGPVersion(3, 6, Alpha, 8)),
				Arguments.of("3.6.0-alpha07", AGPVersion(3, 6, Alpha, 7)),
				Arguments.of("3.6.0-alpha06", AGPVersion(3, 6, Alpha, 6)),
				Arguments.of("3.6.0-alpha05", AGPVersion(3, 6, Alpha, 5)),
				Arguments.of("3.6.0-alpha04", AGPVersion(3, 6, Alpha, 4)),
				Arguments.of("3.6.0-alpha03", AGPVersion(3, 6, Alpha, 3)),
				Arguments.of("3.6.0-alpha02", AGPVersion(3, 6, Alpha, 2)),
				Arguments.of("3.6.0-alpha01", AGPVersion(3, 6, Alpha, 1)),
				Arguments.of("3.5.4", AGPVersion(3, 5, Stable, 4)),
				Arguments.of("3.5.3", AGPVersion(3, 5, Stable, 3)),
				Arguments.of("3.5.2", AGPVersion(3, 5, Stable, 2)),
				Arguments.of("3.5.1", AGPVersion(3, 5, Stable, 1)),
				Arguments.of("3.5.0", AGPVersion(3, 5, Stable, 0)),
				Arguments.of("3.5.0-rc03", AGPVersion(3, 5, Candidate, 3)),
				Arguments.of("3.5.0-rc02", AGPVersion(3, 5, Candidate, 2)),
				Arguments.of("3.5.0-rc01", AGPVersion(3, 5, Candidate, 1)),
				Arguments.of("3.5.0-beta05", AGPVersion(3, 5, Beta, 5)),
				Arguments.of("3.5.0-beta04", AGPVersion(3, 5, Beta, 4)),
				Arguments.of("3.5.0-beta03", AGPVersion(3, 5, Beta, 3)),
				Arguments.of("3.5.0-beta02", AGPVersion(3, 5, Beta, 2)),
				Arguments.of("3.5.0-beta01", AGPVersion(3, 5, Beta, 1)),
				Arguments.of("3.5.0-alpha13", AGPVersion(3, 5, Alpha, 13)),
				Arguments.of("3.5.0-alpha12", AGPVersion(3, 5, Alpha, 12)),
				Arguments.of("3.5.0-alpha11", AGPVersion(3, 5, Alpha, 11)),
				Arguments.of("3.5.0-alpha10", AGPVersion(3, 5, Alpha, 10)),
				Arguments.of("3.5.0-alpha09", AGPVersion(3, 5, Alpha, 9)),
				Arguments.of("3.5.0-alpha08", AGPVersion(3, 5, Alpha, 8)),
				Arguments.of("3.5.0-alpha07", AGPVersion(3, 5, Alpha, 7)),
				Arguments.of("3.5.0-alpha06", AGPVersion(3, 5, Alpha, 6)),
				Arguments.of("3.5.0-alpha05", AGPVersion(3, 5, Alpha, 5)),
				Arguments.of("3.5.0-alpha04", AGPVersion(3, 5, Alpha, 4)),
				Arguments.of("3.5.0-alpha03", AGPVersion(3, 5, Alpha, 3)),
				Arguments.of("3.5.0-alpha02", AGPVersion(3, 5, Alpha, 2)),
				Arguments.of("3.5.0-alpha01", AGPVersion(3, 5, Alpha, 1)),
				Arguments.of("3.4.3", AGPVersion(3, 4, Stable, 3)),
				Arguments.of("3.4.2", AGPVersion(3, 4, Stable, 2)),
				Arguments.of("3.4.1", AGPVersion(3, 4, Stable, 1)),
				Arguments.of("3.4.0", AGPVersion(3, 4, Stable, 0)),
				Arguments.of("3.4.0-rc03", AGPVersion(3, 4, Candidate, 3)),
				Arguments.of("3.4.0-rc02", AGPVersion(3, 4, Candidate, 2)),
				Arguments.of("3.4.0-rc01", AGPVersion(3, 4, Candidate, 1)),
				Arguments.of("3.4.0-beta05", AGPVersion(3, 4, Beta, 5)),
				Arguments.of("3.4.0-beta04", AGPVersion(3, 4, Beta, 4)),
				Arguments.of("3.4.0-beta03", AGPVersion(3, 4, Beta, 3)),
				Arguments.of("3.4.0-beta02", AGPVersion(3, 4, Beta, 2)),
				Arguments.of("3.4.0-beta01", AGPVersion(3, 4, Beta, 1)),
				Arguments.of("3.4.0-alpha10", AGPVersion(3, 4, Alpha, 10)),
				Arguments.of("3.4.0-alpha09", AGPVersion(3, 4, Alpha, 9)),
				Arguments.of("3.4.0-alpha08", AGPVersion(3, 4, Alpha, 8)),
				Arguments.of("3.4.0-alpha07", AGPVersion(3, 4, Alpha, 7)),
				Arguments.of("3.4.0-alpha06", AGPVersion(3, 4, Alpha, 6)),
				Arguments.of("3.4.0-alpha05", AGPVersion(3, 4, Alpha, 5)),
				Arguments.of("3.4.0-alpha04", AGPVersion(3, 4, Alpha, 4)),
				Arguments.of("3.4.0-alpha03", AGPVersion(3, 4, Alpha, 3)),
				Arguments.of("3.4.0-alpha02", AGPVersion(3, 4, Alpha, 2)),
				Arguments.of("3.4.0-alpha01", AGPVersion(3, 4, Alpha, 1)),
				Arguments.of("3.3.3", AGPVersion(3, 3, Stable, 3)),
				Arguments.of("3.3.2", AGPVersion(3, 3, Stable, 2)),
				Arguments.of("3.3.1", AGPVersion(3, 3, Stable, 1)),
				Arguments.of("3.3.0", AGPVersion(3, 3, Stable, 0)),
				Arguments.of("3.3.0-rc03", AGPVersion(3, 3, Candidate, 3)),
				Arguments.of("3.3.0-rc02", AGPVersion(3, 3, Candidate, 2)),
				Arguments.of("3.3.0-rc01", AGPVersion(3, 3, Candidate, 1)),
				Arguments.of("3.3.0-beta04", AGPVersion(3, 3, Beta, 4)),
				Arguments.of("3.3.0-beta03", AGPVersion(3, 3, Beta, 3)),
				Arguments.of("3.3.0-beta02", AGPVersion(3, 3, Beta, 2)),
				Arguments.of("3.3.0-beta01", AGPVersion(3, 3, Beta, 1)),
				Arguments.of("3.3.0-alpha13", AGPVersion(3, 3, Alpha, 13)),
				Arguments.of("3.3.0-alpha12", AGPVersion(3, 3, Alpha, 12)),
				Arguments.of("3.3.0-alpha11", AGPVersion(3, 3, Alpha, 11)),
				Arguments.of("3.3.0-alpha10", AGPVersion(3, 3, Alpha, 10)),
				Arguments.of("3.3.0-alpha09", AGPVersion(3, 3, Alpha, 9)),
				Arguments.of("3.3.0-alpha08", AGPVersion(3, 3, Alpha, 8)),
				Arguments.of("3.3.0-alpha07", AGPVersion(3, 3, Alpha, 7)),
				Arguments.of("3.3.0-alpha06", AGPVersion(3, 3, Alpha, 6)),
				Arguments.of("3.3.0-alpha05", AGPVersion(3, 3, Alpha, 5)),
				Arguments.of("3.3.0-alpha04", AGPVersion(3, 3, Alpha, 4)),
				Arguments.of("3.3.0-alpha03", AGPVersion(3, 3, Alpha, 3)),
				Arguments.of("3.3.0-alpha02", AGPVersion(3, 3, Alpha, 2)),
				Arguments.of("3.3.0-alpha01", AGPVersion(3, 3, Alpha, 1)),
				Arguments.of("3.2.1", AGPVersion(3, 2, Stable, 1)),
				Arguments.of("3.2.0", AGPVersion(3, 2, Stable, 0)),
				Arguments.of("3.2.0-rc03", AGPVersion(3, 2, Candidate, 3)),
				Arguments.of("3.2.0-rc02", AGPVersion(3, 2, Candidate, 2)),
				Arguments.of("3.2.0-rc01", AGPVersion(3, 2, Candidate, 1)),
				Arguments.of("3.2.0-beta05", AGPVersion(3, 2, Beta, 5)),
				Arguments.of("3.2.0-beta04", AGPVersion(3, 2, Beta, 4)),
				Arguments.of("3.2.0-beta03", AGPVersion(3, 2, Beta, 3)),
				Arguments.of("3.2.0-beta02", AGPVersion(3, 2, Beta, 2)),
				Arguments.of("3.2.0-beta01", AGPVersion(3, 2, Beta, 1)),
				Arguments.of("3.2.0-alpha18", AGPVersion(3, 2, Alpha, 18)),
				Arguments.of("3.2.0-alpha17", AGPVersion(3, 2, Alpha, 17)),
				Arguments.of("3.2.0-alpha16", AGPVersion(3, 2, Alpha, 16)),
				Arguments.of("3.2.0-alpha15", AGPVersion(3, 2, Alpha, 15)),
				Arguments.of("3.2.0-alpha14", AGPVersion(3, 2, Alpha, 14)),
				Arguments.of("3.2.0-alpha13", AGPVersion(3, 2, Alpha, 13)),
				Arguments.of("3.2.0-alpha12", AGPVersion(3, 2, Alpha, 12)),
				Arguments.of("3.2.0-alpha11", AGPVersion(3, 2, Alpha, 11)),
				Arguments.of("3.2.0-alpha10", AGPVersion(3, 2, Alpha, 10)),
				Arguments.of("3.2.0-alpha09", AGPVersion(3, 2, Alpha, 9)),
				Arguments.of("3.2.0-alpha08", AGPVersion(3, 2, Alpha, 8)),
				Arguments.of("3.2.0-alpha07", AGPVersion(3, 2, Alpha, 7)),
				Arguments.of("3.2.0-alpha06", AGPVersion(3, 2, Alpha, 6)),
				Arguments.of("3.2.0-alpha05", AGPVersion(3, 2, Alpha, 5)),
				Arguments.of("3.2.0-alpha04", AGPVersion(3, 2, Alpha, 4)),
				Arguments.of("3.2.0-alpha03", AGPVersion(3, 2, Alpha, 3)),
				Arguments.of("3.2.0-alpha02", AGPVersion(3, 2, Alpha, 2)),
				Arguments.of("3.2.0-alpha01", AGPVersion(3, 2, Alpha, 1)),
				Arguments.of("3.1.4", AGPVersion(3, 1, Stable, 4)),
				Arguments.of("3.1.3", AGPVersion(3, 1, Stable, 3)),
				Arguments.of("3.1.2", AGPVersion(3, 1, Stable, 2)),
				Arguments.of("3.1.1", AGPVersion(3, 1, Stable, 1)),
				Arguments.of("3.1.0", AGPVersion(3, 1, Stable, 0)),
				Arguments.of("3.1.0-rc03", AGPVersion(3, 1, Candidate, 3)),
				Arguments.of("3.1.0-rc02", AGPVersion(3, 1, Candidate, 2)),
				Arguments.of("3.1.0-rc01", AGPVersion(3, 1, Candidate, 1)),
				Arguments.of("3.1.0-beta4", AGPVersion(3, 1, Beta, 4)),
				Arguments.of("3.1.0-beta3", AGPVersion(3, 1, Beta, 3)),
				Arguments.of("3.1.0-beta2", AGPVersion(3, 1, Beta, 2)),
				Arguments.of("3.1.0-beta1", AGPVersion(3, 1, Beta, 1)),
				Arguments.of("3.1.0-alpha09", AGPVersion(3, 1, Alpha, 9)),
				Arguments.of("3.1.0-alpha08", AGPVersion(3, 1, Alpha, 8)),
				Arguments.of("3.1.0-alpha07", AGPVersion(3, 1, Alpha, 7)),
				Arguments.of("3.1.0-alpha06", AGPVersion(3, 1, Alpha, 6)),
				Arguments.of("3.1.0-alpha05", AGPVersion(3, 1, Alpha, 5)),
				Arguments.of("3.1.0-alpha04", AGPVersion(3, 1, Alpha, 4)),
				Arguments.of("3.1.0-alpha03", AGPVersion(3, 1, Alpha, 3)),
				Arguments.of("3.1.0-alpha02", AGPVersion(3, 1, Alpha, 2)),
				Arguments.of("3.1.0-alpha01", AGPVersion(3, 1, Alpha, 1)),
				Arguments.of("3.0.1", AGPVersion(3, 0, Stable, 1)),
				Arguments.of("3.0.0", AGPVersion(3, 0, Stable, 0)),
				Arguments.of("3.0.0-rc2", AGPVersion(3, 0, Candidate, 2)),
				Arguments.of("3.0.0-rc1", AGPVersion(3, 0, Candidate, 1)),
				Arguments.of("3.0.0-beta7", AGPVersion(3, 0, Beta, 7)),
				Arguments.of("3.0.0-beta6", AGPVersion(3, 0, Beta, 6)),
				Arguments.of("3.0.0-beta5", AGPVersion(3, 0, Beta, 5)),
				Arguments.of("3.0.0-beta4", AGPVersion(3, 0, Beta, 4)),
				Arguments.of("3.0.0-beta3", AGPVersion(3, 0, Beta, 3)),
				Arguments.of("3.0.0-beta2", AGPVersion(3, 0, Beta, 2)),
				Arguments.of("3.0.0-beta1", AGPVersion(3, 0, Beta, 1)),
				Arguments.of("3.0.0-alpha9", AGPVersion(3, 0, Alpha, 9)),
				Arguments.of("3.0.0-alpha8", AGPVersion(3, 0, Alpha, 8)),
				Arguments.of("3.0.0-alpha7", AGPVersion(3, 0, Alpha, 7)),
				Arguments.of("3.0.0-alpha6", AGPVersion(3, 0, Alpha, 6)),
				Arguments.of("3.0.0-alpha5", AGPVersion(3, 0, Alpha, 5)),
				Arguments.of("3.0.0-alpha4", AGPVersion(3, 0, Alpha, 4)),
				Arguments.of("3.0.0-alpha3", AGPVersion(3, 0, Alpha, 3)),
				Arguments.of("3.0.0-alpha2", AGPVersion(3, 0, Alpha, 2)),
				Arguments.of("3.0.0-alpha1", AGPVersion(3, 0, Alpha, 1)),
			)

		/**
		 * List of AGP versions from https://mvnrepository.com/artifact/com.android.tools.build/gradle.
		 */
		@JvmStatic
		fun mavenCentral(): List<Arguments> =
			listOf(
				Arguments.of("2.3.0", AGPVersion(2, 3, Stable, 0)),
				Arguments.of("2.1.3", AGPVersion(2, 1, Stable, 3)),
				Arguments.of("2.1.2", AGPVersion(2, 1, Stable, 2)),
				Arguments.of("2.1.0", AGPVersion(2, 1, Stable, 0)),
				Arguments.of("2.0.0", AGPVersion(2, 0, Stable, 0)),
				Arguments.of("1.5.0", AGPVersion(1, 5, Stable, 0)),
				Arguments.of("1.3.1", AGPVersion(1, 3, Stable, 1)),
				Arguments.of("1.3.0", AGPVersion(1, 3, Stable, 0)),
				Arguments.of("1.2.3", AGPVersion(1, 2, Stable, 3)),
				Arguments.of("1.2.2", AGPVersion(1, 2, Stable, 2)),
				Arguments.of("1.2.1", AGPVersion(1, 2, Stable, 1)),
				Arguments.of("1.2.0", AGPVersion(1, 2, Stable, 0)),
				Arguments.of("1.2.0-beta2", AGPVersion(1, 2, Beta, 2)),
				Arguments.of("1.2.0-beta1", AGPVersion(1, 2, Beta, 1)),
				Arguments.of("1.1.3", AGPVersion(1, 1, Stable, 3)),
				Arguments.of("1.1.2", AGPVersion(1, 1, Stable, 2)),
				Arguments.of("1.1.1", AGPVersion(1, 1, Stable, 1)),
				Arguments.of("1.1.0", AGPVersion(1, 1, Stable, 0)),
				Arguments.of("1.1.0-rc3", AGPVersion(1, 1, Candidate, 3)),
				Arguments.of("1.1.0-rc2", AGPVersion(1, 1, Candidate, 2)),
				Arguments.of("1.1.0-rc1", AGPVersion(1, 1, Candidate, 1)),
				Arguments.of("1.0.1", AGPVersion(1, 0, Stable, 1)),
				Arguments.of("1.0.0", AGPVersion(1, 0, Stable, 0)),
				Arguments.of("1.0.0-rc4", AGPVersion(1, 0, Candidate, 4)),
				Arguments.of("1.0.0-rc3", AGPVersion(1, 0, Candidate, 3)),
				Arguments.of("1.0.0-rc2", AGPVersion(1, 0, Candidate, 2)),
				Arguments.of("1.0.0-rc1", AGPVersion(1, 0, Candidate, 1)),
				Arguments.of("0.14.4", AGPVersion(0, 14, Stable, 4)),
				Arguments.of("0.14.3", AGPVersion(0, 14, Stable, 3)),
				Arguments.of("0.14.2", AGPVersion(0, 14, Stable, 2)),
				Arguments.of("0.14.1", AGPVersion(0, 14, Stable, 1)),
				Arguments.of("0.14.0", AGPVersion(0, 14, Stable, 0)),
				Arguments.of("0.13.3", AGPVersion(0, 13, Stable, 3)),
				Arguments.of("0.13.2", AGPVersion(0, 13, Stable, 2)),
				Arguments.of("0.13.1", AGPVersion(0, 13, Stable, 1)),
				Arguments.of("0.13.0", AGPVersion(0, 13, Stable, 0)),
				Arguments.of("0.12.2", AGPVersion(0, 12, Stable, 2)),
				Arguments.of("0.12.1", AGPVersion(0, 12, Stable, 1)),
				Arguments.of("0.12.0", AGPVersion(0, 12, Stable, 0)),
				Arguments.of("0.11.2", AGPVersion(0, 11, Stable, 2)),
				Arguments.of("0.11.1", AGPVersion(0, 11, Stable, 1)),
				Arguments.of("0.11.0", AGPVersion(0, 11, Stable, 0)),
				Arguments.of("0.10.4", AGPVersion(0, 10, Stable, 4)),
				Arguments.of("0.10.2", AGPVersion(0, 10, Stable, 2)),
				Arguments.of("0.10.1", AGPVersion(0, 10, Stable, 1)),
				Arguments.of("0.10.0", AGPVersion(0, 10, Stable, 0)),
				Arguments.of("0.9.2", AGPVersion(0, 9, Stable, 2)),
				Arguments.of("0.9.1", AGPVersion(0, 9, Stable, 1)),
				Arguments.of("0.9.0", AGPVersion(0, 9, Stable, 0)),
				Arguments.of("0.8.3", AGPVersion(0, 8, Stable, 3)),
				Arguments.of("0.8.2", AGPVersion(0, 8, Stable, 2)),
				Arguments.of("0.8.1", AGPVersion(0, 8, Stable, 1)),
				Arguments.of("0.8.0", AGPVersion(0, 8, Stable, 0)),
				Arguments.of("0.7.3", AGPVersion(0, 7, Stable, 3)),
				Arguments.of("0.7.2", AGPVersion(0, 7, Stable, 2)),
				Arguments.of("0.7.1", AGPVersion(0, 7, Stable, 1)),
				Arguments.of("0.7.0", AGPVersion(0, 7, Stable, 0)),
				Arguments.of("0.6.3", AGPVersion(0, 6, Stable, 3)),
				Arguments.of("0.6.2", AGPVersion(0, 6, Stable, 2)),
				Arguments.of("0.6.1", AGPVersion(0, 6, Stable, 1)),
				Arguments.of("0.6.0", AGPVersion(0, 6, Stable, 0)),
				Arguments.of("0.5.7", AGPVersion(0, 5, Stable, 7)),
				Arguments.of("0.5.6", AGPVersion(0, 5, Stable, 6)),
				Arguments.of("0.5.5", AGPVersion(0, 5, Stable, 5)),
				Arguments.of("0.5.4", AGPVersion(0, 5, Stable, 4)),
				Arguments.of("0.5.3", AGPVersion(0, 5, Stable, 3)),
				Arguments.of("0.5.2", AGPVersion(0, 5, Stable, 2)),
				Arguments.of("0.5.1", AGPVersion(0, 5, Stable, 1)),
				Arguments.of("0.5.0", AGPVersion(0, 5, Stable, 0)),
				Arguments.of("0.4.3", AGPVersion(0, 4, Stable, 3)),
				Arguments.of("0.4.2", AGPVersion(0, 4, Stable, 2)),
				Arguments.of("0.4.1", AGPVersion(0, 4, Stable, 1)),
				Arguments.of("0.4", AGPVersion(0, 4, Stable, null)),
				Arguments.of("0.3", AGPVersion(0, 3, Stable, null)),
				Arguments.of("0.2", AGPVersion(0, 2, Stable, null)),
				Arguments.of("0.1", AGPVersion(0, 1, Stable, null)),
			)

		@JvmStatic
		fun lowerTypes(): List<Arguments> =
			ReleaseType.values()
				.flatMap { type ->
					ReleaseType.values()
						.filter { it < type }
						.map { Arguments.of(type, it) }
				}

		@JvmStatic
		fun higherTypes(): List<Arguments> =
			ReleaseType.values()
				.flatMap { type ->
					ReleaseType.values()
						.filter { it > type }
						.map { Arguments.of(type, it) }
				}
	}

	@MethodSource("mavenCentral", "mavenGoogle")
	@ParameterizedTest fun parsing(input: String, expected: AGPVersion) {
		val version = AGPVersion.parse(input)
		assertEquals(expected, version)
	}

	@Test fun sorting() {
		val all = (mavenGoogle() + mavenCentral()).reversed().map { it.get()[1] as AGPVersion }
		assertThat(all.sorted(), contains(*all.toTypedArray()))

		val unsorted = all.shuffled()
		assertThat(unsorted.sorted(), contains(*all.toTypedArray()))
	}

	@EnumSource(ReleaseType::class)
	@ParameterizedTest fun comparison(type: ReleaseType) {
		infix fun AGPVersion.assertLessThan(other: AGPVersion) {
			assertThat(this, lessThan(other))
			assertThat(other, greaterThan(this))
		}

		val majorJoker = AGPVersion(4, null, null, null)
		val minorJoker = AGPVersion(4, 1, null, null)
		val typeJoker = AGPVersion(4, 1, type, null)
		val majorLower = AGPVersion(3, 1, type, 0)
		val majorHigher = AGPVersion(5, 2, type, 0)
		val minorLower = AGPVersion(4, 0, type, 0)
		val minorHigher = AGPVersion(4, 2, type, 0)
		val patchRange = AGPVersion(4, 1, type, 2)

		assertThat(majorJoker, comparesEqualTo(majorJoker))
		assertThat(minorJoker, comparesEqualTo(minorJoker))
		assertThat(typeJoker, comparesEqualTo(typeJoker))
		assertThat(majorLower, comparesEqualTo(majorLower))
		assertThat(majorHigher, comparesEqualTo(majorHigher))
		assertThat(minorLower, comparesEqualTo(minorLower))
		assertThat(minorHigher, comparesEqualTo(minorHigher))
		assertThat(patchRange, comparesEqualTo(patchRange))

		majorJoker assertLessThan minorJoker
		minorJoker assertLessThan typeJoker
		majorJoker assertLessThan typeJoker

		majorLower assertLessThan majorHigher
		minorLower assertLessThan minorHigher
		majorLower assertLessThan minorLower
		minorHigher assertLessThan majorHigher

		majorLower assertLessThan majorJoker
		majorLower assertLessThan minorJoker
		majorLower assertLessThan typeJoker

		majorJoker assertLessThan majorHigher
		minorJoker assertLessThan majorHigher
		typeJoker assertLessThan majorHigher

		majorJoker assertLessThan minorLower
		minorLower assertLessThan minorJoker
		minorLower assertLessThan typeJoker

		majorJoker assertLessThan minorHigher
		minorJoker assertLessThan minorHigher
		typeJoker assertLessThan minorHigher

		majorJoker assertLessThan patchRange
		minorJoker assertLessThan patchRange
		typeJoker assertLessThan patchRange
	}

	@EnumSource(ReleaseType::class)
	@ParameterizedTest fun compatibility(type: ReleaseType) {
		val majorJoker = AGPVersion(4, null, null, null)
		val minorJoker = AGPVersion(4, 1, null, null)
		val typeJoker = AGPVersion(4, 1, type, null)
		val majorLower = AGPVersion(3, 1, type, 0)
		val majorHigher = AGPVersion(5, 2, type, 0)
		val minorLower = AGPVersion(4, 0, type, 0)
		val minorHigher = AGPVersion(4, 2, type, 0)
		val patchRange = AGPVersion(4, 1, type, 2)

		assertTrue(majorJoker compatible majorJoker)
		assertTrue(minorJoker compatible minorJoker)
		assertTrue(typeJoker compatible typeJoker)
		assertThrows<IllegalArgumentException> { majorLower compatible majorLower }
		assertThrows<IllegalArgumentException> { majorHigher compatible majorHigher }
		assertThrows<IllegalArgumentException> { minorLower compatible minorLower }
		assertThrows<IllegalArgumentException> { minorHigher compatible minorHigher }
		assertThrows<IllegalArgumentException> { patchRange compatible patchRange }

		assertTrue(majorJoker compatible minorJoker)
		assertTrue(majorJoker compatible typeJoker)
		assertTrue(minorJoker compatible typeJoker)
		assertFalse(minorJoker compatible majorJoker)
		assertFalse(typeJoker compatible majorJoker)
		assertFalse(typeJoker compatible minorJoker)

		val stables = listOf(majorLower, majorHigher, minorLower, minorHigher, patchRange)
		stables.forEach { first ->
			stables.forEach { second ->
				if (first != second) {
					assertThrows<IllegalArgumentException> { first compatible second }
				}
			}
		}
		assertFalse(majorJoker compatible majorLower)
		assertTrue(majorJoker compatible minorLower)
		assertTrue(majorJoker compatible patchRange)
		assertTrue(majorJoker compatible minorHigher)
		assertFalse(majorJoker compatible majorHigher)

		assertFalse(minorJoker compatible majorLower)
		assertFalse(minorJoker compatible minorLower)
		assertTrue(minorJoker compatible patchRange)
		assertFalse(minorJoker compatible minorHigher)
		assertFalse(minorJoker compatible majorHigher)

		assertFalse(typeJoker compatible majorLower)
		assertFalse(typeJoker compatible minorLower)
		assertTrue(typeJoker compatible patchRange)
		assertFalse(typeJoker compatible minorHigher)
		assertFalse(typeJoker compatible majorHigher)
	}

	@MethodSource("lowerTypes")
	@ParameterizedTest fun `compatibility of lower types`(type: ReleaseType, other: ReleaseType) {
		assertThat(other, lessThan(type))
		val typeJoker = AGPVersion(4, 1, type, null)
		val typeLower = AGPVersion(4, 1, other, 0)
		assertFalse(typeJoker compatible typeLower)
	}

	@EnumSource(ReleaseType::class)
	@ParameterizedTest fun `compatibility of patch`(type: ReleaseType) {
		val typeJoker = AGPVersion(4, 1, type, null)
		(0..3).forEach { patch ->
			val typeSame = AGPVersion(4, 1, type, patch)
			assertTrue(typeJoker compatible typeSame)
		}
	}

	@MethodSource("higherTypes")
	@ParameterizedTest fun `compatibility of higher types`(type: ReleaseType, other: ReleaseType) {
		assertThat(other, greaterThan(type))
		val typeJoker = AGPVersion(4, 1, type, null)
		val typeHigher = AGPVersion(4, 1, other, 0)
		assertFalse(typeJoker compatible typeHigher)
	}
}
