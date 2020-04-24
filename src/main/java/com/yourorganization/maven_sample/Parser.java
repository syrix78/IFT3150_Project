/**
 * Written by:	Lucas HORNUNG 
 * 
 */

package com.yourorganization.maven_sample;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
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
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
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
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
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
import java.util.Properties;

/**
 * Some code that uses JavaParser.
 */
public class Parser {

	// ------------------------------------------------------------------------------
	// 							VARIABLES
	// ------------------------------------------------------------------------------
	
	//TypeSolver typeSolver = new ReflectionTypeSolver();
	CombinedTypeSolver typeSolver = null;
	JavaSymbolSolver symbolSolver = null;

	//Config
	private InputStream inputStream;
	private Properties prop = new Properties();
	private int allowDeleteMutations;
	private int allowSwapMutations;
	private int allowDuplicationMutations;
	private int totalMutationProbabilities;
	private int allowNoMutations;
	private boolean allowInterproceduralMutations;
	
	private String filePath = new String();
	public CompilationUnit cu = null;
	
	FileWriter mutationLogFile = null;
	
	
	ArrayList<Mutation> mutations = new ArrayList<Mutation>();
	
	private String callsDictPath = "calls.dict";
	
	
	// ------------------------------------------------------------------------------
	// 							CONSTRUCTOR
	// ------------------------------------------------------------------------------
	/**
	 * Constructor
	 * @param filePath
	 * @throws IOException 
	 */
	public Parser(String filePath){
		//cu = sourceRoot.parse("", filePath);
		this.filePath = filePath;
		
		try {
			this.typeSolver = new CombinedTypeSolver( 
					  new ReflectionTypeSolver(),
					  JarTypeSolver.getJarTypeSolver("guava-r08.jar"),
					  JarTypeSolver.getJarTypeSolver("closure-compiler-r1043.jar")
					); 
			this.symbolSolver = new JavaSymbolSolver(typeSolver);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
			this.generateMutationProperties("config.properties");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//------------------------------------------------------------------------------
	//                        METHODS
	//------------------------------------------------------------------------------
	
	public CompilationUnit visitStatements(ArrayList<MethodCallExpr> allowedCalls) throws IOException {
		
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
					if(calls.size() > 0) {
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
		
		CombinedTypeSolver solve = this.typeSolver;
		
		
		this.cu.accept(new GenericVisitorAdapter<Integer, ArrayList<MethodCallExpr>>() {
			
			@Override
			public Integer visit(MethodCallExpr n, ArrayList<MethodCallExpr> arg) {
			
					// Gets method name
					String name = n.getNameAsString();
					try {
						
						String test2 = n.getTypeArguments().toString();;
						String test = n.resolve().getSignature();
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
		int allowDeleteMutations= this.allowDeleteMutations;
		int allowSwapMutations = this.allowSwapMutations;
		int allowDuplicationMutations = this.allowDuplicationMutations;
		int allowNoMutations = this.allowNoMutations;
		int totalMutations = this.totalMutationProbabilities;
		
		
		ArrayList<Mutation> mutations = new ArrayList<Mutation>();
		
		method.accept(new ModifierVisitor<MethodDeclaration>() {
			
			@Override
			public MethodCallExpr visit(MethodCallExpr n, MethodDeclaration arg) {
					
					MethodCallExpr methodCopy = n;
				
					// Gets method name
					String name = n.toString() ;
					
					
					ArrayList<MethodCallExpr> newCalls = (ArrayList<MethodCallExpr>) calls.clone();
					newCalls.remove(n);
					
					int randomMutation = 0;
					int swapMutation = allowSwapMutations;
					
					if(newCalls.size() <= 1) {
						randomMutation = (int) (Math.random() * (totalMutations - allowSwapMutations));
						swapMutation = 0;
					}
					else {
						randomMutation = (int) (Math.random() * totalMutations);
					}
					
					
					
					
					if(randomMutation < swapMutation) {
						
						int randomIndex = (int) (Math.random() * newCalls.size());
						MethodCallExpr mutant = newCalls.get(randomIndex);
						mutantCalls.remove(mutant);
						

						Mutation callSwap = new Mutation(cu.getStorage().get().getFileName(), "Swap", n, mutant);
						mutations.add(callSwap);
					
						
						return mutant;
						
					}
					else if(randomMutation < swapMutation + allowDeleteMutations) {
						Mutation callDelete = new Mutation(cu.getStorage().get().getFileName(), "Delete", n, n);
						mutations.add(callDelete);
						return null;
					} else {
						return n;
					}	

			}
	
		}, method);
		
		this.mutations.addAll(mutations);
	
		return method;
	
	}
	
	private void logMutations() {
		// Our example data

		FileWriter csvWriter = this.mutationLogFile;
		try {
			csvWriter.append("File Name");
			csvWriter.append(",");
			csvWriter.append("Mutation Type");
			csvWriter.append(",");
			csvWriter.append("Edited Line Number");
			csvWriter.append(",");
			csvWriter.append("Edited Call");
			csvWriter.append(",");
			csvWriter.append("Swaped Line Number");
			csvWriter.append(",");
			csvWriter.append("New Call");
			csvWriter.append("\n");
		
			this.mutations.forEach((mutation) ->{
				String lineContent = String.format("%s, %s, %s, %s, %s, %s", mutation.getJavaFile(), mutation.getMutationType(), mutation.getEditedLine(), mutation.getEditedMethod(), mutation.getSwapedLineNumber(), mutation.getNewCall());
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
		
		if(this.allowInterproceduralMutations) {
			APICalls.addAll(this.getInterProceduralMethods(APICalls));
		}
		
		return APICalls;
	}
	
	private ArrayList<String> getInterProceduralMethods(ArrayList<String> APICalls) {
		ArrayList<String> APIDeclarationCalls = new ArrayList<String>();
		
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
							n.walk(Node.TreeTraversal.PARENTS, node -> {
								if (node instanceof MethodDeclaration) {
									try {
										String declarationName = ((MethodDeclaration) node).getNameAsString();
										String declarationSignature = ((MethodDeclaration) node).resolve().getQualifiedSignature();
										String declarationCall = declarationSignature.substring(0, declarationSignature.indexOf("." + declarationName + "(")) + "#" + declarationName;
										APIDeclarationCalls.add(declarationCall);
									}catch(UnsolvedSymbolException e) {
										
									}
								}
							});
						}
					}catch(UnsolvedSymbolException e) {
						
					}
		
					return super.visit(n, arg);
	
			}
	
		}, calls);
	
		return APIDeclarationCalls;
	}
	
	private void generateMutationProperties(String configFilePath) throws IOException{
		try {
			this.inputStream = getClass().getClassLoader().getResourceAsStream(configFilePath);
			
			if (this.inputStream != null) {
				this.prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + configFilePath + "' not found");
			}
			
			this.allowDeleteMutations = Integer.parseInt(this.prop.getProperty("allowDeleteMutations")); 
			this.allowSwapMutations = Integer.parseInt(this.prop.getProperty("allowSwapMutations")); 
			this.allowDuplicationMutations = Integer.parseInt(this.prop.getProperty("allowDuplicationMutations")); 
			this.allowInterproceduralMutations = Boolean.parseBoolean(this.prop.getProperty("allowInterproceduralMutations"));
			this.allowNoMutations = Integer.parseInt(this.prop.getProperty("allowNoMutations")); 
			this.totalMutationProbabilities = this.allowDeleteMutations + this.allowSwapMutations + this.allowDuplicationMutations + this.allowNoMutations;
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			this.inputStream.close();
		}
	}
	
	
	
	// https://stackoverflow.com/questions/31828851/how-to-write-a-java-program-to-filter-all-commented-lines-and-print-only-java-co
	static void removeComments(Node node) {
		for (Comment child : node.getAllContainedComments()) {
			child.remove();
		}
	}
	
	

}
