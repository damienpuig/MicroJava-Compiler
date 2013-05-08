/*
 * The Error class is an error wrapper, used in Scanner and parser
 * to represent errors in a leasy fashion style.
 * The class use the current token, dates the error, with preformat developer's messages
 * and Java Exception if needed.
 */
package MJ;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Damien, Thibault
 */
public class Error {
    //Current Token.
    public  Token T;
    //Date of the error.
    public String DatetimeOfError;
    // Custom Message.
    public  String Msg;
    //Exception if needed.
    public  Exception SystemExc;
    
    /*
     * Constructor.
     */
    public Error(Token t, String msg, Exception systemExc)
    {
        T = t;
        DatetimeOfError = getDateTime();
        Msg = msg;
        SystemExc = systemExc;
    }
    
    /*
    * Format the datetime as a string using a simple date format.
    */
     private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

}
