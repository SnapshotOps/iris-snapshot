package net.irisshaders.iris.gl.state;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import net.irisshaders.iris.mixin.GpuDeviceAccessor;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class GlStateManager {
	private GlStateManager() {}

	public static com.mojang.renderpearl.backend.opengl.GlStateManager instance() {
		return ((GlDevice) ((GpuDeviceAccessor) RenderSystem.getDevice()).getBackend()).stateManager();
	}

	public static void _activeTexture(int texture) {
		instance()._activeTexture(texture);
	}

	public static void _bindTexture(int texture) {
		instance()._bindTexture(texture);
	}

	public static void _deleteTexture(int texture) {
		instance()._deleteTexture(texture);
	}

	public static int _genTexture() {
		return instance()._genTexture();
	}

	public static void _colorMask(int mask) {
		instance()._colorMask(mask);
	}

	public static void _colorMask(int index, int mask) {
		instance()._colorMask(index, mask);
	}

	public static void _depthMask(boolean flag) {
		instance()._depthMask(flag);
	}

	public static void _depthFunc(int func) {
		instance()._depthFunc(func);
	}

	public static void _enableDepthTest() {
		instance()._enableDepthTest();
	}

	public static void _disableDepthTest() {
		instance()._disableDepthTest();
	}

	public static void _enableCull() {
		instance()._enableCull();
	}

	public static void _disableCull() {
		instance()._disableCull();
	}

	public static void _enableBlend(int index) {
		instance()._enableBlend(index);
	}

	public static void _disableBlend(int index) {
		instance()._disableBlend(index);
	}

	public static void _blendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
		instance()._blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
	}

	public static void _blendEquationSeparate(int modeRgb, int modeAlpha) {
		instance()._blendEquationSeparate(modeRgb, modeAlpha);
	}

	public static void _enablePolygonOffset() {
		instance()._enablePolygonOffset();
	}

	public static void _disablePolygonOffset() {
		instance()._disablePolygonOffset();
	}

	public static void _polygonOffset(float factor, float units) {
		instance()._polygonOffset(factor, units);
	}

	public static void _glBindFramebuffer(int target, int framebuffer) {
		instance()._glBindFramebuffer(target, framebuffer);
	}

	public static int getFrameBuffer(int target) {
		return instance().getFrameBuffer(target);
	}

	public static void _glDeleteFramebuffers(int framebuffer) {
		instance()._glDeleteFramebuffers(framebuffer);
	}

	public static void _enableScissorTest() {
		instance()._enableScissorTest();
	}

	public static void _disableScissorTest() {
		instance()._disableScissorTest();
	}

	public static void _scissorBox(int x, int y, int width, int height) {
		GL33C.glScissor(x, y, width, height);
	}

	public static void _viewport(int x, int y, int width, int height) {
		GL33C.glViewport(x, y, width, height);
	}

	public static int _glGenBuffers() {
		return com.mojang.renderpearl.backend.opengl.GlStateManager._glGenBuffers();
	}

	public static void _glDeleteBuffers(int buffer) {
		com.mojang.renderpearl.backend.opengl.GlStateManager._glDeleteBuffers(buffer);
	}

	public static void _glBindBuffer(int target, int buffer) {
		GL15C.glBindBuffer(target, buffer);
	}

	public static void _glBufferSubData(int target, long offset, ByteBuffer data) {
		GL15C.glBufferSubData(target, offset, data);
	}

	public static int _glGenVertexArrays() {
		return GL30C.glGenVertexArrays();
	}

	public static void _glBindVertexArray(int array) {
		GL30C.glBindVertexArray(array);
	}

	public static void _drawElements(int mode, int count, int type, long indices) {
		GL11C.glDrawElements(mode, count, type, indices);
	}

	public static void _clear(int mask) {
		com.mojang.renderpearl.backend.opengl.GlStateManager._clear(mask);
	}

	public static void _clearBuffer(int buffer, Vector4fc value) {
		com.mojang.renderpearl.backend.opengl.GlStateManager._clearBuffer(buffer, value);
	}

	public static void _clearBuffer(double depth) {
		com.mojang.renderpearl.backend.opengl.GlStateManager._clearBuffer(depth);
	}

	public static void clearGlErrors() {
		com.mojang.renderpearl.backend.opengl.GlStateManager.clearGlErrors();
	}

	public static void glShaderSource(int shader, String source) {
		instance().glShaderSource(shader, source);
	}

	public static int _glGetUniformLocation(int program, CharSequence name) {
		return GL20C.glGetUniformLocation(program, name);
	}

	public static void _glUniform1i(int location, int value) {
		GL20C.glUniform1i(location, value);
	}

	public static int glCreateProgram() {
		return GL20C.glCreateProgram();
	}

	public static void glLinkProgram(int program) {
		GL20C.glLinkProgram(program);
	}

	public static void glDeleteProgram(int program) {
		GL20C.glDeleteProgram(program);
	}

	public static void glAttachShader(int program, int shader) {
		GL20C.glAttachShader(program, shader);
	}

	public static void glDeleteShader(int shader) {
		GL20C.glDeleteShader(shader);
	}

	public static int glCreateShader(int type) {
		return GL20C.glCreateShader(type);
	}

	public static void glCompileShader(int shader) {
		GL20C.glCompileShader(shader);
	}

	public static int glGetShaderi(int shader, int pname) {
		return GL20C.glGetShaderi(shader, pname);
	}

	public static int glGetProgrami(int program, int pname) {
		return GL20C.glGetProgrami(program, pname);
	}

	public static String glGetShaderInfoLog(int shader, int maxLength) {
		return GL20C.glGetShaderInfoLog(shader, maxLength);
	}

	public static String glGetProgramInfoLog(int program, int maxLength) {
		return GL20C.glGetProgramInfoLog(program, maxLength);
	}

	public static void _glUseProgram(int program) {
		GL20C.glUseProgram(program);
	}

	public static void _pixelStore(int pname, int param) {
		GL11C.glPixelStorei(pname, param);
	}

	public static int _getInteger(int pname) {
		return GL11C.glGetInteger(pname);
	}

	public static String _getString(int name) {
		return GL11C.glGetString(name);
	}

	public static int _getTexLevelParameter(int target, int level, int pname) {
		return GL11C.glGetTexLevelParameteri(target, level, pname);
	}

	public static int glGenFramebuffers() {
		return GL30C.glGenFramebuffers();
	}

	public static void _glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
		GL30C.glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	public static void _glBindAttribLocation(int program, int index, CharSequence name) {
		GL20C.glBindAttribLocation(program, index, name);
	}

	public static void _texImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
		GL11C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}

	public static void _texImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, IntBuffer pixels) {
		GL11C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}

	public static void glBlendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
		_blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
	}

	public static void _glClipControl(int origin, int depth) {
		if (org.lwjgl.opengl.GL.getCapabilities().GL_ARB_clip_control) {
			org.lwjgl.opengl.ARBClipControl.glClipControl(origin, depth);
		} else {
			org.lwjgl.opengl.GL45C.glClipControl(origin, depth);
		}
	}
}
