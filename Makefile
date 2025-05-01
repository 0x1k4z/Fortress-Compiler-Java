all:
	jflex src/LexicalAnalyzer.flex
	javac -d bin -cp src/ src/Main.java
	jar cfe dist/part3.jar Main -C bin .
#	javadoc -private src/Main.java src/Parser.java src/ParseTree.java src/Symbol.java src/LexicalUnit.java src/FortressToLLVM.java -d doc/javadoc

testing:
	@java -jar dist/part3.jar test/Factorial.fs
#	java -jar dist/part3.jar -wt tree.tex test/test5.fs
#	pdflatex tree.tex
