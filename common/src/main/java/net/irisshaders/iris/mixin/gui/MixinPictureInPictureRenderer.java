package net.irisshaders.iris.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.irisshaders.iris.gl.state.GlStateManager;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL45C;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PictureInPictureRenderer.class)
public abstract class MixinPictureInPictureRenderer<T extends PictureInPictureRenderState> {
	@WrapMethod(method = "prepare")
	private void iris$restoreReverseZForPip(T state, GuiRenderState guiRenderState, FeatureRenderDispatcher featureRenderDispatcher, int guiScale, Operation<Void> original) {
		boolean previousBypass = ImmediateState.bypass;
		boolean previousRenderingLevel = ImmediateState.isRenderingLevel;
		ImmediateState.bypass = true;
		ImmediateState.isRenderingLevel = false;
		DepthColorStorage.unlockDepthColor();
		BlendModeOverride.restore();
		GlStateManager._glClipControl(GL20C.GL_LOWER_LEFT, GL45C.GL_ZERO_TO_ONE);
		try {
			original.call(state, guiRenderState, featureRenderDispatcher, guiScale);
		} finally {
			GlStateManager._glClipControl(GL20C.GL_LOWER_LEFT, GL45C.GL_ZERO_TO_ONE);
			ImmediateState.bypass = previousBypass;
			ImmediateState.isRenderingLevel = previousRenderingLevel;
		}
	}
}
