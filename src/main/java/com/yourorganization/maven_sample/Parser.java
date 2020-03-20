/**
 * Written by:	Lucas HORNUNG 
 * 
 */

package com.yourorganization.maven_sample;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Some code that uses JavaParser.
 */
public class Parser {

	// ------------------------------------------------------------------------------
	// 							VARIABLES
	// ------------------------------------------------------------------------------
	
	JavaParser javaparser = new JavaParser();

	SourceRoot sourceRoot = new SourceRoot(
			CodeGenerationUtils.mavenModuleRoot(Parser.class).resolve("."));
	
	private String filePath = new String();
	private CompilationUnit cu = null;
	
	
	
	// ------------------------------------------------------------------------------
	// 							CONSTRUCTOR
	// ------------------------------------------------------------------------------
	/**
	 * Constructor
	 * @param filePath
	 */
	public Parser(String filePath) {
		cu = sourceRoot.parse("", filePath);
		this.filePath = filePath;
	}
	
	//------------------------------------------------------------------------------
	//                        METHODS
	//------------------------------------------------------------------------------
	
	public CompilationUnit visitStatements() throws IOException {
	
		ArrayList<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
	
		CompilationUnit cu_no_comments2 = this.sourceRoot.parse("", this.filePath);
		removeComments(cu_no_comments2);
		
		cu_no_comments2.accept(new ModifierVisitor<ArrayList<MethodDeclaration>>() {
			
			@Override
			public MethodDeclaration visit(MethodDeclaration n, ArrayList<MethodDeclaration> arg) {
					
				// Gets method name
				String name = n.toString() ;
				MethodDeclaration copyOfN = n;
				
				
				
				MethodDeclaration currentMethod = n.clone();
				ArrayList<MethodCallExpr> calls;
				try {
					calls = visitCalls(currentMethod);
					if(calls.size() > 1) {
						methodMutator(currentMethod, calls);
					}
					int r = 0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
						
	
				return currentMethod;
					
					

			}
	
		}, methods);
	
		return cu_no_comments2;
	
	}
	
	public ArrayList<MethodCallExpr> visitCalls(MethodDeclaration m) throws IOException {
		
		ArrayList<MethodCallExpr> calls = new ArrayList<MethodCallExpr>();
	
		m.accept(new GenericVisitorAdapter<Integer, ArrayList<MethodCallExpr>>() {
			
			@Override
			public Integer visit(MethodCallExpr n, ArrayList<MethodCallExpr> arg) {
			
					// Gets method name
					String name = n.toString() ;
					calls.add(n);		
		
					return super.visit(n, arg);
	
			}
	
		}, calls);
	
		return calls;
	
	}
	
	public MethodDeclaration methodMutator(MethodDeclaration m, ArrayList<MethodCallExpr> calls) throws IOException {
		
		ArrayList<MethodCallExpr> mutantCalls = (ArrayList<MethodCallExpr>) calls.clone();
		MethodDeclaration method = m;
		
		Integer[] blank = null;
		
		method.accept(new ModifierVisitor<MethodDeclaration>() {
			
			@Override
			public MethodCallExpr visit(MethodCallExpr n, MethodDeclaration arg) {
					
					MethodCallExpr methodCopy = n;
				
					// Gets method name
					String name = n.toString() ;
					
					ArrayList<MethodCallExpr> newCalls = (ArrayList<MethodCallExpr>) calls.clone();
					newCalls.remove(n);
					
					if(newCalls.size() > 0) {
						int randomIndex = (int) Math.random() * newCalls.size();
						MethodCallExpr mutant = newCalls.get(randomIndex);
						mutantCalls.remove(mutant);
						return mutant;
					}
					else {
						return n;
					}
					
					

			}
	
		}, method);
		
	
		return method;
	
	}
	
	// https://stackoverflow.com/questions/31828851/how-to-write-a-java-program-to-filter-all-commented-lines-and-print-only-java-co
	static void removeComments(Node node) {
		for (Comment child : node.getAllContainedComments()) {
			child.remove();
		}
	}
	
	

}
