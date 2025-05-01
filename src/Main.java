import java.io.*;
import java.util.TreeMap;
import java.util.regex.*;
import java.util.Vector;

public class Main {
    /**
     * Method that receive the different tokens from the LexicalAnalyzer and then call
     * the Parser to check whether the FORTRESS program is valid or not then generate the corresponding LLVM code.
     * 
     * @param args array of program arguments.
     * @return void.
     */
    public static void main(String[] args) {
        try {
            if (args.length < 1 || args.length == 2 || args.length > 3 || (args.length == 3 && !args[0].equals("-wt"))){  // check program arguments.
                throw new IllegalArgumentException("Wrong program arguments! \nThe command to run the program is: java -jar dist/part2.jar [OPTION] [FILE]\n" + 
                "    - [OPTION] allows to save the parse tree in LaTeX file: -wt sourceFile.tex\n" + 
                "    - [FILE]: path to the file containing the FORTRESS code.\n");}
            
            boolean createParseTree = (args.length == 3)? true : false;
            Reader input = new FileReader(args[createParseTree ? 2 : 0]);
            // Create lexical analyzer.
            LexicalAnalyzer lexer = new LexicalAnalyzer(input);
            TreeMap<String, Integer> variables = new TreeMap<String, Integer>();
            Vector<Symbol> tokens = new Vector<>(); // vector of all tokens found.
            boolean isEOS = false;
            while (!isEOS) {
                Symbol currentSymbol = lexer.yylex();
                LexicalUnit tempType = currentSymbol.getType();
                if (tempType == LexicalUnit.EOS) {
                    isEOS = true;
                } else {
                    tokens.add(currentSymbol); // add symbol to vector.
                    if (tempType == LexicalUnit.VARNAME && !variables.containsKey(currentSymbol.getValue())) {
                        variables.put(currentSymbol.getValue().toString(), currentSymbol.getLine());
                    }
                }
            }
            // Create parser.
            Parser parser = new Parser(tokens);
            ParseTree PT = parser.parse();  // call parsing method.
            if (createParseTree){
                File file = new File(args[1]);
                FileWriter myWriter = new FileWriter(args[1]);
                myWriter.write(PT.toLaTeX());  // complete parse tree can still be seen with appropriate command.
                myWriter.close();
            }
            // LLVM code generation.
            FortressToLLVM FToLLVM = new FortressToLLVM(PT.toAST().getChildren().get(0));  // so that it starts at <code> and not <program>.
            FToLLVM.addFunctions(); FToLLVM.start(); FToLLVM.addMainClosingBracket();
            System.out.println(FToLLVM.getGeneratedCode());

        } catch (FileNotFoundException ex) {
            System.out.println("File not found!");
        } catch (PatternSyntaxException ex) {
            System.out.println(ex.getMessage());
        } catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Problem occurred!" + " " + ex.getMessage());
        }
    }
}
