import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.plugins.ExtensionContainer

@Suppress("UNCHECKED_CAST")
operator fun <T> ExtensionContainer.get(name: String): T = getByName(name) as T

operator fun ConfigurationContainer.get(name: String): Configuration = getAt(name)
