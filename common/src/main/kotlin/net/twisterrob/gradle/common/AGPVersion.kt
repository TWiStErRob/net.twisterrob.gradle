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

	infix fun compatible(other: AGPVersion): Boolean {
		require(this.minor == null || this.type == null || this.patch == null) { "${this} must be a joker." }
		if (this.major != other.major) return false
		if (this.minor != null && this.minor != other.minor) return false
		if (this.type != null && this.type != other.type) return false
		if (this.patch != null && this.patch != other.patch) return false
		return true
	}

	override fun toString(): String =
		listOfNotNull(major, minor ?: "*", type ?: "*", patch ?: "*").joinToString(separator = ".")

	companion object {

		private val AGP_VERSION_REGEX =
			"""^(?<major>\d+)(?:\.(?<minor>\d+)(?:\.(?<patch>\d+))?(?:-(?<type>alpha|beta|rc)(?<iteration>\d+))?)?$""".toRegex()

		fun parse(version: String): AGPVersion {
			val match = AGP_VERSION_REGEX.matchEntire(version)
				?: error("Unrecognised Android Gradle Plugin version: ${version}, only ${AGP_VERSION_REGEX} are supported.")
			val major = match.groups["major"]!!.value.toInt()
			val minor = match.groups["minor"]?.value?.toInt()
			val patch = match.groups["patch"]?.value?.toInt()
			val iteration = match.groups["iteration"]?.value?.trimStart('0')?.toInt()
			if (iteration != null && patch != 0) {
				error("Invalid version, pattern: <major>.<minor>.<patch>-<type><iteration> is not supported with patch other than 0.")
			}
			val type = when (val preReleaseTypeString = match.groups["type"]?.value) {
				null -> ReleaseType.Stable
				"alpha" -> ReleaseType.Alpha
				"beta" -> ReleaseType.Beta
				"rc" -> ReleaseType.Candidate
				else -> error("Unknown ${preReleaseTypeString}")
			}
			return AGPVersion(major, minor, type, iteration ?: patch)
		}
	}

	override fun compareTo(other: AGPVersion): Int =
		compareValuesBy(this, other, AGPVersion::major, AGPVersion::minor, AGPVersion::type, AGPVersion::patch)
}
