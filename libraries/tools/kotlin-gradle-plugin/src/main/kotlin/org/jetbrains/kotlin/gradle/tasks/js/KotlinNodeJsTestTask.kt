/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.tasks.js

import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.process.internal.ExecHandleFactory
import org.jetbrains.kotlin.gradle.AbstractTestProcessForkTask
import org.jetbrains.kotlin.gradle.tasks.testing.teamcity.TCServiceMessagesTestExecutionSpec
import org.jetbrains.kotlin.gradle.tasks.testing.teamcity.TCServiceMessagesTestExecutor
import java.io.File
import javax.inject.Inject

open class KotlinNodeJsTestTask :
    AbstractTestProcessForkTask(),
    KotlinNodeJsTestTaskCommons {
    override val taskName: String
        get() = name

    @Input
    override var ignoredTestSuites: IgnoredTestSuites =
        IgnoredTestSuites.showWithContents

    @Input
    var excludes = mutableSetOf<String>()

    override val excludedPatterns: MutableSet<String>
        get() = excludes

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

    init {
        filterExt.isFailOnNoMatchingTests = false
    }

    override fun createTestExecutionSpec(): TCServiceMessagesTestExecutionSpec = doCreateSpec(project)

    @get:Inject
    open val execHandleFactory: ExecHandleFactory
        get() = error("should be injected by gradle")

    override fun createTestExecuter() = TCServiceMessagesTestExecutor(
        execHandleFactory,
        buildOperationExecutor
    )
}