package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.device.GpuBackend;
import com.mojang.renderpearl.backend.opengl.GlBackend;
import net.irisshaders.iris.Iris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PreferredGraphicsApi;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft_PipelineManagement {
	@Redirect(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/PreferredGraphicsApi;getBackendsToTry()[Lcom/mojang/renderpearl/api/device/GpuBackend;"
		)
	)
	private GpuBackend[] iris$forceOpenGlBackend(PreferredGraphicsApi instance) {
		Iris.logger.info("Forcing OpenGL graphics backend for Iris shader pipeline compatibility.");
		return new GpuBackend[] { new GlBackend() };
	}

	@Inject(method = "clearClientLevel", at = @At("HEAD"))
	public void iris$trackLastDimensionOnLeave(Screen arg, CallbackInfo ci) {
		Iris.lastDimension = Iris.getCurrentDimension();
	}

	@Inject(method = "setLevel", at = @At("HEAD"))
	private void iris$trackLastDimensionOnLevelChange(ClientLevel clientLevel, CallbackInfo ci) {
		Iris.lastDimension = Iris.getCurrentDimension();
	}

	@Inject(method = "updateLevelInEngines", at = @At("HEAD"))
	private void iris$resetPipeline(@Nullable ClientLevel level, CallbackInfo ci) {
		if (Iris.getCurrentDimension() != Iris.lastDimension) {
			Iris.logger.info("Reloading pipeline on dimension change: " + Iris.lastDimension + " => " + Iris.getCurrentDimension());
			Iris.getPipelineManager().destroyPipeline();

			if (level != null) {
				Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());
			}
		}
	}
}