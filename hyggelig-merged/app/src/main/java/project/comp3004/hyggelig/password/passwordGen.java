package project.comp3004.hyggelig.password;

//imported packages
import java.util.Random;
import java.io.PrintWriter;

/*
Name: passwordGen
Input:
	argv:
		[0]: this instructs the type of password to create
			1:  alphabet
			2:	number+alphabet+symbols
			3:	number+alphabet+symbols+foreign characters
			4:	all valid bits
		[1]: this instructs the length of the password
			for example, "12"
		[2]: this instructs if there should be capitals for alphabetic passwords
			0:  no capitals
			1:  capitals
		[3]: this is used to create a header for the password
			for example, if bobby is entered and the password is 8 digits, then:
				bobby123 is a possible output from the program
Output:
	A password following the specified constraints will be outputted
Description:
	A program that creates passwords based on user request
	Program can be used from a command line in which case output is password into both a text file and the terminal window
	If the program is created inside another program, then the password can be created and extracted via the getNewPassword assessor function
	launch.json has pre-loaded arguments to allow the program to be executed from Microsoft Visual Studio
	However it is possible to run the program from any IDE or from inside another program
*/
public class passwordGen {
    //declare as private in order to encapsulate sensitive data
    public static int outputFormat = 0; //use no quotations in output by default
    private static StringBuilder newPassword = new StringBuilder(""); //contains the password being built
    private static Random random; //keep random function encapsulated for security, otherwise hackers can see the utf/ascii 'digit' before it's assigned to a character

    //entry point for the class, can be called externally
    public static String newPassword(String[] argv) {
        //breaks array parameters into separate commands
        //first command is the password type
        int passwordType = Integer.parseInt(argv[0]);
        //second command is the password length
        int passwordLength = Integer.parseInt(argv[1]);
        //third command is allows for there to be capitals or not for the standard alphabetical password
        int capitalLettersAllow = Integer.parseInt(argv[2]);

        //if four parameters are entered
        if (argv.length == 4) {
            //we set the new password to begin with the user defined parameter
            setNewPassword(argv[3]);
            //we reduce the password length to fit this new parameter
            passwordLength -= argv[3].length();
        }
        else {
            //otherwise, just clear out the old password
            clearNewPassword();
        } //END IF

        //controls the ascii lower-bound for the standard character output
        int ASCII_lowerbound;

        if (capitalLettersAllow == 0) { //set ascii lower-bound to include capitals
            ASCII_lowerbound = 97;
        }
        else {
            ASCII_lowerbound = 65; //set ascii lower-bound to include only lowercase
        } //END IF

        if (passwordType == 1) { //output alphabetical password
            alphabeticGenerator(passwordLength, ASCII_lowerbound);
        }
        else if (passwordType == 2) { //output alphanumerical password with symbols
            alphaNumericGenerator(passwordLength);
        }
        else if (passwordType == 3) { //output alphanumerical password including foreign symbols
            globalNumericGenerator(passwordLength);
        }
        else if (passwordType == 4) { //output any valid utf-8 character string
            forgeinNumericGenerator(passwordLength);
        } //END IF

        //return the password to the caller
        return getNewPassword(outputFormat);
    } //END newPassword

    //assessor for the private newPassword variable
    public static String getNewPassword(int format) {
        if (format == 1) { //return with quotations
            return "'" + newPassword.toString() + "'";
        } //END IF
        //else assume the default output is requested
        return newPassword.toString();
    } //END getNewPassword

    //sets the newPassword variable to be something new
    private static void setNewPassword(String userString) {
        newPassword = new StringBuilder(userString);
    } //END setNewPassword

    //sets the newPassword variable to be a blank string
    private static void clearNewPassword() {
        newPassword = new StringBuilder("");
    } //END clearNewPassword

    //entry point for the object if run as a program
   public static void main(String[] argv) {
        /*newPassword(argv);
        System.out.println(newPassword); //print the characters to the console
        System.out.println(newPassword.length()); //print the length of the characters to the console
        try(PrintWriter out = new PrintWriter("output.txt")) { //try to open a file
            out.println(newPassword); //output characters to file
        }
        catch (Exception e) {
            System.out.println("ERROR, file output.txt does not exist, exiting");
            return; //exit if the file cannot be opened
        } //END TRY*/
    } //END main

    public static int randomNumberGenerator(int lowerBound, int upperBound) {
        //define a new random variable
        random = new Random();

        //randomly choose between uppercase and lowercase characters if applicable
        if (lowerBound == 65) { //if an uppercase is the lower bound
            if (random.nextBoolean() == true) { //set to only lowercase
                lowerBound = 97; //lowercase a
            }
            else { //if false...
                upperBound = 90; //set to uppercase Z
            } //END IF
        } //END IF

        //return the next random variable in the given range
        return random.nextInt(upperBound - lowerBound + 1) + lowerBound;
    } //END randomNumberGenerator

    //generates characters consisting of the standard Latin alphabet
    public static void alphabeticGenerator(int length, int ASCII_lowerbound) {
        int leftASCII = ASCII_lowerbound; //corresponds to lowercase a OR uppercase A
        int rightASCII = 122; //corresponds to lowercase z

        //create a new random variable for the following string
        random = new Random();

        //for the number of user requested characters, add characters of the requested type
        for (int i = 0; i < length; i++) {
            newPassword.append((char)randomNumberGenerator(leftASCII, rightASCII));
        } //END FOR
    } //END alphabeticGenerator

    //generates characters from the standard ascii table
    public static void alphaNumericGenerator(int length) {
        int leftASCII = 33; //the ! symbol
        int rightASCII = 126; //the ~ symbol

        //create a new random variable for the following string
        random = new Random();

        //for the number of user requested characters, add characters of the requested type
        for (int i = 0; i < length; i++) {
            newPassword.append((char)randomNumberGenerator(leftASCII, rightASCII));
        } //END FOR
    } //END alphaNumericGenerator

    //generates characters from the standard utf-8 encoder, characters selected come from common languages
    public static void globalNumericGenerator(int length) {
        int leftUtf8 = 33; //start with the ! character
        int rightUtf8 = 6688; //covers most alphabets without support characters

        //create a new random variable for the following string
        random = new Random();

        //for the number of user requested characters, add characters of the requested type
        for (int i = 0; i < length; i++) {
            newPassword.append((char)randomNumberGenerator(leftUtf8, rightUtf8));
        } //END FOR
    } //END globalNumericGenerator

    //select any valid utf-8 character, including characters which don't normally get inputted from a keyboard
    public static void forgeinNumericGenerator(int length) {
        int leftUtf8 = 1; //the 'start of heading' character, also commonly a smiley-face depending on the encoder being used
        //all utf-8 characters that are valid symbols in some language
        //this includes characters in the multilingual including supplementary characters for controlling formats
        //only character excluded is 0, the null terminator
        int rightUtf8 = 8*8*255;

        //create a new random variable for the following string
        random = new Random();

        //for the number of user requested characters, add characters of the requested type
        for (int i = 0; i < length; i++) {
            newPassword.append((char)randomNumberGenerator(leftUtf8, rightUtf8));
        } //END FOR
    } //END forgeinNumericGenerator
} //END passwordGen