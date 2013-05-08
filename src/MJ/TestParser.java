/* MicroJava Parser Tester
   ========================
   Place this file in a subdirectory MJ
   Compile with
     javac MJ\Scanner.java MJ\Parser.java MJ\TestParser.java
   Run with
     java MJ.TestParser <inputFileName>
*/
package MJ;

import MJ.SymTab.Tab;
import java.io.*;

public class TestParser {

	// Main method of the parser tester
	public static void main(String args[]) {
		Token t;
		if (args.length > 0) {
			String source = args[0];
			try {
                                ErrorHandler.Init(null);
				Scanner.init(new InputStreamReader(new FileInputStream(source)));
                                Tab.init();
				Parser.parse();
				System.out.println(Parser.errors + " errors detected");
			} catch (IOException e) {
				System.out.println("-- cannot open input file " + source);
			}
		} else System.out.println("-- synopsis: java MJ.TestParser <inputfileName>");
	}

}