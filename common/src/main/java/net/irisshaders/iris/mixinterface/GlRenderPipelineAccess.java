package net.irisshaders.iris.mixinterface;

import com.mojang.renderpearl.backend.api.BackendRenderPipeline;

public interface GlRenderPipelineAccess {
	com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline.CreateInfo getCreateInfo();
}
