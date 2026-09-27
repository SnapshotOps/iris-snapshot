package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlStateManager;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager_FramebufferBinding {
	@Inject(method = "_activeTexture", at = @At("HEAD"), remap = false)
	private void iris$checkActiveTexture(int i, CallbackInfo ci) {
		int tex = i - GL46C.GL_TEXTURE0;
		if (tex < 0 || tex > 128) throw new IllegalArgumentException("Texture " + tex + " out of range");
	}
}
