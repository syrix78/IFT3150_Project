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
		//Checks if path was entered
		/*if(args.length == 0 || args[0] == null) {
			System.out.println("Please enter the path of the folder you would like to parse!");
			System.exit(1);
		}
		else {
			pathToExplore=args[0];
		}*/
		pathToExplore = "C:\\Users\\Lucas\\Documents\\UDEM 2019-2020\\HIV 2020\\IFT3150\\jtar\\src\\main\\java\\org\\kamranzafar\\jtar";
		searchThrough(new File(pathToExplore));
		
	}
	
public static void searchThrough(File node) throws Exception{
		
		Parser samp;
		String pathNode = node.getAbsolutePath();
		
		if(pathNode.substring(pathNode.length()-5, pathNode.length()).equals(".java")) {
			
			samp = new Parser(pathNode);
			CompilationUnit newCu = samp.visitStatements();

				
		}
		
		if(node.isDirectory()){
			String[] subNode = node.list();
			for(String fileName : subNode){
				searchThrough(new File(node, fileName));
			}
		}
		
	}

} 
