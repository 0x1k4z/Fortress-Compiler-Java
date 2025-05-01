# FORTRESS Compiler

## 📄 Overview

This project is a compiler for **FORTRESS**, a simple imperative language inspired by FORTRAN.  
It includes a **scanner** that performs lexical analysis and a **parser**, built from scratch, that builds the syntax structure of the program.

### 🔤 Identifiers
- `[VarName]`: Lowercase letters and digits, starting with a **lowercase letter** (e.g., `var1`, `x`).
- `[ProgName]`: Starts with an **uppercase letter**, must not be fully uppercase (e.g., `Factorial`, `MyProg1`).
- `[Number]`: Digits only, **no leading zeros** (e.g., `42` is valid, `042` is not).

### 💬 Comments
- `::` for **single-line** comments  
- `%% ... %%` for **multi-line** comments (**no nesting allowed**)

Comments are completely ignored by the scanner and not passed to the parser.

## 🚀 How to Run

```bash
java -jar dist/part3.jar inputFile
```