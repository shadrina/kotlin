/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle;

import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.testing.AbstractTestTask;
import org.gradle.process.ProcessForkOptions;
import org.gradle.process.internal.DefaultProcessForkOptions;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;

/**
 * AbstractTestTask that implements ProcessForkOptions by delegating to org.gradle.process.internal.DefaultProcessForkOptions
 * <p>
 * In Java to get properties usable in Kotlin code.
 */
public abstract class AbstractTestProcessForkTask extends AbstractTestTask implements ProcessForkOptions {
    final private DefaultProcessForkOptions forkOptions;

    public AbstractTestProcessForkTask() {
        super();
        forkOptions = new DefaultProcessForkOptions(getFileResolver());
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Inject
    public FileResolver getFileResolver() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("WeakerAccess")
    public DefaultProcessForkOptions getForkOptions() {
        return forkOptions;
    }

    @Input
    @Optional
    @Override
    public String getExecutable() {
        return getForkOptions().getExecutable();
    }

    @Override
    public void setExecutable(String executable) {
        getForkOptions().setExecutable(executable);
    }

    @Override
    public void setExecutable(Object executable) {
        getForkOptions().setExecutable(executable);
    }

    @Override
    public ProcessForkOptions executable(Object executable) {
        return getForkOptions().executable(executable);
    }

    @Input
    @Optional
    @Override
    public File getWorkingDir() {
        return getForkOptions().getWorkingDir();
    }

    @Override
    public void setWorkingDir(File dir) {
        getForkOptions().setWorkingDir(dir);
    }

    @Override
    public void setWorkingDir(Object dir) {
        getForkOptions().setWorkingDir(dir);
    }

    @Override
    public ProcessForkOptions workingDir(Object dir) {
        return getForkOptions().workingDir(dir);
    }

    @Input
    @Optional
    @Override
    public Map<String, Object> getEnvironment() {
        return getForkOptions().getEnvironment();
    }

    public Map<String, String> getActualEnvironment() {
        return getForkOptions().getActualEnvironment();
    }

    @Override
    public void setEnvironment(Map<String, ?> environmentVariables) {
        getForkOptions().setEnvironment(environmentVariables);
    }

    @Override
    public ProcessForkOptions environment(String name, Object value) {
        return getForkOptions().environment(name, value);
    }

    @Override
    public ProcessForkOptions environment(Map<String, ?> environmentVariables) {
        return getForkOptions().environment(environmentVariables);
    }

    @Override
    public ProcessForkOptions copyTo(ProcessForkOptions target) {
        return getForkOptions().copyTo(target);
    }
}
