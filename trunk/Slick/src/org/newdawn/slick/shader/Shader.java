package org.newdawn.slick.shader;

/**
 * A shader object
 * Based on work by Ciano [http://slick.javaunlimited.net/viewtopic.php?f=3&t=3937]
 * 
 * @author liamzebedee
 */
public class Shader {
	/** ID of the shader program*/
    public int programID;
    
    
    /**
     * @author liamzebedee
     * @param vShader The vertex shader source, as a String
     * @param fShader The fragment shader source, as a String
     */
    public Shader(String vShader, String fShader) {
        programID = ShaderManager.getShaderManager().createShader(vShader, fShader);
    }
    
    /**
     * Sets a uniform float array
     * 
     * @author liamzebedee
     * @param name Name of variable
     * @param values Float array values
     */
    public void setUniformf(String name, float values[]) {
    	ShaderManager.getShaderManager().setUniformf(programID, name, values);
    }
    
    /**
     * Sets a uniform float variable
     * 
     * @author liamzebedee
     * @param name Name of variable
     * @param value Float value
     */
    public void setUniform1f(String name, float value) {
    	ShaderManager.getShaderManager().setUniform1f(programID, name, value);
    }
    
    /**
     * Enables this shader
     * 
     * @author liamzebedee
     */
    public void enable() {
    	ShaderManager.getShaderManager().enableProgram(programID);
    }
    
    /**
     * Disables this shader
     * 
     * @author liamzebedee
     */
    public void disable() {
    	ShaderManager.getShaderManager().enableProgram(0);
    }
}
