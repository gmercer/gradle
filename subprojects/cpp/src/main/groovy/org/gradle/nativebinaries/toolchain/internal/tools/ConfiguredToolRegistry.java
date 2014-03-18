/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.nativebinaries.toolchain.internal.tools;

import org.gradle.nativebinaries.toolchain.GccTool;
import org.gradle.nativebinaries.toolchain.internal.ToolType;

import java.util.HashMap;
import java.util.Map;

public class ConfiguredToolRegistry implements ToolRegistry {
    private final Map<ToolType, GccToolInternal> gccTools = new HashMap<ToolType, GccToolInternal>();

    public ConfiguredToolRegistry(ConfigurableToolChainInternal configurableToolChain) {
        // TODO:DAZ Replace TargetPlatformConfiguration with an action that applies to the entire registry.
        register(configurableToolChain.getCCompiler());
        register(configurableToolChain.getCppCompiler());
        register(configurableToolChain.getAssembler());
        register(configurableToolChain.getObjcCompiler());
        register(configurableToolChain.getObjcppCompiler());
        register(configurableToolChain.getLinker());
        register(configurableToolChain.getStaticLibArchiver());
    }

    private GccTool register(GccToolInternal tool) {
        return gccTools.put(tool.getToolType(), tool);
    }

    public GccToolInternal getTool(ToolType toolType) {
        return gccTools.get(toolType);
    }
}