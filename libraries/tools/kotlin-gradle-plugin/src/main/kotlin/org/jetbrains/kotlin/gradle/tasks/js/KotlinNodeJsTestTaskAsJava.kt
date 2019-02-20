package org.jetbrains.kotlin.gradle.tasks.js

import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.tasks.testing.JvmTestExecutionSpec
import org.gradle.api.internal.tasks.testing.TestExecuter
import org.gradle.api.internal.tasks.testing.TestResultProcessor
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.testing.Test
import org.gradle.process.internal.DefaultProcessForkOptions
import org.gradle.process.internal.ExecHandleFactory
import org.jetbrains.kotlin.gradle.injected
import org.jetbrains.kotlin.gradle.tasks.testing.teamcity.TCServiceMessagesTestExecutionSpec
import org.jetbrains.kotlin.gradle.tasks.testing.teamcity.TCServiceMessagesTestExecutor
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

internal val log = LoggerFactory.getLogger(KotlinNodeJsTestTaskAsJava::class.java)

/**
 * This is workaround for showing test results in IDEA: only classes extending
 * org.gradle.api.tasks.testing.Test are supported (which are Java tests, not AbstractTest).
 *
 * Also, looks like some other plugin works only with Java test task.
 *
 * For now this class is used by default.
 */
open class KotlinNodeJsTestTaskAsJava :
    Test(),
    KotlinNodeJsTestTaskCommons {
    override val taskName: String
        get() = name

    @Input
    override var ignoredTestSuites: IgnoredTestSuites =
        IgnoredTestSuites.showWithContents

    @Input
    @Optional
    var nodeJsExecutable: String? = null

    @Input
    override var nodeModulesDir: File? = null

    @Input
    @SkipWhenEmpty
    override var nodeModulesToLoad: Set<String> = setOf()

    @Input
    @Optional
    override var testRuntimeNodeModule: File? = null

    @Suppress("UnstableApiUsage")
    final override val filterExt: DefaultTestFilter
        get() = filter as DefaultTestFilter

    @Inject
    override fun getFileResolver(): FileResolver = injected

    override fun exclude(vararg excludes: String?): Test {
        return super.exclude(*excludes)
    }

    override val excludedPatterns: MutableSet<String>
        get() = excludes

    override fun getForkOptions() = DefaultProcessForkOptions(fileResolver).also {
        this.copyTo(it)

        it.executable = nodeJsExecutable
    }

    @get:Inject
    open val execHandleFactory: ExecHandleFactory
        get() = injected

    override fun createTestExecuter(): TestExecuter<JvmTestExecutionSpec> {
        val executor = TCServiceMessagesTestExecutor(
            execHandleFactory,
            buildOperationExecutor
        )
        val adapter = object : TestExecuter<SpecAdapter> {
            override fun execute(spec: SpecAdapter, testResultProcessor: TestResultProcessor) {
                executor.execute(spec.tc, testResultProcessor)
            }

            override fun stopNow() {
                executor.stopNow()
            }
        }

        return adapter as TestExecuter<JvmTestExecutionSpec>
    }

    override fun createTestExecutionSpec(): JvmTestExecutionSpec {
        return SpecAdapter(
            doCreateSpec(project),
            super.createTestExecutionSpec()
        )
    }

    class SpecAdapter(
        val tc: TCServiceMessagesTestExecutionSpec,
        base: JvmTestExecutionSpec
    ) : JvmTestExecutionSpec(
        base.testFramework,
        base.classpath,
        base.candidateClassFiles,
        base.isScanForTestClasses,
        base.testClassesDirs,
        base.path,
        base.identityPath,
        base.forkEvery,
        base.javaForkOptions,
        base.maxParallelForks,
        base.previousFailedTestClasses
    )

    override fun getCandidateClassFiles(): FileTree = project.files().asFileTree
}