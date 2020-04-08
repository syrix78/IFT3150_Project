package com.yourorganization.maven_sample;

import com.github.javaparser.ast.expr.MethodCallExpr;

public class Mutation {
	
	String javaFile = null;
	int editedLineNumber = -1;
	String editedCall = null;
	
	public Mutation(String filename, MethodCallExpr oldLine) {
		this.javaFile = filename;
		this.editedLineNumber = oldLine.getRange().get().begin.line;
		this.editedCall = oldLine.getNameAsString();
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
