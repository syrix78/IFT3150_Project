/**
 * Written by:	Lucas HORNUNG 
 * 
 */

package com.yourorganization.maven_sample;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
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
import com.github.javaparser.ast.expr.ClassExpr;
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
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
	
	TypeSolver typeSolver = new ReflectionTypeSolver();
	JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);

	

	
	private String filePath = new String();
	public CompilationUnit cu = null;
	
	FileWriter mutationLogFile = null;
	
	
	ArrayList<SwapMutation> swaps = new ArrayList<SwapMutation>();
	
	private String callsDictPath = "calls.dict";
	
	// ------------------------------------------------------------------------------
	// 							CONSTRUCTOR
	// ------------------------------------------------------------------------------
	/**
	 * Constructor
	 * @param filePath
	 */
	public Parser(String filePath) {
		//cu = sourceRoot.parse("", filePath);
		this.filePath = filePath;
		
		StaticJavaParser.getConfiguration().setSymbolResolver(this.symbolSolver);
		try {
			cu = StaticJavaParser.parse(new File(filePath));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			String fileName = "output/" + cu.getStorage().get().getFileName() + "_mutationLog.csv";
			this.mutationLogFile = new FileWriter(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	//------------------------------------------------------------------------------
	//                        METHODS
	//------------------------------------------------------------------------------
	
	public CompilationUnit visitStatements(ArrayList<MethodCallExpr> allowedCalls) throws IOException {
		
		 //cu.findAll(MethodCallExpr.class).forEach(mce ->
		 //mce.resolve().getSignature()));
		
		
		ArrayList<MethodCallExpr> allowedCallsTest = (ArrayList<MethodCallExpr>) allowedCalls.clone();
		
	
		ArrayList<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
	
		removeComments(cu);
		
		
		cu.accept(new ModifierVisitor<ArrayList<MethodDeclaration>>() {
			
			@Override
			public MethodDeclaration visit(MethodDeclaration n, ArrayList<MethodDeclaration> arg) {
					
				// Gets method name
				String name = n.toString() ;
				MethodDeclaration copyOfN = n;
				
				
				
				MethodDeclaration currentMethod = n.clone();
				ArrayList<MethodCallExpr> calls;
				try {
					calls = visitCalls(currentMethod, allowedCalls);
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
		this.logMutations();
		return cu;
	
	}
	
	public ArrayList<MethodCallExpr> visitCalls(MethodDeclaration m, ArrayList<MethodCallExpr> allowedCalls) throws IOException {
		
		ArrayList<MethodCallExpr> calls = new ArrayList<MethodCallExpr>();
		
		m.accept(new GenericVisitorAdapter<Integer, ArrayList<MethodCallExpr>>() {
			
			@Override
			public Integer visit(MethodCallExpr n, ArrayList<MethodCallExpr> arg) {
			
					// Gets method name
					String name = n.getNameAsString();
					
					
					if(allowedCalls.contains(n)) {
						calls.add(n);	
					}
					else {
						//Used to disable the API call check feature
						//calls.add(n);
					}
		
					return super.visit(n, arg);
	
			}
	
		}, calls);
	
		return calls;
	
	}
	
	public ArrayList<MethodCallExpr> getAllowedCalls() throws IOException {
		
		ArrayList<String> APICalls = this.getAPICalls();
		
		ArrayList<MethodCallExpr> calls = new ArrayList<MethodCallExpr>();
		
		this.cu.accept(new GenericVisitorAdapter<Integer, ArrayList<MethodCallExpr>>() {
			
			@Override
			public Integer visit(MethodCallExpr n, ArrayList<MethodCallExpr> arg) {
			
					// Gets method name
					String name = n.getNameAsString();
					try {
						String signature = n.resolve().getQualifiedSignature();
						String APICall = signature.substring(0, signature.indexOf("." + name + "(")) + "#" + name;
						if(APICalls.contains(APICall)) {
							calls.add(n.clone());
						}
					}catch(UnsolvedSymbolException e) {
						
					}
		
					return super.visit(n, arg);
	
			}
	
		}, calls);
	
		return calls;
	
	}
	
	public MethodDeclaration methodMutator(MethodDeclaration m, ArrayList<MethodCallExpr> calls) throws IOException {
		
		ArrayList<MethodCallExpr> mutantCalls = (ArrayList<MethodCallExpr>) calls.clone();
		MethodDeclaration method = m;
		
		ArrayList<SwapMutation> swaps = new ArrayList<SwapMutation>();
		
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
						

						SwapMutation callSwap = new SwapMutation(cu.getStorage().get().getFileName(), n, mutant);
						swaps.add(callSwap);
					
						
						return mutant;
					}
					else {
						return n;
					}
					
					

			}
	
		}, method);
		
		this.swaps.addAll(swaps);
	
		return method;
	
	}
	
	private void logMutations() {
		// Our example data

		FileWriter csvWriter = this.mutationLogFile;
		try {
			csvWriter.append("File Name");
			csvWriter.append(",");
			csvWriter.append("Edited Line Number");
			csvWriter.append(",");
			csvWriter.append("Edited Call");
			csvWriter.append(",");
			csvWriter.append("Swaped Line Number");
			csvWriter.append(",");
			csvWriter.append("New Call");
			csvWriter.append("\n");
		
			this.swaps.forEach((swap) ->{
				String lineContent = String.format("%s, %s, %s, %s, %s", swap.getJavaFile(), swap.getEditedLine(), swap.getEditedMethod(), swap.getSwapedLineNumber(), swap.getNewCall());
				try {
					csvWriter.append(lineContent);
					csvWriter.append("\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			});
			
			
			csvWriter.flush();
			csvWriter.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ArrayList<String> getAPICalls() {
		
		ArrayList<String> APICalls = new ArrayList<String>();
		
		//https://www.javatpoint.com/how-to-read-file-line-by-line-in-java
		try  {  
			File file=new File(this.callsDictPath);    //creates a new file instance  
			FileReader fr=new FileReader(file);   //reads the file  
			BufferedReader br=new BufferedReader(fr);  //creates a buffering character input stream  
			String line;  
		while((line=br.readLine())!=null){  
			APICalls.add(line.replace(" ", ""));      
		}  
			fr.close();    //closes the stream and release the resources  
			 
		}catch(IOException e)  {  
			e.printStackTrace();  
		}
		
		return APICalls;
	}
	
	// https://stackoverflow.com/questions/31828851/how-to-write-a-java-program-to-filter-all-commented-lines-and-print-only-java-co
	static void removeComments(Node node) {
		for (Comment child : node.getAllContainedComments()) {
			child.remove();
		}
	}
	
	

}
