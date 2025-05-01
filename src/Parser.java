import java.util.Vector;
import java.util.regex.PatternSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

public class Parser {

    private Vector<Symbol> input;
    private Vector<Integer> leftMostDerivation;

    // Constructor.
    /**
     * Constructor of the class Parser.
     * @param tokens the input vector containing tokens of the program to be compiled.
     * @return void.
     */
    public Parser(Vector<Symbol> tokens) {
        input = tokens;
        leftMostDerivation = new Vector<>();
    }

    // Parsing methods.
    /**
     * Method that gives the next token in the input.
     * 
     * @return the next Symbol in the input.
     */
    private Symbol nextToken() {
        if (input.size() > 0){
            return input.get(0);
        }
        else {
            throw new PatternSyntaxException("Incorrect sequence", "Cause: missing tokens in the program", -1);  // when missing tokens.
        }
    }

    /**
     * Method that matches a lexical unit with the next token in the input.
     * 
     * @param expectedTokenUnit the expected token according to the FORTRESS language grammar.
     * @param childrn the array of sub ParseTree's where the token of the input should be added with it's LexicalUnit.
     * @return the next Symbol in the input.
     */
    private void match(LexicalUnit expectedTokenUnit, ArrayList<ParseTree> childrn) {
        Symbol token = nextToken();
        if (token.getType() == expectedTokenUnit) {
            childrn.add(new ParseTree(token));  // last node.
            input.remove(0); // pop element from input since a match occurred.
        }
        else {
            throw new PatternSyntaxException("Incorrect sequence", "Expected: " + expectedTokenUnit + ", received: " + token.getType(), token.getLine());
        }
    }

