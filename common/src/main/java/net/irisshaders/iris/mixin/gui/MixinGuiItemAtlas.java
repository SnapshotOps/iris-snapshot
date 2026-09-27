package net.irisshaders.iris.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.irisshaders.iris.gl.state.GlStateManager;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL45C;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiItemAtlas.class)
public class MixinGuiItemAtlas {
	@WrapMethod(method = "drawToSlot")
	private void iris$restoreReverseZForItemAtlas(int x, int y, boolean clear, ItemStackRenderState state, Operation<Void> original) {
		boolean previousBypass = ImmediateState.bypass;
		boolean previousRenderingLevel = ImmediateState.isRenderingLevel;
		ImmediateState.bypass = true;
		ImmediateState.isRenderingLevel = false;
		DepthColorStorage.unlockDepthColor();
		BlendModeOverride.restore();
		GlStateManager._glClipControl(GL20C.GL_LOWER_LEFT, GL45C.GL_ZERO_TO_ONE);
		GlStateManager._disableCull();
		try {
			original.call(x, y, clear, state);
		} finally {
			GlStateManager._glClipControl(GL20C.GL_LOWER_LEFT, GL45C.GL_ZERO_TO_ONE);
			ImmediateState.bypass = previousBypass;
			ImmediateState.isRenderingLevel = previousRenderingLevel;
		}
	}
}
