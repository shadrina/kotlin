package org.jetbrains.kotlin.gradle.tasks.testing.teamcity

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import org.gradle.api.internal.tasks.testing.TestExecuter
import org.gradle.api.internal.tasks.testing.TestExecutionSpec
import org.gradle.api.internal.tasks.testing.TestResultProcessor
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.process.ProcessForkOptions
import org.gradle.process.internal.ExecHandle
import org.gradle.process.internal.ExecHandleFactory
import org.jetbrains.kotlin.gradle.tasks.js.log
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.concurrent.thread

data class TCServiceMessagesTestExecutionSpec(
    val rootNodeName: String,
    val forkOptions: ProcessForkOptions,
    val args: List<String>,
    val emulateTestFailureExceptions: Boolean = true,
    val skipRoots: Boolean = true
) : TestExecutionSpec

class TCServiceMessagesTestExecutor(
    val execHandleFactory: ExecHandleFactory,
    val buildOperationExecutor: BuildOperationExecutor
) : TestExecuter<TCServiceMessagesTestExecutionSpec> {
    var execHandle: ExecHandle? = null
    var outputReaderThread: Thread? = null
    var shouldStop = false

    override fun execute(spec: TCServiceMessagesTestExecutionSpec, testResultProcessor: TestResultProcessor) {
        val pipeIn = PipedInputStream()

        val rootOperation = buildOperationExecutor.currentOperation.parentId

        outputReaderThread = thread(name = "${spec.forkOptions} output reader") {
            val client = TCServiceMessagesClient(testResultProcessor, spec)

            client.root(rootOperation) {
                pipeIn.reader().useLines { lines ->
                    lines.forEach {
                        if (shouldStop) {
                            client.closeAll()
                            return@thread
                        }

                        try {
                            ServiceMessage.parse(it)?.let(client::receive)
                        } catch (e: Exception) {
                            log.error(
                                "Error while processing test process output message \"$it\"",
                                e
                            )
                        }
                    }
                }
            }
        }

        val exec = execHandleFactory.newExec()
        spec.forkOptions.copyTo(exec)
        exec.args = spec.args
        exec.standardOutput = PipedOutputStream(pipeIn)

        execHandle = exec.build()

        execHandle!!.start()
        val result = execHandle!!.waitForFinish()
        outputReaderThread!!.join()

        if (result.exitValue != 0) {
            error("$execHandle exited with errors (exit code: ${result.exitValue})")
        }
    }

    override fun stopNow() {
        shouldStop = true
        execHandle?.abort()
        outputReaderThread?.join()
    }
}