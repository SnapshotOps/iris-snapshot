package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.frontend.FrontendCommandEncoder;
import net.irisshaders.iris.gl.state.GlStateManager;
import net.irisshaders.iris.vertices.ImmediateState;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL45C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FrontendCommandEncoder.class)
public class MixinFrontendCommandEncoder {
	@Inject(method = "createRenderPass(Lcom/mojang/renderpearl/api/commands/RenderPassDescriptor;)Lcom/mojang/renderpearl/api/commands/RenderPass;", at = @At("HEAD"))
	private void iris$ensureReverseZOnBypassRenderPass(RenderPassDescriptor descriptor, CallbackInfoReturnable<?> cir) {
		if (ImmediateState.bypass || !ImmediateState.isRenderingLevel) {
			GlStateManager._glClipControl(GL20C.GL_LOWER_LEFT, GL45C.GL_ZERO_TO_ONE);
		}
	}
}
