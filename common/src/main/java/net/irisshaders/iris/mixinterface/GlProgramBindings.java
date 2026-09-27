package net.irisshaders.iris.mixinterface;

import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;

import java.util.List;

public interface GlProgramBindings {
	void iris$setupBindings(List<CompiledRenderPipeline.CreateInfo.Uniform> descriptions, int pushConstantsSize);
}
