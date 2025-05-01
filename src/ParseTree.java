import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A skeleton class to represent parse trees. The arity is not fixed: a node can
 * have 0, 1 or more children. Trees are represented in the following way: Tree
 * :== Symbol * List<Tree> In other words, trees are defined recursively: A tree
 * is a root (with a label of type Symbol) and a list of trees children. Thus, a
 * leave is simply a tree with no children (its list of children is empty). This
 * class can also be seen as representing the Node of a tree, in which case a
 * tree is simply represented as its root.
 * 
 * @author Léo Exibard, Sarah Winter
 */

public class ParseTree {
    private Symbol label; // The label of the root of the tree
    private List<ParseTree> children; // Its children, which are trees themselves

    /**
     * Creates a singleton tree with only a root labeled by lbl.
     * 
     * @param lbl The label of the root
     */
    public ParseTree(Symbol lbl) {
        this.label = lbl;
        this.children = new ArrayList<ParseTree>(); // This tree has no children
    }

    /**
     * Creates a tree with root labeled by lbl and children chdn.
     * 
     * @param lbl  The label of the root
     * @param chdn Its children
     */
    public ParseTree(Symbol lbl, List<ParseTree> chdn) {
        this.label = lbl;
        this.children = chdn;
    }

    public Symbol getLabel(){
        return label;
    }

    public List<ParseTree> getChildren(){
        return children;
    }

    /**
     * Writes the tree as LaTeX code
     */
    public String toLaTexTree() {
        StringBuilder treeTeX = new StringBuilder();
        treeTeX.append("[");
        treeTeX.append("{" + label.toTexString() + "}");
        treeTeX.append(" ");

        for (ParseTree child : children) {
            treeTeX.append(child.toLaTexTree());
        }
        treeTeX.append("]");
        return treeTeX.toString();
    }

    /**
     * Checks if the child of the current node contains an epsilon, i.e nothing.
     */
    boolean isEmpty(){
        if (children.size() == 1){
            return children.get(0).label.getValue() == "$\\varepsilon$";
        }
        return false;
    }

    /**
     * Checks if a node is necessary, i.e if the variable node is needed.
     */
    boolean isNecessary(){
        return !Arrays.asList("$<ExprArith'>$", "$<T>$", "$<T'>$", "$<F>$").contains(label.getValue());
    }

    /**
     * Checks if all the children of the current node are necessary.
     */
    private boolean notAllChildrenUseful(){
        for (ParseTree child: children){
            if (!child.isNecessary()){
                return true;
            }
        }
        return false;
    }

    /**
     * Restructures the ParseTree by putting every operator (+, -, *, /) on top and puts the concerned operands as children. This method also
     * removes unnecessary nodes that add nothing to the AST.
     */
    private void restructure(){
        if (label.getValue() == "$<ExprArith>$" || label.getValue() == "$<T>$"){  // put the operator on top as a parent node.
            if (children.get(1).children.size() == 3){
                children.add(children.get(1).children.get(0));
                children.get(1).children.remove(0);
                children.get(2).children.add(children.get(0));
                children.get(2).children.add(children.get(1));
                children.remove(1); children.remove(0);
            }
        }

        else if (label.getValue() == "$<ExprArith'>$" || label.getValue() == "$<T'>$"){
            if (children.size() == 2){
                if (children.get(1).children.size() == 3){
                    children.add(children.get(1).children.get(0));
                    children.get(1).children.remove(0);
                    children.get(2).children.add(children.get(0));
                    children.get(2).children.add(children.get(1));
                    children.remove(1); children.remove(0);
                }
            }
        }

        else if (label.getValue() == "$<F>$"){
            if (children.size() == 2 & children.get(0).label.getType() == LexicalUnit.MINUS){
                Symbol symbol = children.get(1).children.get(0).label;
                if (symbol.getType() == LexicalUnit.NUMBER){
                    String number = (String)symbol.getValue();
                    children.add(new ParseTree(new Symbol(symbol.getType(), symbol.getLine(), symbol.getColumn(), "-" + number)));
                    children.remove(1); children.remove(0);
                }
            }
        }

        for (int i = children.size() - 1; i >= 0; i--){
            if (Arrays.asList(LexicalUnit.BEGIN, LexicalUnit.PROGNAME, LexicalUnit.END, LexicalUnit.COMMA, LexicalUnit.ASSIGN,
                LexicalUnit.LPAREN, LexicalUnit.RPAREN, LexicalUnit.EOS, LexicalUnit.WHILE, LexicalUnit.PRINT, LexicalUnit.READ, LexicalUnit.DO,
                LexicalUnit.IF, LexicalUnit.THEN, LexicalUnit.ELSE
                ).contains(children.get(i).label.getType()) || children.get(i).isEmpty()){
                children.remove(i);
            }
            else {
                children.get(i).restructure();
            }
        }
    }

