package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import com.mojang.renderpearl.backend.opengl.VertexArray;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.GlRenderPipelineAccess;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(RenderSystem.class)
public abstract class MixinShaderManager_Overrides {
	@Unique
	private static Set<RenderPipeline> missingShaders = new HashSet<>();
	@Unique
	private static final Map<CompiledRenderPipeline, Map<GlProgram, Map<List<VertexFormat>, FrontendRenderPipeline>>> iris$overrides = new IdentityHashMap<>();

	@Inject(method = "getCompiledPipelineNullable", at = @At(value = "RETURN"), cancellable = true)
	private static void redirectIrisProgram(RenderPipeline renderPipeline, CallbackInfoReturnable<CompiledRenderPipeline> cir) {
		if (renderPipeline == CompositeRenderer.COMPOSITE_PIPELINE) return;
		if (renderPipeline == RenderPipelines.ANIMATE_SPRITE_BLIT || renderPipeline == RenderPipelines.ANIMATE_SPRITE_INTERPOLATE) return;

		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof IrisRenderingPipeline irisPipeline && irisPipeline.shouldOverrideShaders() && !ImmediateState.bypass) {
			RenderPipeline newProgram = renderPipeline;

			ShaderKey shaderKey = IrisPipelines.getPipeline(irisPipeline, newProgram);
			GlProgram program = shaderKey == null ? null : irisPipeline.getShaderMap().getShader(shaderKey);

			var oldProgram = (GlRenderPipeline) ((FrontendRenderPipeline) cir.getReturnValue()).backendRenderPipeline();

			var old2 = ((FrontendRenderPipeline) cir.getReturnValue());
			if (program != null) {
				GlDevice device = (GlDevice) ((GpuDeviceAccessor) RenderSystem.getDevice()).getBackend();
				List<VertexFormat> vertexFormats = new ArrayList<>(renderPipeline.getVertexFormatBindings());
				FrontendRenderPipeline cached = iris$overrides
					.computeIfAbsent(old2, unused -> new IdentityHashMap<>())
					.computeIfAbsent(program, unused -> new HashMap<>())
					.get(vertexFormats);
				if (cached != null) {
					cir.setReturnValue(cached);
					return;
				}

				com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline.CreateInfo createInfo = iris$createInfo(((GlRenderPipelineAccess) oldProgram).getCreateInfo(), program, vertexFormats);
				VertexArray vertexArray = VertexArray.createSource(GL.getCapabilities(), new HashSet<>()).apply(program, createInfo);
				IntList vertexFormatSizes = new IntArrayList(vertexFormats.size());
				for (VertexFormat vf : vertexFormats) {
					vertexFormatSizes.add(vf == null ? 0 : vf.getVertexSize());
				}
				FrontendRenderPipeline replacement = new FrontendRenderPipeline(old2.name(), new GlRenderPipeline(device, createInfo, program, vertexArray),
					vertexFormatSizes, old2.uniformIndices(), old2.uniforms(), old2.colorTargetStates(), old2.wantsDepthTexture(), old2.depthStencilFormat(), old2.pushConstantSize());
				iris$overrides.get(old2).get(program).put(vertexFormats, replacement);
				cir.setReturnValue(replacement);
			} else if (missingShaders.add(renderPipeline)) {
				if (renderPipeline.getLocation().getNamespace().equals("minecraft")) {
					Iris.logger.fatal("Missing program " + renderPipeline.getLocation() + " in override list. This is likely an Iris bug!!!", new Throwable());
				} else {
					Iris.logger.error("Missing program " + renderPipeline.getLocation() + " in override list. This is not a critical problem, but it could lead to weird rendering.", new Throwable());
				}
			}
		}
	}

	@Unique
	private static com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline.CreateInfo iris$createInfo(com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline.CreateInfo original, GlProgram program, List<VertexFormat> vertexFormats) {
		List<com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline.CreateInfo.VertexBuffer> vertexBuffers = new ArrayList<>();
		List<com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline.CreateInfo.AttribBinding> attribBindings = new ArrayList<>();

		for (int slot = 0; slot < vertexFormats.size(); slot++) {
			VertexFormat format = vertexFormats.get(slot);
			if (format == null) {
				continue;
			}

			vertexBuffers.add(new com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline.CreateInfo.VertexBuffer(slot, format.getVertexSize(), format.getStepRate()));
			for (var element : format.getElements()) {
				int location = GL33C.glGetAttribLocation(program.getProgramId(), "iris_" + element.name());
				if (location == -1) {
					location = GL33C.glGetAttribLocation(program.getProgramId(), element.name());
				}

				if (location != -1) {
					attribBindings.add(new com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline.CreateInfo.AttribBinding(slot, location, element.offset(), element.format()));
				}
			}
		}

		return new com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline.CreateInfo(original.name(), original.shaders(), vertexBuffers, attribBindings, original.uniforms(),
			original.pushConstantsSize(), original.depthStencilState(), original.depthStencilFormat(), original.polygonMode(), original.cull(), original.colorTargetStates(), original.primitiveTopology());
	}
}
