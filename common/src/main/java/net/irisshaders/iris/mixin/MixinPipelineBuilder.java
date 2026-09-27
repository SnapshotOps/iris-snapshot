package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.pipeline.PipelineBuilder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.ShaderSource;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.PipelineBuilder;
import net.irisshaders.iris.vertices.ImmediateState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(PipelineBuilder.class)
public class MixinPipelineBuilder {
	@ModifyReturnValue(method = "compilePipeline", at = @At("RETURN"))
	private CompletableFuture<CompiledRenderPipeline.Pending> iris$useDeclaredVertexFormats(CompletableFuture<CompiledRenderPipeline.Pending> original) {
		return original.thenApply(inner -> () -> {
			boolean previous = ImmediateState.skipExtension.get();
			ImmediateState.skipExtension.set(true);
			try {
				return inner.finishCompile();
			} finally {
				ImmediateState.skipExtension.set(previous);
			}
		});
	}

	@WrapMethod(method = "generateBackendCreateInfo")
	private CompiledRenderPipeline.CreateInfo iris$useDeclaredVertexFormats(RenderPipeline pipeline, ShaderSource shaderSource, Operation<CompiledRenderPipeline.CreateInfo> original) {
		boolean previous = ImmediateState.skipExtension.get();
		ImmediateState.skipExtension.set(true);
		try {
			return original.call(pipeline, shaderSource);
		} finally {
			ImmediateState.skipExtension.set(previous);
		}
	}
}
