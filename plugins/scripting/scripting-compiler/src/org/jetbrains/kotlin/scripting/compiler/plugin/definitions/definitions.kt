/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin.definitions

import com.intellij.ide.highlighter.JavaClassFileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.script.KotlinScriptDefinition
import org.jetbrains.kotlin.script.ScriptDefinitionProvider

fun PsiFile.scriptDefinition(project: Project): KotlinScriptDefinition? {
    if (this !is KtFile || this.script == null) return null
    if (virtualFile.isNonScript()) return null

    return scriptDefinitionByFileName(project, virtualFile.name)
}

fun VirtualFile.findScriptDefinition(project: Project): KotlinScriptDefinition? {
    if (isNonScript()) return null
    if ((PsiManager.getInstance(project).findFile(this) as? KtFile)?.script == null) return null

    return scriptDefinitionByFileName(project, name)
}

fun scriptDefinitionByFileName(project: Project, fileName: String): KotlinScriptDefinition {
    val scriptDefinitionProvider = ScriptDefinitionProvider.getInstance(project) ?: return null
        ?: throw IllegalStateException("Unable to get script definition: ScriptDefinitionProvider is not configured.")

    return scriptDefinitionProvider.findScriptDefinition(fileName) ?: scriptDefinitionProvider.getDefaultScriptDefinition()
}

private fun VirtualFile.isNonScript(): Boolean =
    isDirectory ||
            extension == KotlinFileType.EXTENSION ||
            extension == JavaClassFileType.INSTANCE.defaultExtension ||
            !this.isKotlinFileType()

private fun VirtualFile.isKotlinFileType(): Boolean {
    val typeRegistry = FileTypeRegistry.getInstance()
    return typeRegistry.getFileTypeByFile(this) == KotlinFileType.INSTANCE ||
            typeRegistry.getFileTypeByFileName(name) == KotlinFileType.INSTANCE
}
