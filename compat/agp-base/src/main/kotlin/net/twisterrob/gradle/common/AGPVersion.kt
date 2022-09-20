package net.twisterrob.gradle.common

data class AGPVersion(
	val major: Int,
	val minor: Int?,
	val type: ReleaseType?,
	val patch: Int?,
) : Comparable<AGPVersion> {

	init {
		require(major >= 0)
		require((minor ?: 0) >= 0)
		require((patch ?: 0) >= 0)
		if (patch != null) requireNotNull(type) { "If a patch release is given, you must specify its type." }
		if (patch != null) requireNotNull(minor) { "Cannot specify a patch version without minor." }
		if (type != null) requireNotNull(minor) { "Cannot specify a release type without minor." }
	}

	enum class ReleaseType : Comparable<ReleaseType> {
		Alpha,
		Beta,
		Candidate,
		Stable,
	}

	override fun compareTo(other: AGPVersion): Int =
		compareValuesBy(this, other, AGPVersion::major, AGPVersion::minor, AGPVersion::type, AGPVersion::patch)

	// TODEL https://github.com/detekt/detekt/issues/5321
	@Suppress("ReturnCount", "DataClassContainsFunctions")
	infix fun compatible(other: AGPVersion): Boolean {
		require(other.minor == null || other.type == null || other.patch == null) { "${other} must be a joker." }
		if (this.major != other.major) return false
		if (other.minor != null && this.minor != other.minor) return false
		if (other.type != null && this.type != other.type) return false
		if (other.patch != null && this.patch != other.patch) return false
		return true
	}

	override fun toString(): String =
		"${major}.${minor ?: "*"}.${type ?: "*"}.${patch ?: "*"}"

	companion object {

		private val AGP_VERSION_REGEX: Regex =
			"""^(?<major>\d+)(?:\.(?<minor>\d+)(?:\.(?<patch>\d+))?(?:-(?<type>alpha|beta|rc)(?<iteration>\d+))?)?$""".toRegex()

		fun parse(version: String): AGPVersion {
			val match = AGP_VERSION_REGEX.matchEntire(version)
				?: error("Unrecognised Android Gradle Plugin version: ${version}, only ${AGP_VERSION_REGEX} are supported.")
			val major = match.intGroup("major") ?: error("major in ${AGP_VERSION_REGEX} was empty for ${version}.")
			val minor = match.intGroup("minor")
			val patch = match.intGroup("patch")
			val iteration = match.groups["iteration"]?.run { value.trimStart('0').toInt() }
			if (iteration != null && patch != 0) {
				val complexVersion = "<major>.<minor>.<patch>-<type><iteration>"
				error("Invalid version ${version}, pattern: ${complexVersion} is not supported with patch other than 0.")
			}
			val type = when (val preReleaseTypeString = match.groups["type"]?.value) {
				null -> ReleaseType.Stable
				"alpha" -> ReleaseType.Alpha
				"beta" -> ReleaseType.Beta
				"rc" -> ReleaseType.Candidate
				else -> error("Unknown ${preReleaseTypeString} in ${version}.")
			}
			return AGPVersion(
				major = major,
				minor = minor,
				type = type,
				patch = iteration ?: patch
			)
		}

		private fun MatchResult.intGroup(name: String): Int? =
			groups[name]?.run { value.toInt() }
	}
}
