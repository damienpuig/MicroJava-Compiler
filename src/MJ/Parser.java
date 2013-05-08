/*  MicroJava Parser (HM 06-12-28)
    ================
    * Caution: Arithmetic expressions has not been done yet on the project.
*/
package MJ;

import MJ.Error;
import MJ.Scanner;
import MJ.SymTab.Obj;
import MJ.SymTab.Struct;
import MJ.SymTab.Tab;
import java.util.*;

public class Parser {
	private static final int  // token codes
		none      = 0,
		ident     = 1,
		number    = 2,
		charCon   = 3,
		plus      = 4,
		minus     = 5,
		times     = 6,
		slash     = 7,
		rem       = 8,
		eql       = 9,
		neq       = 10,
		lss       = 11,
		leq       = 12,
		gtr       = 13,
		geq       = 14,
		assign    = 15,
		semicolon = 16,
		comma     = 17,
		period    = 18,
		lpar      = 19,
		rpar      = 20,
		lbrack    = 21,
		rbrack    = 22,
		lbrace    = 23,
		rbrace    = 24,
		class_    = 25,
		else_     = 26,
		final_    = 27,
		if_       = 28,
		new_      = 29,
		print_    = 30,
		program_  = 31,
		read_     = 32,
		return_   = 33,
		void_     = 34,
		while_    = 35,
		eof       = 36;
	private static final String[] name = { // token names for error messages
		"none", "identifier", "number", "char constant", "+", "-", "*", "/", "%",
		"==", "!=", "<", "<=", ">", ">=", "=", ";", ",", ".", "(", ")",
		"[", "]", "{", "}", "class", "else", "final", "if", "new", "print",
		"program", "read", "return", "void", "while", "eof"
		};

	private static Token t;			// current token (recently recognized)
	private static Token la;		// lookahead token
	private static int sym;			// always contains la.kind
	public  static int errors;  // error counter
	private static int errDist;	// no. of correctly recognized tokens since last error

	private static BitSet exprStart, statStart, statSync, statSeqFollow, declStart, declFollow, declSync;

	//------------------- auxiliary methods ----------------------
	private static void scan() {
		t = la;
		la = Scanner.next();
		sym = la.kind;
		errDist++;
		/*
		System.out.print("line " + la.line + ", col " + la.col + ": " + name[sym]);
		if (sym == ident) System.out.print(" (" + la.string + ")");
		if (sym == number || sym == charCon) System.out.print(" (" + la.val + ")");
		System.out.println();*/
	}

	private static void check(int expected) {
		if (sym == expected) {
                scan();
            }
		else {
                error(name[expected] + " expected");
            }
	}

	public static void error(String msg) { // syntactic error at token la
		if (errDist >= 3) {
                        // print errors in a nicer format style, custom classes
                        ErrorHandler.Handle(new Error(t, "-- line " + la.line + " col " + la.col + ": " + msg, null));
			errors++;
		}
		errDist = 0;
	}

         // ActPars = "(" [ Expr {"," Expr} ] ")".
         private static void ActPars(){
             if(sym == lpar){
                 scan();
                 if(exprStart.get(sym)) { 
                    Expr();
                    while(sym == comma){ scan(); Expr();}
                 }
                 check(rpar);
             }
         }
         
        // Addop = "+" | "-".
        private static void Addop(){
            if(sym == plus || sym == minus){ scan();}
            else { error("Addop parsing error");}
        }
        
        // Block = "{" {Statement} "}".
         private static void Block() {
             check(lbrace);
             while(!statSeqFollow.get(sym)){ Statement();}
             check(rbrace);
         }
         
        // Condition = Expr Relop Expr.
        private static void Condition(){
            Expr();
            Relop();
            Expr();
        }
        
        // ClassDecl = "class" ident "{" {VarDecl} "}".
        private static void ClassDecl() {
             Obj classEntry;
             
             check(class_);
             check(ident);
             
             classEntry = Tab.insert(Obj.Type, t.string, new Struct(Struct.Class));
             
             Tab.openScope();
             
             check(lbrace);
             while(sym == ident){ VarDecl();}
             check(rbrace);
             
             classEntry.type.fields = Tab.curScope.locals;
             classEntry.type.nFields = Tab.curScope.nVars;
             
             Tab.closeScope();
         }
         
