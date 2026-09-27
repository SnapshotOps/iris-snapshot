package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager_DepthColorOverride {
	@Inject(method = "_colorMask(I)V", at = @At("HEAD"), cancellable = true, remap = false)
	private void iris$colorMaskLock(int writeMask, CallbackInfo ci) {
		if (DepthColorStorage.isDepthColorLocked()) {
			DepthColorStorage.deferColorMask(writeMask);
			ci.cancel();
		}
	}

	@Inject(method = "_colorMask(II)V", at = @At("HEAD"), cancellable = true, remap = false)
	private void iris$colorMaskLock(int index, int writeMask, CallbackInfo ci) {
		if (DepthColorStorage.isDepthColorLocked()) {
			DepthColorStorage.deferColorMask(index, writeMask);
			ci.cancel();
		}
	}

	@Inject(method = "_depthMask", at = @At("HEAD"), cancellable = true, remap = false)
	private void iris$depthMaskLock(boolean enable, CallbackInfo ci) {
		if (DepthColorStorage.isDepthColorLocked()) {
			DepthColorStorage.deferDepthEnable(enable);
			ci.cancel();
		}
	}
}
