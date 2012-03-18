package org.newdawn.slick.shader;

import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * ShaderManager singleton class
 * Based on work by Ciano [http://slick.javaunlimited.net/viewtopic.php?f=3&t=3937]
 * 
 * @author liamzebedee
 */
public class ShaderManager {
	/** Enum for representing the support type we have for shaders on the platform, if any */
	enum ShaderSupportType { 
		/** No support whatsoever */
		none,
		/** Support through ARB Extensions */
		arb,
		/** Support through OpenGL2.0+ */
		opengl2  }
	/** Shader support type. Defaults to none until ShaderManager is initialised */
	public ShaderSupportType shaderSupportType = ShaderSupportType.none; 
	/** ShaderManager singleton instance */
	private static ShaderManager shaderManager;
	
	/**
	 * Get's the ShaderManager instance if it exists, and creates one if necessary
	 * 
	 * @author liamzebedee
	 * @return Global ShaderManager instanceS
	 */
	public static ShaderManager getShaderManager() {
		if(shaderManager == null) shaderManager = new ShaderManager();
		return shaderManager;
	}
	
	private ShaderManager() {
		if (getGLVersion() >= 2) {
			System.out.println("We have shader support through opengl 2.0!");
			shaderSupportType = ShaderSupportType.opengl2;
		}
		else if(ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB)>0) {
			System.out.println("We have shader support through ARB extensions!");
			shaderSupportType = ShaderSupportType.arb;
		}
		else {
			shaderSupportType = ShaderSupportType.none;
			System.out.println("No shader support on this platform!");
		}
	}

	/**
	 * @return Returns a list with the GL version numbers. I.e. [2, 1, 0].
	 * @author liamzebedee
	 */
	public int getGLVersion() {
		return Integer.parseInt(Character.toString(GL11.glGetString(GL11.GL_VERSION).charAt(0)));
	}

	/**
	 * Creates a new shader program
	 * This method is not the advised use method. It's better to use the Shader class.
	 * 
	 * @author liamzebedee
	 * @param vSource The vertex shader source, as a String
	 * @param fSource The fragment shader source, as a String
	 * @return The id of the shader program
	 */
	public int createShader(String vSource, String fSource) {
		switch(shaderSupportType){
		case opengl2:
			return createShaderOpenGL_L2(vSource, fSource);
		case arb:
			return createShaderARB(vSource, fSource);
		default:
			System.out.println("Can't create program. No shader support!");
			return -1;
		}
	}
	
	/**
	 * Helper method for creating OpenGL2 shader programs
	 * 
	 * @author liamzebedee
	 * @return The shader ID
	 */
	private int createShaderOpenGL_L2(String vertex_source, String fragment_source) {
		//# Create shaders
		Integer v_shader = null;
		if (vertex_source!=null) {
			v_shader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
			GL20.glShaderSource(v_shader, vertex_source);
			GL20.glCompileShader(v_shader);
		}

		Integer f_shader = null;
		if (fragment_source!=null) {
			f_shader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
			GL20.glShaderSource(f_shader, fragment_source);
			GL20.glCompileShader(f_shader);
		}
		int program = GL20.glCreateProgram();

		//# Attach the shaders
		if (v_shader!=null) GL20.glAttachShader(program, v_shader);
		if (f_shader!=null) GL20.glAttachShader(program, f_shader);

		//# Link the program
		GL20.glLinkProgram(program);
		return program;
	}
	
	/**
	 * Helper method for creating ARB shader programs
	 * 
	 * @author liamzebedee
	 * @return The shader ID
	 */
	private int createShaderARB(String vertex_source, String fragment_source) {
		//# Create shaders
		Integer v_shader = null;
		if (vertex_source!=null) {
			v_shader = ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
			ARBShaderObjects.glShaderSourceARB(v_shader, vertex_source);
			ARBShaderObjects.glCompileShaderARB(v_shader);
		}

		Integer f_shader = null;
		if (fragment_source!=null) {
			f_shader = ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
			ARBShaderObjects.glShaderSourceARB(f_shader, fragment_source);
			ARBShaderObjects.glCompileShaderARB(f_shader);
		}

		int program = ARBShaderObjects.glCreateProgramObjectARB();

		//# Attach the shaders
		if (v_shader!=null) ARBShaderObjects.glAttachObjectARB(program, v_shader);
		if (f_shader!=null) ARBShaderObjects.glAttachObjectARB(program, f_shader);

		//# Link the program
		ARBShaderObjects.glLinkProgramARB(program);

		return program;
	}
	
	/**
	 * Enables a shader program
	 * 
	 * @author liamzebedee
	 * @param program The ID of the program
	 */
	public void enableProgram(int program) {
		switch(shaderSupportType){
		case opengl2:
			GL20.glUseProgram(program);
			break;
		case arb:
			ARBShaderObjects.glUseProgramObjectARB(program);
			break;
		default:
			System.out.println("Can't enable program. No shader support!");
			break;
		}
	}

	public void setUniform1i(int program, String name, int value) {
		int loc;
		switch(shaderSupportType){
		case opengl2:
			loc = GL20.glGetUniformLocation(program, name);
			GL20.glUniform1i(loc, value);
			break;
		case arb:
			loc = ARBShaderObjects.glGetUniformLocationARB(program, name);
			ARBShaderObjects.glUniform1iARB(loc, value);
			break;
		default:
			System.out.println("Cannot call this without shader support!");
			break;
		}
	}

	public void setUniform1f(int program, String name, float value) {
		int loc;
		switch(shaderSupportType){
		case opengl2:
			loc = GL20.glGetUniformLocation(program, name);
			GL20.glUniform1f(loc, value);
			break;
		case arb:
			loc = ARBShaderObjects.glGetUniformLocationARB(program, name);
			ARBShaderObjects.glUniform1fARB(loc, value);
			break;
		default:
			System.out.println("Cannot call this without shader support!");
			break;
		}
	}

	public void setUniformf(int program, String name, float values[]) {
		int loc;
		switch(shaderSupportType){
		case opengl2:
			loc = GL20.glGetUniformLocation(program, name);
			if(values.length == 2) GL20.glUniform2f(loc, values[0], values[1]);
			else if(values.length == 3) GL20.glUniform3f(loc, values[0], values[1], values[2]);
			else if(values.length == 4) GL20.glUniform4f(loc, values[0], values[1], values[2], values[3]);
			break;
		case arb:
			loc = ARBShaderObjects.glGetUniformLocationARB(program, name);
			if(values.length == 2) ARBShaderObjects.glUniform2fARB(loc, values[0], values[1]);
			else if(values.length == 3) ARBShaderObjects.glUniform3fARB(loc, values[0], values[1], values[2]);
			else if(values.length == 4) ARBShaderObjects.glUniform4fARB(loc, values[0], values[1], values[2], values[3]);
			break;
		default:
			System.out.println("Cannot call this without shader support!");
		}
	}


	/*
	 switch(shaderSupportType){
		case opengl2:
			break;
		case arb:
			break;
		default:
			break;
		}
	 */
}
