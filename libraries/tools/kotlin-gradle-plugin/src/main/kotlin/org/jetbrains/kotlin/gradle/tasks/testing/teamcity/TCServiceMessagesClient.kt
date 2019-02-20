package org.jetbrains.kotlin.gradle.tasks.testing.teamcity

import jetbrains.buildServer.messages.serviceMessages.*
import org.gradle.api.internal.tasks.testing.*
import org.gradle.api.tasks.testing.TestOutputEvent.Destination.StdErr
import org.gradle.api.tasks.testing.TestOutputEvent.Destination.StdOut
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.TestResult.ResultType.*
import org.gradle.internal.operations.OperationIdentifier

class TCServiceMessagesClient(
    private val results: TestResultProcessor,
    val settings: TCServiceMessagesTestExecutionSpec
) {
    /**
     * Node of tests tree
     */
    abstract inner class Node(
        var parent: Node? = null,
        val name: String
    ) {
        val reportingParent: Node?
            get() = when {
                parent == null -> null
                parent!!.skipReporting -> parent!!.reportingParent
                else -> parent
            }

        val skipReporting: Boolean
            get() = if (settings.skipRoots) parent != null && parent!!.parent == null else false

        val id: String = if (parent != null) "${reportingParent?.descriptor?.id}.$name" else name

        abstract val descriptor: TestDescriptorInternal

        var hasFailures: Boolean = false
            set(value) {
                // traverse parents only on first failure
                if (!field) {
                    field = value
                    parent?.hasFailures = true
                }
            }

        /**
         * If all tests in group are ignored, then group marked as skipped.
         * This is workaround for absence of ignored test suite flag in TC service messages protocol.
         */
        var containsNotIgnored: Boolean = false
            set(value) {
                // traverse parents only on first test
                if (!field) {
                    field = value
                    parent?.containsNotIgnored = true
                }
            }

        val resultType: TestResult.ResultType
            get() = when {
                containsNotIgnored -> when {
                    hasFailures -> FAILURE
                    else -> SUCCESS
                }
                else -> SKIPPED
            }

        override fun toString(): String = descriptor.toString()
    }

    inner class RootNode(val ownerBuildOperationId: Any) : Node(null, settings.rootNodeName) {
        override val descriptor =
            object : DefaultTestSuiteDescriptor(settings.rootNodeName, name) {
                override fun getOwnerBuildOperationId(): Any? = this@RootNode.ownerBuildOperationId
            }
    }

    inner class GroupNode(parent: Node? = null, name: String) : Node(parent, name) {
        override val descriptor = object : DefaultTestSuiteDescriptor(id, name) {
            override fun getParent(): TestDescriptorInternal? = this@GroupNode.parent?.descriptor
        }
    }

    inner class TestNode(parent: Node, name: String, ignored: Boolean = false) : Node(parent, name) {
        override val descriptor = object : DefaultTestMethodDescriptor(id, parent.id, name) {
            override fun getParent(): TestDescriptorInternal? = this@TestNode.parent?.descriptor
        }

        init {
            if (!ignored) containsNotIgnored = true
        }
    }

    private var leaf: Node? = null

    private val ServiceMessage.ts: Long
        get() = creationTimestamp?.timestamp?.time ?: System.currentTimeMillis()

    private fun push(node: Node) = node.also { leaf = node }
    private fun pop() = leaf!!.also { leaf = it.parent }

    inline fun root(operation: OperationIdentifier, actions: () -> Unit) {
        val root = open(System.currentTimeMillis(), RootNode(operation.id))
        actions()
        assert(close(System.currentTimeMillis(), root.name) === root)
    }

    @PublishedApi
    internal fun open(ts: Long, new: Node): Node = new.also {
        if (!it.skipReporting) {
            results.started(it.descriptor, TestStartEvent(ts, it.reportingParent?.descriptor?.id))
        }
        push(it)
    }

    @PublishedApi
    internal fun close(ts: Long, name: String) = pop().also {
        check(it.name == name)
        if (!it.skipReporting) {
            results.completed(it.descriptor.id, TestCompleteEvent(ts, it.resultType))
        }
    }

    fun closeAll() {
        val ts = System.nanoTime() / 1000000

        while (leaf != null) {
            close(ts, leaf!!.name)
        }
    }

    private fun Node.failure(
        message: TestFailed
    ) {
        hasFailures = true

        if (settings.emulateTestFailureExceptions) {
            results.failure(descriptor.id, object : Throwable(message.messageName) {
                override fun fillInStackTrace(): Throwable = this
                override fun toString(): String = message.stacktrace
            })
        } else {
            val stacktrace = message.stacktrace
            val errOutput = if (stacktrace.isNullOrBlank()) message.messageName else stacktrace

            results.output(leaf?.descriptor, DefaultTestOutputEvent(StdErr, errOutput))
        }
    }

    private fun requireGroup() = leaf ?: error("test out of group")
    private fun requireTest() = leaf as? TestNode
        ?: error("no running test")

    fun receive(message: ServiceMessage) {
        when (message) {
            is TestSuiteStarted -> open(message.ts, GroupNode(leaf, message.suiteName))
            is TestStarted -> open(message.ts, TestNode(requireGroup(), message.testName))
            is TestStdOut -> results.output(requireTest().descriptor, DefaultTestOutputEvent(StdOut, message.stdOut))
            is TestStdErr -> results.output(requireTest().descriptor, DefaultTestOutputEvent(StdErr, message.stdErr))
            is TestFailed -> requireTest().failure(message)
            is TestFinished -> close(message.ts, message.testName)
            is TestIgnored -> {
                if (message.attributes["suite"] == "true") {
                    // non standard property for dealing with ignored test suites without visiting all inner tests
                    open(message.ts, GroupNode(requireGroup(), message.testName)).also {
                        check(close(message.ts, message.testName) === it)
                    }
                } else {
                    open(message.ts, TestNode(requireGroup(), message.testName, ignored = true)).also {
                        check(close(message.ts, message.testName) === it)
                    }
                }
            }
            is TestSuiteFinished -> close(message.ts, message.suiteName)
            else -> Unit
        }
    }
}