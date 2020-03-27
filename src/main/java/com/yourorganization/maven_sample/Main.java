/**
 * Written by:	Lucas HORNUNG 
 */

package com.yourorganization.maven_sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.math.RoundingMode;
import java.io.IOException;
import java.util.ArrayList;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.yourorganization.maven_sample.*;

public class Main {
	
	// ------------------------------------------------------------------------------
	// 								VARIABLES
	// ------------------------------------------------------------------------------

		
	public static String pathToExplore;
	
	// ------------------------------------------------------------------------------
	// 									MAIN
	// ------------------------------------------------------------------------------
	public static void main(String[] args) throws Exception {
		pathToExplore = "C:\\Users\\Lucas\\Documents\\UDEM 2019-2020\\HIV 2020\\IFT3150\\jtar\\src\\main\\java\\org\\kamranzafar\\jtar";
		searchThrough(new File(pathToExplore));
		
	}
	
public static void searchThrough(File node) throws Exception{
		
		Parser samp;
		String pathNode = node.getAbsolutePath();
		
		//For tests only
		ArrayList<String> allowedCalls = new ArrayList<String>();
		allowedCalls.add("getOctalBytes");
		allowedCalls.add("arraycopy");
		
		if(pathNode.substring(pathNode.length()-5, pathNode.length()).equals(".java")) {
			
			samp = new Parser(pathNode);
			CompilationUnit old = samp.cu.clone();
			CompilationUnit newCu = samp.visitStatements(allowedCalls);
			int a = 0;
				
		}
		
		if(node.isDirectory()){
			String[] subNode = node.list();
			for(String fileName : subNode){
				searchThrough(new File(node, fileName));
			}
		}
		
	}

} 
