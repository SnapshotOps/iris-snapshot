package net.irisshaders.iris.gl.state;

import com.mojang.renderpearl.backend.opengl.GlStateManager;

public final class GlStateManagerAccessor {
	private GlStateManagerAccessor() {}

	public static GlStateManager instance() {
		return net.irisshaders.iris.gl.state.GlStateManager.instance();
	}

	public static GlStateManager.BlendState getBLEND() {
		return instance().blend;
	}

	public static boolean[] getBLEND_ENABLE() {
		return instance().blendEnable;
	}

	public static int[] getCOLOR_MASK() {
		return instance().COLOR_MASK;
	}

	public static GlStateManager.DepthState getDEPTH() {
		return instance().depth;
	}

	public static int getActiveTexture() {
		return instance().activeTexture;
	}

	public static GlStateManager.TextureState[] getTEXTURES() {
		return instance().TEXTURES;
	}
}