        // ConstDecl = "final" Type ident "=" (number | charConst) ";".
        private static void ConstDecl() {
             Struct constType;
             String declname;
             Obj current;
             
             check(final_);
            
             constType = Type(); 
             
               check(ident);
               declname = t.string;
               current = Tab.insert(Obj.Con, declname, constType);

             check(assign);
             
             if(sym == number)
             {
                 scan();
                 if(!constType.equals(Tab.intType)) { error("Invalid Const value for " + declname); }
                 current.val = t.val;
                 
             }
             
             if(sym == charCon )
             {
                 scan();
                 if(!constType.equals(Tab.charType)) { error("Invalid Const value for " + declname); }
                 current.val = t.val;
                 
             }
             
             check(semicolon); 
         }
        
         // Designator = ident {"." ident | "[" Expr "]"}.
        private static void Designator(){
            check(ident);
            while(sym == period || sym == lbrack)
            {
                if(sym == period) {  scan(); check(ident);}
                if(sym == lbrack) {  scan(); Expr(); check(rbrack); }
            }
            
        }
        
        //Expr = ["-"] Term {Addop Term}.
        private static void Expr(){
           
            if(sym == minus) {scan();}
            Term();
            while(sym == plus || sym == minus){
                scan();
                Term();
            }
        }
        
        /*Factor = Designator [ActPars]
           | number
           | charConst
           | "new" ident ["[" Expr "]"]
           | "(" Expr ")".*/
        private static void Factor(){
            if(sym == ident){ 
                Designator(); 
                if(sym == lpar){ ActPars();}}
            
            else if(sym == number){ scan();}
            else if(sym == charCon){ scan();}
            else if(sym == new_){
                 scan(); check(ident);
                 if(sym == lbrack){ 
                     scan(); Expr(); check(rbrack);}}
            else if(sym == lpar){ scan(); Expr(); check(rpar);}
            else { error("Factor parsing error");}
        }
        
        // FormPars = Type ident {"," Type ident}.
       private static void FormPars(){
            Struct paramType;
            String paramName;
            paramType = Type();
            
            check(ident);
            paramName = t.string;
            Tab.insert(Obj.Var, paramName, paramType);
            
            while(sym == comma){ 
                scan();
                paramType= Type();
                check(ident);
                paramName = t.string;
                Tab.insert(Obj.Var, paramName, paramType);
            }
        }
        
        
        // MethodDecl = (Type | "void") ident "(" [FormPars] ")" {VarDecl} Block.
        private static void MethodDecl(){
             
             String methName;
             Struct methType = new Struct(Struct.None);
             
            if(sym == ident){
                methType = Type();
            }
            else if(sym == void_) {scan();}
            else error("Method declaration parsing error");
            
            check(ident);
            methName = t.string;
            Tab.curMethod = Tab.insert(Obj.Meth, methName, methType);
            Tab.openScope();
            
            check(lpar);
            if(sym == ident){ FormPars();}
            check(rpar);

            while(sym == ident){ VarDecl();}
            
            Tab.curMethod.locals = Tab.curScope.locals;
            Tab.curMethod.nPars = Tab.curScope.nVars;
            
            Block();
            
            Tab.closeScope();
        }
        
        // Mulop = "*" | "/" | "%".
        private static void Mulop(){
            if(sym == times || sym == slash || sym == rem){ scan();}
            else { error("Mulop parsing error"); }
        }
	
        // Program = "program" ident {ConstDecl | ClassDecl | VarDecl} '{' {MethodDecl} '}'.
	private static void Program() {
            Tab.openScope();
            check(program_);
            check(ident);
            for(;;)
            {
                if(sym == final_) {  ConstDecl();}
                else if (sym == ident){ VarDecl();}
                else if(sym == class_){ClassDecl();}
                else if(!declSync.get(sym)){ RecoveryDecl();}
                else {break;}
            }
            
            check(lbrace);
            while(sym == ident || sym == void_){ MethodDecl();}
            check(rbrace);
            Tab.closeScope();
	}
        
