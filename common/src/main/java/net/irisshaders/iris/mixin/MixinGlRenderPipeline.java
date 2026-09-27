package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.VertexArray;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.mixinterface.GlProgramBindings;
import net.irisshaders.iris.mixinterface.GlRenderPipelineAccess;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.IrisProgram;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlRenderPipeline.class)
public class MixinGlRenderPipeline implements GlRenderPipelineAccess {
	@Shadow
	@Final
	private GlProgram program;
	@Unique
	private com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline.CreateInfo createInfo;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$setCreateInfo(GlDevice device, com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline.CreateInfo createInfo, GlProgram program, VertexArray vertexArray, CallbackInfo ci) {
		this.createInfo = createInfo;
	}
	@Override
	public com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline.CreateInfo getCreateInfo() {
		return this.createInfo;
	}

	@WrapOperation(method = "bind", at = @At(value = "FIELD", target = "Lcom/mojang/renderpearl/backend/opengl/GlRenderPipeline;cull:Z"))
	private boolean iris$redirectCull(GlRenderPipeline instance, Operation<Boolean> original) {
		return !ShadowRenderingState.areShadowsCurrentlyBeingRendered() && original.call(instance);
	}

	@WrapOperation(method = "bind", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_depthFunc(I)V"))
	private void iris$pass(GlStateManager stateManager, int compareOp, Operation<Void> original) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			original.call(stateManager, switch (compareOp) {
				case GL46C.GL_ALWAYS -> 519;
				case GL46C.GL_LESS -> GL46C.GL_GREATER;
				case GL46C.GL_LEQUAL -> GL46C.GL_GEQUAL;
				case GL46C.GL_EQUAL -> 514;
				case GL46C.GL_NOTEQUAL -> 517;
				case GL46C.GL_GEQUAL -> GL46C.GL_LEQUAL;
				case GL46C.GL_GREATER -> GL46C.GL_LESS;
				case GL46C.GL_NEVER -> 512;
				default -> throw new IllegalStateException("Unexpected value: " + compareOp);
			});
		} else {
			original.call(stateManager, compareOp);
		}
	}

	@WrapOperation(method = "bind", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_enableBlend(I)V"))
	private void iris$enableBlend(GlStateManager stateManager, int index, Operation<Void> original) {
		if (this.program instanceof IrisProgram && this.createInfo.colorTargetStates().size() == 1) {
			IrisRenderSystem.enableBlend();
		} else {
			original.call(stateManager, index);
		}
	}

	@WrapOperation(method = "bind", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_disableBlend(I)V"))
	private void iris$disableBlend(GlStateManager stateManager, int index, Operation<Void> original) {
		if (this.program instanceof IrisProgram && this.createInfo.colorTargetStates().size() == 1) {
			IrisRenderSystem.disableBlend();
		} else {
			original.call(stateManager, index);
		}
	}

	@Inject(method = "bind", at = @At("RETURN"))
	private void iris$bind(CallbackInfo ci) {
		if (this.program instanceof IrisProgram irisProgram) {
			((GlProgramBindings) this.program).iris$setupBindings(this.createInfo.uniforms(), this.createInfo.pushConstantsSize());
			irisProgram.iris$setupState(this.createInfo.uniforms());
		}
	}
}
