package net.irisshaders.iris.mixin.texture;

import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.irisshaders.iris.pbr.TextureInfoCache;
import net.irisshaders.iris.pbr.TextureTracker;
import net.irisshaders.iris.pbr.texture.PBRTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
	@Inject(method = "_deleteTexture(I)V", at = @At("TAIL"), remap = false)
	private void iris$onDeleteTexture(int id, CallbackInfo ci) {
		iris$onDeleteTextureStatic(id);
	}

	@Unique
	private static void iris$onDeleteTextureStatic(int id) {
		TextureTracker.INSTANCE.onDeleteTexture(id);
		TextureInfoCache.INSTANCE.onDeleteTexture(id);
		PBRTextureManager.INSTANCE.onDeleteTexture(id);
	}
}
