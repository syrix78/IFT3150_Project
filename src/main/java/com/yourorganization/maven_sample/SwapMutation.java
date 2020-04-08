package com.yourorganization.maven_sample;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class SwapMutation extends Mutation {
	
	int swapedLineNumber = -1;
	String newCall = null;
	
	/*public SwapMutation(String filename, int editedLineNumber, int swapedLineNumber, String editedClass, String editedMethod, String newClass, String newMethod) {
		super(filename, editedLineNumber, editedClass, editedMethod);
		this.swapedLineNumber = swapedLineNumber;
		this.newClass = newClass;
		this.newMethod = newMethod;
	}*/
	
	public SwapMutation(String filename, MethodCallExpr oldLine, MethodCallExpr newLine) {
		super(filename, oldLine);
		this.swapedLineNumber = newLine.getRange().get().begin.line;
		this.newCall = newLine.getNameAsString();
	}

	public int getSwapedLineNumber() {
		return this.swapedLineNumber;
	}
	
	public String getNewCall() {
		return this.newCall;
	}

}
