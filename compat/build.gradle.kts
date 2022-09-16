tasks.register("tests") {
	dependsOn(subprojects.map { it.tasks.named("test") })
}