        // Relop = "==" | "!=" | ">" | ">=" | "<" | "<=".
        private static void Relop() {
            if(sym == eql || sym == neq ||
               sym == lss || sym == leq ||
               sym == gtr || sym == geq) { scan();}
            else { error("Relop parsing error");}
        }
        
        /*Statement = Designator ("=" Expr | ActPars) ";"
            | "if" "(" Condition ")" Statement ["else" Statement]
            | "while" "(" Condition ")" Statement
            | "return" [Expr] ";"
            | "read" "(" Designator ")" ";"
            | "print" "(" Expr ["," number] ")" ";"
            | Block */
        private static void Statement() {
            if (!statStart.get(sym)) {
                error("Invalid start of statement");
                do scan(); while (!statSync.get(sym) );
                if (sym == semicolon) { scan(); }
                errDist = 0;
                    }

            if(sym == ident){
               Designator(); 
               if(sym == assign){ scan(); Expr();}
               else if(sym == lpar){ActPars();}
               else { error("Invalid assignment or call"); }
               check(semicolon);
            }
            else if(sym == if_ || sym == while_)
            { 
                scan(); check(lpar); Condition(); check(rpar);
                Statement();
                if(sym == else_){ scan(); Statement();}
            }
            else if(sym == return_) { scan(); if(exprStart.get(sym)){ Expr();} check(semicolon);}
            else if(sym == read_){ scan(); check(lpar); Designator(); check(rpar); check(semicolon);}
            else if(sym == print_){
               scan(); check(lpar); Expr(); 
               if(sym == comma) {scan(); check(number);}
               check(rpar); check(semicolon);
            }
            else if(sym == lbrace){ Block(); }
            else if(sym == semicolon){ scan(); }    
            else { error("Statement parsing error");}
        }
        
        // Term = Factor {Mulop Factor}.
        private static void Term() {
            Factor();
            while(sym == times || sym == slash || sym == rem){
                scan(); 
                Factor();
            }
	}
        
        // Type = ident ["[" "]"].
        private static Struct Type() {
             Struct result;
             Obj currentType;
                     
            check(ident);
            currentType = Tab.find(t.string);
            
            if(currentType.kind != Obj.Type)
                error("Type expected");
            
            result = currentType.type;
             
             if(sym == lbrack){ 
                 scan(); 
                 result = new Struct(Struct.Arr, result);
                 check(rbrack);
               }
             
             return result;
         }
        
        // VarDecl = Type ident {"," ident } ";".
        public static void VarDecl() {
            Struct varType;
            String varName;
            
            varType = Type();
            
            check(ident);
            varName = t.string;
            Tab.insert(Obj.Var, varName, varType); 
           
            while(sym == comma){ 
                scan(); check(ident);
                varName = t.string;
                Tab.insert(Obj.Var, varName, varType);
            }
            check(semicolon);
        }
        
        private static void RecoveryDecl()
        {
                error("Invalid start of Declaration");
                while (!declSync.get(sym)){ scan(); }
                if (sym == semicolon) { scan(); }
                errDist = 0;
        }
        
	public static void parse() {
		// initialize symbol sets
		BitSet s;
		s = new BitSet(64); exprStart = s;
		s.set(ident); s.set(number); s.set(charCon); s.set(new_); s.set(lpar); s.set(minus);

		s = new BitSet(64); statStart = s;
		s.set(ident); s.set(if_); s.set(while_); s.set(read_);
		s.set(return_); s.set(print_); s.set(lbrace); s.set(semicolon);

                s = new BitSet(64); statSync = s;
                s.set(if_); s.set(while_); s.set(read_);
		s.set(return_); s.set(print_); s.set(lbrace); s.set(semicolon);
                s.set(eof);
                
                s = new BitSet(64); declSync = s;
		s.set(lbrace); s.set(void_); s.set(eof);
                s.set(final_); s.set(class_);
                
		s = new BitSet(64); statSeqFollow = s;
		s.set(rbrace); s.set(eof);

		s = new BitSet(64); declStart = s;
		s.set(final_); s.set(ident); s.set(class_);

		s = new BitSet(64); declFollow = s;
		s.set(lbrace); s.set(void_); s.set(eof);

		// start parsing
		errors = 0; errDist = 3;
		scan();
		Program();
		if (sym != eof) error("end of file found before end of program");
	}

}








