# Compilers Course
## Exercise 2 - LL(1) Parser

Implementing a simple Compiler's LL(1) Parser using Java & according to exercise defined config file.

* Input: Config File (representing LL(1) table)
* Output: Syntax Tree definition to be drawn using [webgraphviz.com](http://www.webgraphviz.com/)


### Usage
1. Export project as runnable Jar file, for example `syntax_parser.jar`, with main class: `SyntaxMain.java`
2. Run from CLI: `java -jar syntax_parser.jar config.ll1 input.txt`
    * Config file must be a valid LL(1) parse table with first row defining Terminals
3. Output will be in file `input.ptree`
4. Goto [webgraphviz.com](http://www.webgraphviz.com/) and paste the output to view the parse tree