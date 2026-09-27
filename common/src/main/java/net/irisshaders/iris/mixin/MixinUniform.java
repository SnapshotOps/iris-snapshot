package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlProgram;
import net.irisshaders.iris.gl.state.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlProgram.class)
public class MixinUniform {
	@Redirect(method = "setupUniforms", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL20;glGetUniformLocation(ILjava/lang/CharSequence;)I"), require = 0, remap = false)
	private int iris$glGetUniformLocation(int programId, CharSequence name) {
		int location = GlStateManager._glGetUniformLocation(programId, name);

		if (location == -1 && (name.equals("Sampler0") || name.equals("u_BlockTex"))) {
			location = GlStateManager._glGetUniformLocation(programId, "tex");

			if (location == -1) {
				location = GlStateManager._glGetUniformLocation(programId, "gtexture");

				if (location == -1) {
					location = GlStateManager._glGetUniformLocation(programId, "texture");
				}
			}
		}

		if (location == -1 && name.equals("Sampler1")) {
			location = GlStateManager._glGetUniformLocation(programId, "iris_overlay");
		}

		if (location == -1 && (name.equals("Sampler2") || name.equals("u_LightTex"))) {
			location = GlStateManager._glGetUniformLocation(programId, "lightmap");
		}

		return location;
	}
}
