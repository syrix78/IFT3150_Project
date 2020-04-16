package com.yourorganization.maven_sample;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MutationProperties {
	
	private InputStream inputStream;
	private Properties prop = new Properties();
	
	private int allowSwapMutations=34;
	private int allowDeleteMutations=33;
	private int allowDuplicationMutations=33;
	private boolean allowInterproceduralMutations=true;
	
	public MutationProperties(String configFilePath) throws IOException{
		try {
			this.inputStream = getClass().getClassLoader().getResourceAsStream(configFilePath);
			
			if (this.inputStream != null) {
				this.prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + configFilePath + "' not found");
			}
			
			this.generateProperties();
			
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			this.inputStream.close();
		}
	}
	
	public int getAllowSwapMutations() {
		return allowSwapMutations;
	}

	public int getAllowDeleteMutations() {
		return allowDeleteMutations;
	}

	public int getAllowDuplicationMutations() {
		return allowDuplicationMutations;
	}

	public boolean isAllowInterproceduralMutations() {
		return allowInterproceduralMutations;
	}

	private void generateProperties() {
		this.allowDeleteMutations = Integer.parseInt(this.prop.getProperty("allowDeleteMutations")); 
		this.allowSwapMutations = Integer.parseInt(this.prop.getProperty("allowSwapMutations")); 
		this.allowDuplicationMutations = Integer.parseInt(this.prop.getProperty("allowDuplicationMutations")); 
		this.allowInterproceduralMutations = Boolean.parseBoolean(this.prop.getProperty("allowInterproceduralMutations")); 
		
	}
	
	
	
	
}
