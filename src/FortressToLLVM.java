import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class FortressToLLVM {
    
    private static Integer varCounter = -1;
    private static Integer readVarCounter = 0;
    private static Integer printVarCounter = 0;
    private static Integer caseLabelCounter = 0;
    private static Vector<String> variables = new Vector<>();  // vector that contains already stored variables in the memory LLVM.

    private ParseTree AST;  // <code>
    private List<ParseTree> instructions;
    private String generatedCode;

    /**
     * Constructor of class FortressToLLVM.
     * @param abstractSyntaxTree the AST root node from which to start generating the LLVM code.
     * @return void.
     */
    public FortressToLLVM(ParseTree abstractSyntaxTree){
        AST = abstractSyntaxTree;
        instructions = new ArrayList<ParseTree>();
        generatedCode = new String();
    }

    /**
     * Public method that's called to start generating the LLVM code by using the AST.
     * @return void.
     */
    public void start(){
        retrieveInstructions(AST);  // starts from <code>.
        
        for (ParseTree instruction : instructions){
            processInstruction(instruction);
        }
    }

    /**
     * Method that retrieves all the instructions that the AST contains.
     * @param root root <code> node from which the instruction is retrieved.
     * @return void.
     */
    private void retrieveInstructions(ParseTree root){
        instructions.add(root.getChildren().get(0).getChildren().get(0));
        
        if (root.getChildren().size() == 2){
            retrieveInstructions(root.getChildren().get(1));
        }
    }

    /**
     * Method that process a given instruction retrieved from the AST.
     * @param instruction the instruction to process.
     * @return void.
     */
    private void processInstruction(ParseTree instruction){
        // process the instruction and produce the corresponding LLVM code block.
        switch ((String)instruction.getLabel().getValue()){
            case "$<Assign>$":  // (done)
                generatedCode += assignInstruction(instruction);
                break;
            case "$<IF>$":  // (done)
                generatedCode += ifInstruction(instruction);
                break;
            case "$<While>$":  // (done)
                generatedCode += whileInstruction(instruction);
                break;
            case "$<Print>$":  // (done)
                generatedCode += printInstruction(instruction);
                break;
            case "$<Read>$":  // (done)
                generatedCode += readInstruction(instruction);
                break;
        }
    }

    /**
     * Method exprArith that process an arithmetic expression in the AST.
     * @param instruction the exprArith node in the AST.
     * @return string containing the LLVM code generated that represents the arithmetic expression.
     */
    private String exprArith(ParseTree instruction){
        String string = new String();
        
        if (instruction.getChildren().size() == 0){ // if its a leaf in the expression.
            varCounter++;
            if (instruction.getLabel().getType() == LexicalUnit.VARNAME){
                // we suppose here that the variable "varname" is already defined.
                string += "\t%" + varCounter.toString() + " = load i32, i32* %" + instruction.getLabel().getValue() + "\n";
            }
            else {
                string += "\t%" + varCounter.toString() + " = add i32 " + instruction.getLabel().getValue() + ", 0\n";
            }
        }
        else if (Arrays.asList(LexicalUnit.PLUS, LexicalUnit.DIVIDE, LexicalUnit.MINUS, LexicalUnit.TIMES).contains(instruction.getLabel().getType())){
            string += exprArith(instruction.getChildren().get(0));  // evaluate left child.
            Integer leftCounter = varCounter;
            string += exprArith(instruction.getChildren().get(1));  // evaluate right child.
            Integer rightCounter = varCounter;

            switch(instruction.getLabel().getType()){
                case PLUS:
                    string += ("\t%" + (++varCounter).toString() + " = add i32 %" + leftCounter.toString() + ", %" + rightCounter.toString() + "\n");
                    break;
                case MINUS:
                    string += ("\t%" + (++varCounter).toString() + " = sub i32 %" + leftCounter.toString() + ", %" + rightCounter.toString() + "\n");
                    break;
                case DIVIDE:
                    string += ("\t%" + (++varCounter).toString() + " = sdiv i32 %" + leftCounter.toString() + ", %" + rightCounter.toString() + "\n");
                    break;
                case TIMES:
                    string += ("\t%" + (++varCounter).toString() + " = mul i32 %" + leftCounter.toString() + ", %" + rightCounter.toString() + "\n");
                    break;
            }
        }
        else if (instruction.getChildren().size() == 2){
            if (instruction.getChildren().get(0).getLabel().getType() == LexicalUnit.MINUS){
                // idea is that when there is -(exprArith), we evaluate (exprArith) and then multiply the result by -1.
                string += exprArith(instruction.getChildren().get(1));  // evaluate exprArith.
                Integer rightCounter = varCounter;
                string += "\t%" + (++varCounter).toString() + " = mul i32 -1, %" + rightCounter.toString() + "\n";
            }

        }
        else {
            for (ParseTree child: instruction.getChildren()){
                string += exprArith(child);
            }
        }
        
        return string;
    }

    /**
     * Method cond that process a condition in the AST.
     * @param instruction the cond node in the AST.
     * @param caseCounter the case label counter.
     * @return string containing the LLVM code generated that represents the condition.
     */
    private String cond(ParseTree instruction, Integer caseCounter){
        String string = new String();
        string += exprArith(instruction.getChildren().get(0));
        Integer leftCounter = varCounter;
        string += exprArith(instruction.getChildren().get(2));
        Integer rightCounter = varCounter;

        LexicalUnit comparisonType = instruction.getChildren().get(1).getChildren().get(0).getLabel().getType();

        switch (comparisonType){
            case GREATER:
                string += "\t%cond" + caseCounter.toString() + " = icmp sgt i32 %" + leftCounter.toString() + ", %" + rightCounter.toString() + "\n";
                break;
            case SMALLER:
                string += "\t%cond" + caseCounter.toString() + " = icmp slt i32 %" + leftCounter.toString() + ", %" + rightCounter.toString() + "\n";
                break;
            case EQUAL:
                string += "\t%cond" + caseCounter.toString() + " = icmp eq i32 %" + leftCounter.toString() + ", %" + rightCounter.toString() + "\n";
                break;
        }
        
        return string;
    }

    /**
     * Method code that process a code in the AST.
     * @param instruction the code node in the AST.
     * @return string containing the LLVM code generated that represents the code.
     */
    private String code(ParseTree instruction){
        FortressToLLVM code = new FortressToLLVM(instruction);  // as if it's a whole "program".
        code.start();
        return code.getGeneratedCode();
    }

    /**
     * Method that adds the print, read and main LLVM functions in the beggining of the program.
     */
    public void addFunctions(){
        // add the function that allows to print a variable in LLVM (Source: computer practical 2).
        generatedCode += "\n@.strP = private unnamed_addr constant [4 x i8] c\"%d\\0A\\00\", align 1\n\n" +
        "define void @println(i32 %x) #0 {\n" +
        "\t%1 = alloca i32, align 4\n" +
        "\tstore i32 %x, i32* %1, align 4\n" +
        "\t%2 = load i32, i32* %1, align 4\n" +
        "\t%3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)\n" +
        "\tret void\n" +
        "}\n\n" +
        "declare i32 @printf(i8*, ...) #1\n";

        // add the function that allows to read an input in LLVM (Source: computer practical 2).
        generatedCode += "\ndeclare i32 @getchar()\n\n" +
        "define i32 @readInt() {\n" +
        "entry:\n" +
        "\t%res   = alloca i32\n" +
        "\t%digit = alloca i32\n" +
        "\tstore i32 0, i32* %res\n" +
        "\tbr label %read\n" +
        "read:\n" + 
        "\t%0 = call i32 @getchar()\n" +
        "\t%1 = sub i32 %0, 48\n" +
        "\tstore i32 %1, i32* %digit\n" +
        "\t%2 = icmp ne i32 %0, 10\n" +
        "\tbr i1 %2, label %check, label %exit\n" +
        "check:\n" +
        "\t%3 = icmp sle i32 %1, 9\n" +
        "\t%4 = icmp sge i32 %1, 0\n" +
        "\t%5 = and i1 %3, %4\n" +
        "\tbr i1 %5, label %save, label %exit\n" +
        "save:\n" +
        "\t%6 = load i32, i32* %res\n" +
        "\t%7 = load i32, i32* %digit\n" +
        "\t%8 = mul i32 %6, 10\n" +
        "\t%9 = add i32 %8, %7\n" +
        "\tstore i32 %9, i32* %res\n" +
        "\tbr label %read\n" +
        "exit:\n" +
        "\t%10 = load i32, i32* %res\n" +
        "\tret i32 %10\n" +
        "}\n\n";

        // add the beggining of main function.
        generatedCode += "define i32 @main() {\n" +
        "entry:\n";
    }

    /**
     * Method that process an assign instruction.
     * @param instruction the assign instruction node.
     * @return string containing the LLVM code generated that represents the assign instruction.
     */
    private String assignInstruction(ParseTree instruction){
        // instruction is parseTree.
        String LLVMcodeBlock = new String();
        if (!variables.contains(instruction.getChildren().get(0).getLabel().getValue())){
            LLVMcodeBlock += ("\t%" + instruction.getChildren().get(0).getLabel().getValue() + " = alloca i32\n");
            variables.add((String)instruction.getChildren().get(0).getLabel().getValue());
        }
        LLVMcodeBlock += exprArith(instruction.getChildren().get(1));
        LLVMcodeBlock += ("\tstore i32 %" + varCounter.toString() + ", i32* %" + instruction.getChildren().get(0).getLabel().getValue()) + "\n";
        return LLVMcodeBlock;
    }

    /**
     * Method that process an IF instruction.
     * @param instruction the IF instruction node.
     * @return string containing the LLVM code generated that represents the IF instruction.
     */
    private String ifInstruction(ParseTree instruction){
        String LLVMcodeBlock = new String();
        Integer counter = ++caseLabelCounter;

        LLVMcodeBlock += cond(instruction.getChildren().get(0), counter);
        LLVMcodeBlock += "\tbr i1 %cond" + counter.toString() + ", label %caseTrue" + counter.toString() + ", label %caseFalse" + counter.toString() + "\n";
        LLVMcodeBlock += "caseTrue" + counter.toString() + ": \n";  // case True.
        LLVMcodeBlock += code(instruction.getChildren().get(1));
        LLVMcodeBlock += "\tbr label %next" + counter.toString() + "\n";  // add an unconditional jump to skip the code of false label.
        LLVMcodeBlock += "caseFalse" + counter.toString() + ": \n";  // if there is no "else" caseFalse will allow to keep going to next instruction.
        
        if (!(instruction.getChildren().get(2).getChildren().size() == 0)){
            LLVMcodeBlock += code(instruction.getChildren().get(2).getChildren().get(0));
        }

        LLVMcodeBlock += "\tbr label %next" + counter.toString() + "\n";
        LLVMcodeBlock += "next" + counter.toString() + ": \n";
        
        return LLVMcodeBlock;
    }

    /**
     * Method that process a while instruction.
     * @param instruction the while instruction node.
     * @return string containing the LLVM code generated that represents the while instruction.
     */
    private String whileInstruction(ParseTree instruction){
        String LLVMcodeBlock = new String();
        Integer counter = ++caseLabelCounter;

        LLVMcodeBlock += "\tbr label %Cond" + counter.toString() + "\n";
        LLVMcodeBlock += "Cond" + counter.toString() + ": \n";  // condition label to be checked everytime before executing what's inside the loop.
        LLVMcodeBlock += cond(instruction.getChildren().get(0), counter);
        LLVMcodeBlock += "\tbr i1 %cond" + counter.toString() + ", label %caseTrue" + counter.toString() + ", label %caseFalse" + counter.toString() + "\n";
        LLVMcodeBlock += "caseTrue" + counter.toString() + ": \n";  // case True.
        LLVMcodeBlock += code(instruction.getChildren().get(1));
        LLVMcodeBlock += "\tbr label %Cond" + counter.toString() + "\n";  // jump back to the Cond label to check the condition again.
        LLVMcodeBlock += "caseFalse" + counter.toString() + ": \n";

        return LLVMcodeBlock;
    }

    /**
     * Method that process a print instruction.
     * @param instruction the print instruction node.
     * @return string containing the LLVM code generated that represents the print instruction.
     */
    private String printInstruction(ParseTree instruction){
        String LLVMcodeBlock = new String();
        LLVMcodeBlock += "\t%p" + (++printVarCounter).toString() +" = load i32, i32* %" + instruction.getChildren().get(0).getLabel().getValue() + "\n";
        LLVMcodeBlock += "\tcall void @println(i32 %p" + printVarCounter.toString() + ")\n";
        return LLVMcodeBlock;
    }

    /**
     * Method that process a read instruction.
     * @param instruction the read instruction node.
     * @return string containing the LLVM code generated that represents the read instruction.
     */
    private String readInstruction(ParseTree instruction){
        String LLVMcodeBlock = new String();
        LLVMcodeBlock += "\t%r" + (++readVarCounter).toString() + " = call i32 @readInt()\n";
        if (!variables.contains(instruction.getChildren().get(0).getLabel().getValue())){
            LLVMcodeBlock += "\t%" + instruction.getChildren().get(0).getLabel().getValue() + " = alloca i32\n"; // creates variable in memory and stores what's read.
            variables.add((String)instruction.getChildren().get(0).getLabel().getValue());
        }
        LLVMcodeBlock += "\tstore i32 %r" + readVarCounter.toString() +  ", i32* %" + instruction.getChildren().get(0).getLabel().getValue() + "\n";
        return LLVMcodeBlock;
    }

    /**
     * Method that adds the return of the main LLVM function as well as the closing bracket.
     */
    public void addMainClosingBracket(){
        generatedCode += "\tret i32 0\n" +
        "}\n";
    }

    /**
     * Getter that allows to get the generated LLVM code.
     * @return the LLVM generated code.
     */
    public String getGeneratedCode(){
        return generatedCode;
    }
    
}
