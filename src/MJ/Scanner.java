/* MicroJava Scanner (HM 06-12-28)
   =================
*/
package MJ;
import MJ.Error;
import java.io.*;

public class Scanner {
	private static final char eofCh = '\u0080';
	private static final char eol = '\n';
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
	private static final String key[] = { // sorted list of keywords
		"class", "else", "final", "if", "new", "print",
		"program", "read", "return", "void", "while"
	};
	private static final int keyVal[] = {
		class_, else_, final_, if_, new_, print_,
		program_, read_, return_, void_, while_
	};

	private static char ch;			// lookahead character
	public  static int col;			// current column
	public  static int line;		// current line
	private static int pos;			// current position from start of source file
	private static Reader in;  	// source file reader
	private static char[] lex;	// current lexeme (token string)

	//----- ch = next input character
	private static void nextCh() {
		try {
			ch = (char)in.read(); col++; pos++;
			if (ch == eol) {line++; col = 0;}
			else if (ch == '\uffff') ch = eofCh;
		} catch (IOException e) {
			ch = eofCh;
		}
	}

	//--------- Initialize scanner
	public static void init(Reader r) {
		in = new BufferedReader(r);
		lex = new char[64];
		line = 1; col = 0;
		nextCh();
	}
	
        // -------- Binary Search over keywords
	public static int searchString(String[] names, String key) {  
		int first = 0;  
		int last  = names.length-1;  
  
		while (first <= last) {  
			int mid = (first + last) / 2;  
			if (key.compareTo(names[mid]) < 0) {  
				last = mid - 1;  
			} else if (key.compareTo(names[mid]) > 0) {  
				first = mid + 1;  
			} else {  
				return mid;  
			}  
		}  
		return -1;
	} 
	
        // -------- Read an identifier or keywords
	private static void readName(Token t){
		//put the first letter into t.string
		StringBuilder builder = new StringBuilder();
                //read letter and digit and store them into the StringBuilder
		while (Character.isLetterOrDigit(ch) ||ch == '_' )
		{
			builder.append(ch);
			nextCh();
		}
		// lookup the name in the keyword table
		int index = searchString(key, builder.toString());
		if(index >= 0) // if a keywork is found, t.kind is the corresponding keyVal 
			t.kind = keyVal[index];
                else{ // else t.kind is ident
                    t.kind = ident;
                }
                t.string = builder.toString();
	}
	
        // -------- Read numbers
	private static void readNumber(Token t){
		StringBuilder builder = new StringBuilder();
                // read digits and store them into the StringBuilder
		while (Character.isDigit(ch)){
			builder.append(ch);
			nextCh();
		}
		try{ // try to convert the StringBuilder into a number
                        // store the number value to t.val
			t.val = Integer.parseInt(builder.toString());
		}
		catch(Exception e){ // report an error in case of overflow
                        ErrorHandler.Handle(new Error(t, "The entered number doesn't feet int capacity", e));             
		}
		t.kind = number;
	}
	
        // -------- Read Char on given specification
	private static void readCharCon(Token t){ 
                char result = 0;
            //Step 1 => If the character is a letter
                nextCh();
		if(Character.isLetter(ch))
                {
                    t.string = Character.toString(ch);
                    result = ch;
                }
           //Step 2 => If the character is a slash, then valid character only
                if(ch == '\\'){   
                    nextCh();		
                    if(ch == 'n'){ t.string = "\n"; result = '\n'; }
                    if(ch == 't'){ t.string = "\t"; result = '\t'; }
                    if(ch == 'r'){ t.string = "\r"; result = '\r'; }   
                }   
           //Step 3 => if the end of the character is following by a "'"
		nextCh();
		if(ch == '\''){
			t.kind = charCon;
                        try{
                            t.val = result;
                        }
                        catch(Exception e){
                          ErrorHandler.Handle(new Error(t, "The entered character couldn't be affected", e));
                        } 
		}
		else {
                    t.kind = none;
                    ErrorHandler.Handle(new Error(t, "The entered character is invalid", null));
                }
                nextCh();   
	}

	//---------- Return next input token
	public static Token next() {
		while (ch <= ' ') nextCh(); // skip blanks, tabs, eols
		Token t = new Token(); t.line = line; t.col = col;
		switch (ch) {
			case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j': case 'k':
					case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u':
					case 'v': case 'w': case 'x': case 'y': case 'z': case 'A': case 'B': case 'C': case 'D': case 'E': 
					case 'F': case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': 
					case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': 
					case 'Z':
				readName(t); 
				break;
			case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
				readNumber(t); 
				break;
			case ';': 
				nextCh(); 
				t.kind = semicolon; 
				break;
			case '.': 
				nextCh(); 
				t.kind = period; 
				break;
			case eofCh: 
				t.kind = eof; 
				break; // no nextCh() any more
			case '=': 
				nextCh();
				if (ch == '=') { 
					nextCh(); 
					t.kind = eql; 
				} else t.kind = assign;
				break;
                            
			case '/': nextCh();
				if (ch == '/') {
				do nextCh(); 
				while (ch != '\n' && ch != eofCh);
					t = next(); // call scanner recursively
				} else t.kind = slash;
				break;
			case '+':
				nextCh();
				t.kind = plus;
				break;
			  case '-':
				nextCh();
				t.kind = minus;
				break;
			  case '*':
				nextCh();
				t.kind = times;
				break;
			  case '<':
				nextCh();
				if (ch == '=') { nextCh(); t.kind = leq; } 
				else t.kind = lss;
				break;
			  case '>':
				nextCh();
				if (ch == '=') { nextCh(); t.kind = geq; } 
				else t.kind = gtr;
				break;
			  case '!':
				nextCh();
				if (ch == '=') { nextCh(); t.kind = neq; } 
                                else{ t.kind = none; ErrorHandler.Handle(new Error(t, "The entered symbol is invalid", null)); }
				break;
			  case ',':
				nextCh();
				t.kind = comma;
				break;
			  case '%':
				nextCh();
				t.kind = rem;
				break;
			  case '(':
				nextCh();
				t.kind = lpar;
				break;
			  case ')':
				nextCh();
				t.kind = rpar;
				break;
			  case '[':
				nextCh();
				t.kind = lbrack;
				break;
			  case ']':
				nextCh();
				t.kind = rbrack;
				break;
			  case '{':
				nextCh();
				t.kind = lbrace;
				break;
			  case '}':
				nextCh();
				t.kind = rbrace;
				break;
			case '\'':
				readCharCon(t);
				break;
			default: 
				nextCh(); 
				t.kind = none;
                                ErrorHandler.Handle(new Error(t, "The token is not recognized", null));
				break;
		}
		return t;
		} // ch holds the next character that is still unprocessed
}







