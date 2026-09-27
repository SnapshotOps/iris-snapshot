package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiRenderer.class)
public class MixinGuiRenderer {
	@WrapMethod(method = "render")
	private void iris$bypassWorldRenderingState(Operation<Void> original) {
		boolean previous = ImmediateState.bypass;
		boolean previousRenderingLevel = ImmediateState.isRenderingLevel;
		ImmediateState.bypass = true;
		ImmediateState.isRenderingLevel = false;
		net.irisshaders.iris.gl.state.GlStateManager._glClipControl(org.lwjgl.opengl.GL20C.GL_LOWER_LEFT, org.lwjgl.opengl.GL45C.GL_ZERO_TO_ONE);
		try {
			original.call();
		} finally {
			net.irisshaders.iris.gl.state.GlStateManager._glClipControl(org.lwjgl.opengl.GL20C.GL_LOWER_LEFT, org.lwjgl.opengl.GL45C.GL_ZERO_TO_ONE);
			ImmediateState.bypass = previous;
			ImmediateState.isRenderingLevel = previousRenderingLevel;
		}
	}
}
