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

package org.jetbrains.kotlin.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import kotlin.meta.Node;
import kotlin.meta.Writer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.macros.MacroExpander;
import org.jetbrains.kotlin.psi.macros.MetaTools;
import org.jetbrains.kotlin.psi.stubs.KotlinStubWithFqName;
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes;

import java.util.Collections;
import java.util.List;

public abstract class KtTypeParameterListOwnerStub<T extends KotlinStubWithFqName<?>>
        extends KtNamedDeclarationStub<T> implements KtTypeParameterListOwner, KtReplaceable {
    private KtElement replacedElement = this;
    private KtElement hiddenElement;
    private MetaTools metaTools;

    private boolean isHidden = false;
    private boolean isRoot = false;

    public KtTypeParameterListOwnerStub(@NotNull T stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public KtTypeParameterListOwnerStub(@NotNull ASTNode node) {
        super(node);
        metaTools = new MetaTools(node);
    }

    @Override
    @Nullable
    public KtTypeParameterList getTypeParameterList() {
        return getStubOrPsiChild(KtStubElementTypes.TYPE_PARAMETER_LIST);
    }

    @Override
    @Nullable
    public KtTypeConstraintList getTypeConstraintList() {
        return getStubOrPsiChild(KtStubElementTypes.TYPE_CONSTRAINT_LIST);
    }

    @Override
    @NotNull
    public List<KtTypeConstraint> getTypeConstraints() {
        KtTypeConstraintList typeConstraintList = getTypeConstraintList();
        if (typeConstraintList == null) {
            return Collections.emptyList();
        }
        return typeConstraintList.getConstraints();
    }

    @Override
    @NotNull
    public List<KtTypeParameter> getTypeParameters() {
        KtTypeParameterList list = getTypeParameterList();
        if (list == null) return Collections.emptyList();

        return list.getParameters();
    }

    @Override
    public void initializeHiddenElement(@NotNull MacroExpander macroExpander) {
        if (metaTools == null || !isMacroAnnotated()) {
            return;
        }
        Node nodeToConvert = convertToNode();
        Node converted = macroExpander.run(getAnnotationEntries().get(0), nodeToConvert);
        if (converted == null) {
            return;
        }
        String convertedText = Writer.Companion.write(converted, null);
        KtReplaceable convertedKtElement = (KtReplaceable) createHiddenElementFromContent(convertedText);
        KtReplaceableKt.markHiddenRoot(convertedKtElement, this);
        hiddenElement = convertedKtElement;
    }

    @NotNull
    @Override
    public MetaTools getMetaTools() {
        return metaTools;
    }

    @Override
    public void setMetaTools(@NotNull MetaTools metaTools) {
        this.metaTools = metaTools;
    }

    @NotNull
    @Override
    public KtElement getReplacedElement() {
        return replacedElement;
    }

    @Override
    public void setReplacedElement(@NotNull KtElement replacedElement) {
        this.replacedElement = replacedElement;
    }

    @NotNull
    @Override
    public KtElement getHiddenElement() {
        return hiddenElement;
    }

    @Override
    public void setHiddenElement(@NotNull KtElement hiddenElement) {
        this.hiddenElement = hiddenElement;
    }

    @Override
    public boolean getHasHiddenElementInitialized() {
        return hiddenElement != null;
    }

    @Override
    public boolean isHidden() {
        return isHidden;
    }

    @Override
    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    @Override
    public boolean isRoot() {
        return isRoot;
    }

    @Override
    public void setRoot(boolean root) {
        isRoot = root;
    }
}
