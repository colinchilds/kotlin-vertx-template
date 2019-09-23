rootProject.name = "kvt"

val projects = listOf("Koddle")

for (project: String in projects) {
    include(project)
    project(":$project").projectDir = File(settingsDir, "../$project")
}