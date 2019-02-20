package org.jetbrains.kotlin.gradle.tasks.js

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.KotlinNodeJsTestPlugin
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

const val kotlinNodeJsTestRuntimeBin: String = "kotlin-js-test"

open class KotlinNodeJsTestRuntimeToNodeModulesTask : DefaultTask() {
    @Input
    public var resourceName: String = "kotlin-js-test.js"

    @Input
    @SkipWhenEmpty
    public lateinit var nodeModulesDir: File

    @TaskAction
    fun copyRuntime() {
        val testsRuntime = KotlinNodeJsTestPlugin::class.java.getResourceAsStream(resourceName)
        val bin = nodeModulesDir.resolve(".bin").resolve(kotlinNodeJsTestRuntimeBin)
        bin.parentFile.mkdirs()
        Files.copy(
            testsRuntime,
            bin.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
    }
}

data class KotlinNodeJsTestRunnerCliArgs(
    val moduleNames: List<String>,
    val include: Collection<String> = listOf(),
    val exclude: Collection<String> = listOf(),
    val ignoredTestSuites: IgnoredTestSuitesReporting = IgnoredTestSuitesReporting.reportAllInnerTestsAsIgnored
) {
    fun toList(): List<String> = mutableListOf<String>().also { args ->
        if (include.isNotEmpty()) {
            args.add("--include")
            args.add(include.joinToString(","))
        }

        if (exclude.isNotEmpty()) {
            args.add("--exclude")
            args.add(exclude.joinToString(","))
        }

        if (ignoredTestSuites !== IgnoredTestSuitesReporting.reportAllInnerTestsAsIgnored) {
            args.add("--ignoredTestSuites")
            args.add(ignoredTestSuites.name)
        }

        args.addAll(moduleNames)
    }

    @Suppress("EnumEntryName")
    enum class IgnoredTestSuitesReporting {
        skip, reportAsIgnoredTest, reportAllInnerTestsAsIgnored
    }
}

