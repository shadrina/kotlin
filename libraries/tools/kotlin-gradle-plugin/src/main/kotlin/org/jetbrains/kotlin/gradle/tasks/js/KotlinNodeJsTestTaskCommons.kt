package org.jetbrains.kotlin.gradle.tasks.js

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.variant.VariantBuilder
import org.gradle.api.Project
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter
import org.gradle.internal.file.PathToFileResolver
import org.gradle.process.internal.DefaultProcessForkOptions
import org.jetbrains.kotlin.gradle.tasks.testing.teamcity.TCServiceMessagesTestExecutionSpec
import java.io.File

interface KotlinNodeJsTestTaskCommons {
    val taskName: String
    val ignoredTestSuites: IgnoredTestSuites
    val excludedPatterns: MutableSet<String>
    var nodeModulesDir: File?
    var nodeModulesToLoad: Set<String>
    val testRuntimeNodeModule: File?

    val filterExt: DefaultTestFilter

    fun getFileResolver(): PathToFileResolver

    fun getForkOptions(): DefaultProcessForkOptions

    val finalTestRuntimeNodeModule: File
        get() = testRuntimeNodeModule
            ?: nodeModulesDir!!.resolve(".bin").resolve(kotlinNodeJsTestRuntimeBin)


    fun doCreateSpec(project: Project): TCServiceMessagesTestExecutionSpec {
        val extendedForkOptions = DefaultProcessForkOptions(getFileResolver())
        getForkOptions().copyTo(extendedForkOptions)

        if (extendedForkOptions.executable == null) {
            extendedForkOptions.executable = getNodeJsFromMooworkPlugin(project, extendedForkOptions)
        }

        extendedForkOptions.environment.addPath("NODE_PATH", nodeModulesDir!!.canonicalPath)

        val cliArgs = KotlinNodeJsTestRunnerCliArgs(
            nodeModulesToLoad.toList(),
            filterExt.includePatterns + filterExt.commandLineIncludePatterns,
            excludedPatterns,
            ignoredTestSuites.cli
        )

        return TCServiceMessagesTestExecutionSpec(
            taskName,
            extendedForkOptions,
            listOf(finalTestRuntimeNodeModule.absolutePath) + cliArgs.toList()
        )
    }

    fun getNodeJsFromMooworkPlugin(
        project: Project,
        extendedForkOptions: DefaultProcessForkOptions
    ): String? {
        project.pluginManager.apply(NodePlugin::class.java)
        val nodeJsSettings = NodeExtension.get(project)
        return VariantBuilder(nodeJsSettings).build().nodeExec
    }
}

@Suppress("EnumEntryName")
enum class IgnoredTestSuites(val cli: KotlinNodeJsTestRunnerCliArgs.IgnoredTestSuitesReporting) {
    hide(KotlinNodeJsTestRunnerCliArgs.IgnoredTestSuitesReporting.skip),
    showWithContents(KotlinNodeJsTestRunnerCliArgs.IgnoredTestSuitesReporting.reportAllInnerTestsAsIgnored),
    showWithoutContents(KotlinNodeJsTestRunnerCliArgs.IgnoredTestSuitesReporting.reportAsIgnoredTest)
}

private fun MutableMap<String, Any>.addPath(key: String, path: String) {
    val prev = get(key)
    if (prev == null) set(key, path)
    else set(key, prev as String + File.pathSeparator + path)
}