    // Variable methods.
    /**
     * Method representing the rule number 1 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree program() {
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();
        
        switch (token.getType()) {
            case BEGIN:
                leftMostDerivation.add(1);  // rule n°1.
                match(LexicalUnit.BEGIN, children); match(LexicalUnit.PROGNAME, children); children.add(code()); match(LexicalUnit.END, children);
                break;
            default:
                throw new PatternSyntaxException("Incorrect sequence", "Expected: BEGIN" + ", received: " + token.getType(), token.getLine());
        }

        return new ParseTree(new Symbol(null, "$<program>$"), children);
    }

    /**
     * Method representing the rule number 2 & 3 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree code(){
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();
        
        // code is nullable.
        switch(token.getType()){
            case END:
                leftMostDerivation.add(3);  // rule n°3.
                // When code is null, we add epsilon as its single child in the parse tree.
                return new ParseTree(new Symbol(null, "$<code>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case ELSE:
                leftMostDerivation.add(3);  // rule n°3. 
                return new ParseTree(new Symbol(null, "$<code>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            }
        
        leftMostDerivation.add(2);  // rule n°2.
        children.add(instruction()); match(LexicalUnit.COMMA, children); children.add(code());
        
        return new ParseTree(new Symbol(null, "$<code>$"), children);
    }

    /**
     * Method representing the rule number 4, 5, 6, 7 & 8 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree instruction(){
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();

        switch (token.getType()){
            case VARNAME:
                leftMostDerivation.add(4);  // rule n°4.
                children.add(assign());
                break;
            case IF:
                leftMostDerivation.add(5);  // rule n°5.
                children.add(if_());
                break;
            case WHILE:
                leftMostDerivation.add(6);  // rule n°6.
                children.add(while_());
                break;
            case PRINT:
                leftMostDerivation.add(7);  // rule n°7.
                children.add(print());
                break;
            case READ:
                leftMostDerivation.add(8);  // rule n°8.
                children.add(read());
                break;
            default:
                throw new PatternSyntaxException("Incorrect sequence", "Expected: {VARNAME, IF, WHILE, PRINT, READ}" 
                + ", received: " + token.getType(), token.getLine());
        }
        
        return new ParseTree(new Symbol(null, "$<instruction>$"), children);
    }

    /**
     * Method representing the rule number 9 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree assign(){
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();

        switch(token.getType()){
            case VARNAME:
                leftMostDerivation.add(9);  // rule n°9.
                match(LexicalUnit.VARNAME, children); match(LexicalUnit.ASSIGN, children); children.add(exprArith());
                break;
            default:
                throw new PatternSyntaxException("Incorrect sequence", "Expected: VARNAME" + ", received: " + token.getType(), token.getLine());
        }
        
        return new ParseTree(new Symbol(null, "$<Assign>$"), children);
    }

    /**
     * Method representing the rule number 10 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree exprArith(){
        ArrayList<ParseTree> children = new ArrayList<>();
        leftMostDerivation.add(10);  // rule n°10.
        children.add(T()); children.add(exprArithPrime());
        
        return new ParseTree(new Symbol(null, "$<ExprArith>$"), children);
    }

    /**
     * Method representing the rule number 11, 12 & 13 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree exprArithPrime(){
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();

        // exprArithPrime is nullable.
        switch(token.getType()){
            case COMMA:
                leftMostDerivation.add(13);  // rule n°13.
                return new ParseTree(new Symbol(null, "$<ExprArith'>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case EQUAL:
                leftMostDerivation.add(13);
                return new ParseTree(new Symbol(null, "$<ExprArith'>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case GREATER:
                leftMostDerivation.add(13);
                return new ParseTree(new Symbol(null, "$<ExprArith'>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case SMALLER:
                leftMostDerivation.add(13);
                return new ParseTree(new Symbol(null, "$<ExprArith'>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case RPAREN:
                leftMostDerivation.add(13);
                return new ParseTree(new Symbol(null, "$<ExprArith'>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case PLUS:
                leftMostDerivation.add(11);  // rule n°11.
                match(LexicalUnit.PLUS, children); children.add(T()); children.add(exprArithPrime());
                break;
            case MINUS:
                leftMostDerivation.add(12);  // rule n°12.
                match(LexicalUnit.MINUS, children); children.add(T()); children.add(exprArithPrime());
                break;
            default:
                throw new PatternSyntaxException("Incorrect sequence", "Expected: {PLUS, MINUS, COMMA, EQUAL, GREATER, SMALLER, RPAREN}" 
                + ", received: " + token.getType(), token.getLine());
        }

        return new ParseTree(new Symbol(null, "$<ExprArith'>$"), children);
    }

    /**
     * Method representing the rule number 14 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree T(){
        ArrayList<ParseTree> children = new ArrayList<>();
        leftMostDerivation.add(14);  // rule n°14.
        children.add(F()); children.add(TPrime());

        return new ParseTree(new Symbol(null, "$<T>$"), children);
    }

    /**
     * Method representing the rule number 15, 16 & 17 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree TPrime(){
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();

        // TPrime is nullable.
        switch(token.getType()){
            case PLUS:
                leftMostDerivation.add(17);  // rule n°17.
                return new ParseTree(new Symbol(null, "$<T'>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case MINUS:
                leftMostDerivation.add(17);
                return new ParseTree(new Symbol(null, "$<T'>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case COMMA:
                leftMostDerivation.add(17);
                return new ParseTree(new Symbol(null, "$<T'>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case EQUAL:
                leftMostDerivation.add(17);
                return new ParseTree(new Symbol(null, "$<T'>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case GREATER:
                leftMostDerivation.add(17);
                return new ParseTree(new Symbol(null, "$<T'>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case SMALLER:
                leftMostDerivation.add(17);
                return new ParseTree(new Symbol(null, "$<T'>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case RPAREN:
                leftMostDerivation.add(17);
                return new ParseTree(new Symbol(null, "$<T'>$"), Arrays.asList(new ParseTree(new Symbol(null, "$\\varepsilon$"))));
            case TIMES:
                leftMostDerivation.add(15);  // rule n°15.
                match(LexicalUnit.TIMES, children); children.add(F()); children.add(TPrime());
                break;
            case DIVIDE:
                leftMostDerivation.add(16);  // rule n°16.
                match(LexicalUnit.DIVIDE, children); children.add(F()); children.add(TPrime());
                break;
            default:
                throw new PatternSyntaxException("Incorrect sequence", "Expected: {TIMES, DIVIDE, PLUS, MINUS, EQUAL, GREATER, SMALLER, RPAREN}" 
                + ", received: " + token.getType(), token.getLine());
        }

        return new ParseTree(new Symbol(null, "$<T'>$"), children);
    }

    /**
     * Method representing the rule number 18, 19, 20 & 21 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree F(){
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();

        switch(token.getType()){
            case MINUS:
                leftMostDerivation.add(18);  // rule n°18.
                match(LexicalUnit.MINUS, children); children.add(F());
                break;
            case VARNAME:
                leftMostDerivation.add(19);  // rule n°19.
                match(LexicalUnit.VARNAME, children);
                break;
            case NUMBER:
                leftMostDerivation.add(20);  // rule n°20.
                match(LexicalUnit.NUMBER, children);
                break;
            case LPAREN:
                leftMostDerivation.add(21);  // rule n°21.
                match(LexicalUnit.LPAREN, children); children.add(exprArith()); match(LexicalUnit.RPAREN, children);
                break;
            default:
                throw new PatternSyntaxException("Incorrect sequence", "Expected: {MINUS, VARNAME, NUMBER, LPAREN}" 
                + ", received: " + token.getType(), token.getLine());
        }

        return new ParseTree(new Symbol(null, "$<F>$"), children);
    }

    /**
     * Method representing the rule number 22 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree if_(){
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();

        switch(token.getType()){
            case IF:
                leftMostDerivation.add(22);  // rule n°22.
                match(LexicalUnit.IF, children); match(LexicalUnit.LPAREN, children); 
                children.add(cond()); match(LexicalUnit.RPAREN, children); match(LexicalUnit.THEN, children); 
                children.add(code()); children.add(if_tail());
                break;
            default:
                throw new PatternSyntaxException("Incorrect sequence", "Expected: IF" + ", received: " + token.getType(), token.getLine());
        }

        return new ParseTree(new Symbol(null, "$<IF>$"), children);
    }

    /**
     * Method representing the rule number 23 & 24 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree if_tail(){
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();

        switch(token.getType()){
            case END:
                leftMostDerivation.add(23);  // rule n°23.
                match(LexicalUnit.END, children);
                break;
            case ELSE:
                leftMostDerivation.add(24);  // rule n°24.
                match(LexicalUnit.ELSE, children); children.add(code()); match(LexicalUnit.END, children);
                break;
            default:
                throw new PatternSyntaxException("Incorrect sequence", "Expected: {END, ELSE}" + ", received: " + token.getType(), token.getLine());
        }

        return new ParseTree(new Symbol(null, "$<IF-tail>$"), children);
    }

    /**
     * Method representing the rule number 25 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree cond(){
        ArrayList<ParseTree> children = new ArrayList<>();
        leftMostDerivation.add(25);  // rule n°25.
        children.add(exprArith()); children.add(comp()); children.add(exprArith());

        return new ParseTree(new Symbol(null, "$<Cond>$"), children);
    }

    /**
     * Method representing the rule number 26, 27, 28 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree comp(){
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();

        switch(token.getType()){
            case EQUAL:
                leftMostDerivation.add(26);  // rule n°26.
                match(LexicalUnit.EQUAL, children);
                break;
            case GREATER:
                leftMostDerivation.add(27);  // rule n°27.
                match(LexicalUnit.GREATER, children);
                break;
            case SMALLER:
                leftMostDerivation.add(28);  // rule n°28.
                match(LexicalUnit.SMALLER, children);
                break;
            default:
                throw new PatternSyntaxException("Incorrect sequence", "Expected: {EQUAL, GREATER, SMALLER}" 
                + ", received: " + token.getType(), token.getLine());
        }

        return new ParseTree(new Symbol(null, "$<Comp>$"), children);
    }

    /**
     * Method representing the rule number 29 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree while_(){
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();

        switch(token.getType()){
            case WHILE:
                leftMostDerivation.add(29);  // rule n°29.
                match(LexicalUnit.WHILE, children); match(LexicalUnit.LPAREN, children); children.add(cond()); match(LexicalUnit.RPAREN, children); 
                match(LexicalUnit.DO, children); children.add(code()); match(LexicalUnit.END, children);
                break;
            default:
                throw new PatternSyntaxException("Incorrect sequence", "Expected: WHILE" + ", received: " + token.getType(), token.getLine());
        }

        return new ParseTree(new Symbol(null, "$<While>$"), children);
    }

    /**
     * Method representing the rule number 30 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree print(){
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();

        switch(token.getType()){
            case PRINT:
                leftMostDerivation.add(30);  // rule n°30.
                match(LexicalUnit.PRINT, children); match(LexicalUnit.LPAREN, children); match(LexicalUnit.VARNAME, children); 
                match(LexicalUnit.RPAREN, children);
                break;
            default:
                throw new PatternSyntaxException("Incorrect sequence", "Expected: PRINT" + ", received: " + token.getType(), token.getLine());
        }

        return new ParseTree(new Symbol(null, "$<Print>$"), children);
    }

    /**
     * Method representing the rule number 31 in the grammar.
     * 
     * @return the corresponding parse tree of the method.
     */
    private ParseTree read(){
        Symbol token = nextToken();
        ArrayList<ParseTree> children = new ArrayList<>();

        switch(token.getType()){
            case READ:
                leftMostDerivation.add(31);  // rule n°31.
                match(LexicalUnit.READ, children); match(LexicalUnit.LPAREN, children); match(LexicalUnit.VARNAME, children);
                match(LexicalUnit.RPAREN, children);
                break;
            default:
                throw new PatternSyntaxException("Incorrect sequence", "Expected: READ" + ", received: " + token.getType(), token.getLine());
        }

        return new ParseTree(new Symbol(null, "$<Read>$"), children);
    }

    /**
     * Method that calls the starting variable program function to start parsing.
     * 
     * @return string containing the LaTeX text of the parse tree.
     */
    public ParseTree parse() {
        ParseTree root = program();  // call start variable program function to start the parsing.
        return root;
    }

}
