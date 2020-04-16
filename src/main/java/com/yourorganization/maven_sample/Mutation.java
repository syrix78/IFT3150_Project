package com.yourorganization.maven_sample;

import com.github.javaparser.ast.expr.MethodCallExpr;

public class Mutation {
	
	String javaFile = null;
	int editedLineNumber = -1;
	String editedCall = null;
	String mutationType = null;
	int swapedLineNumber = -1;
	String newCall = null;

	
	public Mutation(String filename, String mutationType, MethodCallExpr oldLine, MethodCallExpr newLine) {
		this.javaFile = filename;
		this.mutationType = mutationType;
		this.editedLineNumber = oldLine.getRange().get().begin.line;
		this.editedCall = oldLine.getNameAsString();
		this.swapedLineNumber = newLine.getRange().get().begin.line;
		this.newCall = newLine.getNameAsString();
	}

	public int getSwapedLineNumber() {
		return this.swapedLineNumber;
	}
	
	public String getNewCall() {
		return this.newCall;
	}
	
	public String getMutationType() {
		return this.mutationType;
	}

	public String getJavaFile() {
		return javaFile;
	}

	public int getEditedLine() {
		return editedLineNumber;
	}
	
	public String getEditedMethod() {
		return editedCall;
	}
}
