/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.backend.common.CodegenUtil
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.isHidden
import org.jetbrains.kotlin.psi.psiUtil.replacedParent

data class SourceInfo(val source: String, val pathOrCleanFQN: String, val linesInFile: Int) {

    companion object {
        fun createInfo(element: KtElement?, internalClassName: String): SourceInfo {
            assert(element != null) { "Couldn't create source mapper for null element $internalClassName" }
            val elementToAnalyze = (if (element!!.isHidden()) element.replacedParent() else element) as KtElement
            val lineNumbers = CodegenUtil.getLineNumberForElement(elementToAnalyze.containingFile, true)
                ?: error("Couldn't extract line count in ${elementToAnalyze.containingFile}")

            //TODO hack condition for package parts cleaning
            val isTopLevel = elementToAnalyze is KtFile || (elementToAnalyze is KtNamedFunction && elementToAnalyze.getParent() is KtFile)
            val cleanedClassFqName = if (!isTopLevel) internalClassName else internalClassName.substringBefore('$')

            return SourceInfo(elementToAnalyze.containingKtFile.name, cleanedClassFqName, lineNumbers)
        }

        fun createInfoForIr(lineNumbers: Int, internalClassName: String, containingFileName: String): SourceInfo {
            //TODO cut topLevel names
//            val isTopLevel = element is KtFile || (element is KtNamedFunction && element.getParent() is KtFile)
//            val cleanedClassFqName = if (!isTopLevel) internalClassName else internalClassName.substringBefore('$')

            return SourceInfo(containingFileName, internalClassName, lineNumbers)
        }
    }
}

