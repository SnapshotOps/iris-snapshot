package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlCommandEncoder;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlRenderPass;
import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.blaze3d.systems.ScissorState;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.irisshaders.iris.pipeline.programs.IrisProgram;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL46C;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GlCommandEncoder.class)
public class MixinGlCommandEncoder {
	@Shadow
	@Nullable
	private GlRenderPipeline lastPipeline;

	@Shadow
	@Nullable
	private GlProgram lastProgram;

	@Shadow
	private GlStateManager stateManager;

	@Unique
	private int tempFBO;

	@Unique
	private List<IrisProgram> programsToClear = new ArrayList<>();

	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;glViewport(IIII)V"))
	private void changeViewport(int i, int j, int k, int l) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return;
		} else {
			GL33C.glViewport(i, j, k, l);
		}
	}

	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;glScissor(IIII)V"))
	private void changeViewport2(int i, int j, int k, int l) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			GL33C.glScissor(0, 0, ShadowRenderer.RESOLUTION, ShadowRenderer.RESOLUTION);
			return;
		} else {
			GL33C.glScissor(i, j, k, l);
		}
	}

	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/ScissorState;enable(IIII)V"))
	private void changeViewport3(ScissorState instance, int x, int y, int width, int height) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			instance.enable(0, 0, ShadowRenderer.RESOLUTION, ShadowRenderer.RESOLUTION);
			return;
		} else {
			instance.enable(x, y, width, height);
		}
	}

	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_glBindFramebuffer(II)V"))
	private void changeFramebuffer(GlStateManager instance, int i, int j) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() || ImmediateState.safeToMultiply) {
			this.tempFBO = j;
			return;
		} else {
			instance._glBindFramebuffer(i, j);
		}
	}

	@Redirect(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;glDrawBuffers([I)V"))
	private void iris$changeDrawBuffers(int[] buffers) {
		if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered() && !ImmediateState.safeToMultiply) {
			if (buffers == null || buffers.length == 0) {
				return;
			}
			int currentDrawFbo = org.lwjgl.opengl.GL11C.glGetInteger(org.lwjgl.opengl.GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
			if (currentDrawFbo == 0) {
				boolean hasAttachment = false;
				for (int b : buffers) {
					if (b != org.lwjgl.opengl.GL11C.GL_NONE) {
						hasAttachment = true;
						break;
					}
				}
				GL33C.glDrawBuffers(hasAttachment ? new int[]{org.lwjgl.opengl.GL11C.GL_BACK} : new int[]{org.lwjgl.opengl.GL11C.GL_NONE});
			} else {
				try {
					GL33C.glDrawBuffers(buffers);
				} catch (Throwable ignored) {
				}
			}
		}
	}

	@Unique
	private static GlRenderPass lastPass;

	@Inject(method = "setupDraw", at = @At(value = "FIELD", target = "Lcom/mojang/renderpearl/backend/opengl/GlCommandEncoder;lastPipeline:Lcom/mojang/renderpearl/backend/opengl/GlRenderPipeline;", opcode = Opcodes.GETFIELD), cancellable = true)
	private void iris$bypassSetup(GlRenderPass glRenderPass, CallbackInfo cir) {
		DepthColorStorage.unlockDepthColor();

		if (ImmediateState.safeToMultiply && !(glRenderPass.pipeline.program() instanceof ExtendedShader)) {
			this.stateManager._glBindFramebuffer(GL46C.GL_FRAMEBUFFER, tempFBO);
		}

		lastPass = glRenderPass;
		if (Iris.isPackInUseQuick() || glRenderPass.iris$getCustomPass() != null) {
			this.lastProgram = null;
		}

		if (glRenderPass.iris$getCustomPass() != null) {
			cir.cancel();

			BlendModeOverride.restore();
			IrisRenderSystem.disableBlend();
			glRenderPass.iris$getCustomPass().setupState();

			if (glRenderPass.isScissorEnabled()) {
				this.stateManager._enableScissorTest();
				GL33C.glScissor(glRenderPass.getScissorX(), glRenderPass.getScissorY(), glRenderPass.getScissorWidth(), glRenderPass.getScissorHeight());
			} else {
				this.stateManager._disableScissorTest();
			}

			this.stateManager._disableDepthTest();
			this.stateManager._depthMask(false);
			this.stateManager._disablePolygonOffset();
			this.stateManager._disableCull();
			this.stateManager._colorMask(15);
		}
		if (glRenderPass.pipeline.program() instanceof ExtendedShader shader) {
			ImmediateState.usingTessellation = shader.usesTessellation();
		}
	}

	@Inject(method = "setupDraw", at = @At("RETURN"))
	private void iris$trackProgram(GlRenderPass glRenderPass, CallbackInfo ci) {
		if (glRenderPass.pipeline.program() instanceof IrisProgram irisProgram && !this.programsToClear.contains(irisProgram)) {
			this.programsToClear.add(irisProgram);
		}
	}

	@Inject(method = "submitRenderPass", at = @At("HEAD"))
	private void iris$clearState(CallbackInfo ci) {
		programsToClear.forEach(IrisProgram::iris$clearState);
		programsToClear.clear();
	}

	@ModifyArg(method = "executeDraws", index = 0, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;nglMultiDrawElementsBaseVertex(IJIJIJ)V"))
	private int iris$terrainTessShaderCompat(int mode) {
		if (mode == GL43C.GL_TRIANGLES && ImmediateState.usingTessellation) {
			mode = GL43C.GL_PATCHES;
		}
		return mode;
	}

	@ModifyArg(method = "executeDraw", index = 0, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;glDrawElementsInstancedBaseVertex(IIIJII)V"))
	private int iris$entityTessShaderCompat(int mode) {
		if (mode == GL43C.GL_TRIANGLES && ImmediateState.usingTessellation) {
			mode = GL43C.GL_PATCHES;
		}
		return mode;
	}
}