    /**
     * Remove all unnecessary variable nodes (such as <F> etc.) from the parse tree to simplify it.
     */
    private void removeUselessVariableNodes(){

        while (notAllChildrenUseful()){
            for (int i = children.size() - 1; i >= 0; i--){
                if (!children.get(i).isNecessary()){
                    ParseTree node = children.remove(i);
                    children.addAll(i, node.children);
                }
            }
        }

        for (ParseTree child: children){
            child.removeUselessVariableNodes();
        }
    }

    /**
     * Restructures the parse tree in order to make it take into account the left associativity of operations.
     */
    private void restructureLeftAssociativity(){

        if (children.size() == 2){
            if ((label.getType() == LexicalUnit.PLUS & children.get(1).label.getType() == LexicalUnit.MINUS) ||
                (label.getType() == LexicalUnit.MINUS & children.get(1).label.getType() == LexicalUnit.PLUS) ||
                (label.getType() == LexicalUnit.TIMES & children.get(1).label.getType() == LexicalUnit.DIVIDE) ||
                (label.getType() == LexicalUnit.DIVIDE & children.get(1).label.getType() == LexicalUnit.TIMES)){

                    children.add(1, children.get(1).children.get(0));
                    children.get(2).children.remove(0);
                    children.get(2).children.add(0, new ParseTree(label, Arrays.asList(children.get(0), children.get(1))));
                    label = children.get(2).label;
                    children = children.get(2).children;
            }
        }

        for (ParseTree child : children){
            child.restructureLeftAssociativity();
        }
    }

    /**
     * Public method that's called to simplify the parse tree in order to get an AST.
     */
    public ParseTree toAST(){
        restructure();
        removeUselessVariableNodes();
        restructureLeftAssociativity();
        return this;
    }

    /**
     * Writes the tree as TikZ code. TikZ is a language to specify drawings in LaTeX
     * files.
     */
    public String toTikZ() {
        StringBuilder treeTikZ = new StringBuilder();
        treeTikZ.append("node {");
        treeTikZ.append(label.toTexString()); // Implement this yourself in Symbol.java
        treeTikZ.append("}\n");
        for (ParseTree child : children) {
            treeTikZ.append("child { ");
            treeTikZ.append(child.toTikZ());
            treeTikZ.append(" }\n");
        }
        return treeTikZ.toString();
    }

    /**
     * Writes the tree as a TikZ picture. A TikZ picture embeds TikZ code so that
     * LaTeX undertands it.
     */
    public String toTikZPicture() {
        return "\\begin{tikzpicture}[tree layout]\n\\" + toTikZ() + ";\n\\end{tikzpicture}";
    }

    /**
     * Writes the tree as a forest picture. Returns the tree in forest enviroment
     * using the latex code of the tree
     */
    public String toForestPicture() {
        return "\\begin{forest}for tree={rectangle, draw, l sep=20pt}" + toLaTexTree() + ";\n\\end{forest}";
    }

    /**
     * Writes the tree as a LaTeX document which can be compiled using PDFLaTeX.
     * <br>
     * <br>
     * The result can be used with the command:
     * 
     * <pre>
     * pdflatex some-file.tex
     * </pre>
     */
    public String toLaTeX() {
        return "\\documentclass[border=5pt]{standalone}\n\n\\usepackage{tikz}\n\\usepackage{forest}\n\n\\begin{document}\n\n"
                + toForestPicture() + "\n\n\\end{document}\n%% Local Variables:\n%% TeX-engine: pdflatex\n%% End:";
    }
}
