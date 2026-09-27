package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.irisshaders.iris.gl.blending.BlendModeStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GlStateManager.class, remap = false)
public class MixinGlStateManager_BlendOverride {
	@Inject(method = "_disableBlend", at = @At("HEAD"), cancellable = true)
	private void iris$blendDisableLock(int index, CallbackInfo ci) {
		if (BlendModeStorage.isBlendLocked()) {
			BlendModeStorage.deferBlendModeToggle(index, false);
			ci.cancel();
		}
	}

	@Inject(method = "_enableBlend", at = @At("HEAD"), cancellable = true)
	private void iris$blendEnableLock(int index, CallbackInfo ci) {
		if (BlendModeStorage.isBlendLocked()) {
			BlendModeStorage.deferBlendModeToggle(index, true);
			ci.cancel();
		}
	}

	@Shadow
	@Final
	private GlStateManager.BlendState blend;

	@Inject(method = "_blendFuncSeparate", at = @At("HEAD"), cancellable = true)
	private void iris$blendFuncSeparateLock(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha, CallbackInfo ci) {
		if (BlendModeStorage.isBlendLocked()) {
			BlendModeStorage.deferBlendFunc(srcRgb, dstRgb, srcAlpha, dstAlpha);
			ci.cancel();
		} else if (BlendModeStorage.isBlendUnknown()) {
			this.blend.srcRgb = srcRgb;
			this.blend.dstRgb = dstRgb;
			this.blend.srcAlpha = srcAlpha;
			this.blend.dstAlpha = dstAlpha;
			org.lwjgl.opengl.GL14C.glBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
			ci.cancel();
		}
	}
}
