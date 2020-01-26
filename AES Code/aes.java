//imported libraries
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.io.File;
 
/*
Name: aes
Discription:
	Contains a collection of functions used to encrypt and decrypt various forms of data using the AES cypher
*/
public class aes {
	/*
	Name: Main
	Input: String[] - user defined set of command line arguments
		[0] - contains the number of bits for the AES cipher (128, 192, 256)
		[1] - cotnains the user key for the data
		[2] - contains the file to be encrypted
	Output: None
	Discription: The entry point function for the program
		First step is to convert the users password into valid password the AES cipher can use
		The second step is to convert that password into an AES key
		The final step is to use the key to convert the data
	*/
    public static void main(String[] args) throws Exception {
		String message = readFile(args[2]);
		
        SecretKey secKey = convertToAESHash(Integer.parseInt(args[0]), args[1]);
        byte[] cipherText = encrypt(message, secKey);
        String decryptedText = decrypt(cipherText, secKey);
		System.out.println("Original: \n" + message);
        System.out.println("AES Key (Hexidecimal): \n" + DatatypeConverter.printHexBinary(secKey.getEncoded()));
        System.out.println("Encrypted data (Hexidecimal): \n" + DatatypeConverter.printHexBinary(cipherText));
        System.out.println("Descrypted data: \n" + decryptedText);
    } //END main
	
	public static String readFile(String filename) throws Exception {
		return new Scanner(new File(filename)).useDelimiter("\\Z").next();
	}
	
	/*
	Name: convertToAESHash
	Input:
		keyBits - the number of bits in the AES key being used (i.e. 128)
		password - the password being used to open the AES file
	Output:
		SecretKey - an AES secret key representation of the password the user entered
	Discription:
		generates a secret key using a user password
	*/
	public static SecretKey convertToAESHash(int keyBits, String password) {
		//based on the number of bits in the AES key, find the number of characters in the string
		int digits = 0;
		if (keyBits == 128) {
			digits = 16;
		}
		else if (keyBits == 192) {
			digits = 16;
		}
		else if (keyBits == 256) {
			digits = 22;
		}
		else {
			return null;
		}
		//find the length of the currently inputted string
		int passLength = password.length();
		//set the psudopassword to password
		String psudoPassword = password;
		//current character to add
		int character = 0;
		
		if (passLength > digits) {
			return null;
		} //END IF

		//for the number of characters missing from the AES character limit, append characters from the begining of the string to the end
		for (int i = 0; i < digits-passLength; i++) {
			psudoPassword = psudoPassword+password.charAt(character);
			//if the whole string has been repeated, start again from the begining
			if (character == passLength-1) {
				character = 0;
			}
			else { //get the next character next iteration of the for loop
				character += 1;
			} //END IF
		} //END FOR
		System.out.println(psudoPassword);
		//convert the psudopassword to a character array
		char[] charPass = psudoPassword.toCharArray();

		//create a new string builder variable
		StringBuilder intPass = new StringBuilder();
		
		//for every character in the psudopassword
		for (char x : charPass) {
			//convert the character from decimal to hexidecimal character code
			intPass.append(Integer.toHexString((int) x).toUpperCase());
		} //END FOR

		//remove the last hexidecimal digit to get an even value of 32 hexidecimal digits
		//this occurs because 256 bit AES cannot be converted from keyboard characters without having an extra hexidecimal digit
		if (keyBits == 256) {
			intPass.deleteCharAt(intPass.length() - 1);
		} //END IF
		
		//convert the hexidecimal representation of the psudopassword into a byte stream
		byte[] decodedKey = Base64.getDecoder().decode(intPass.toString());
		
		//convert byte stream into an AES secret key
		SecretKey secretKeyFormatted = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		//return the secret key
		return secretKeyFormatted;
	} //END convertToAESHash
     
    /*
	Name: encrypt
	Input: data - a set of characters in planetext to be converted back to encrypted characters
		   key - the secret key used to convert between AES and non-AES text
	Output: return cipherText - this is encrypted text
	Discription: Converts an array of planetext characters back into AES encrypted characters using the password key
	*/
    public static byte[] encrypt(String data,SecretKey key) throws Exception{
		//Java 7 uses AES with PKCS5Padding
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = cipher.doFinal(data.getBytes());
        return cipherText;
    } //END encrypt
     
    /*
	Name: decrypt
	Input: cipherText - a set of characters in AES to be converted back to unencrypted characters
		   key - the secret key used to convert between AES and non-AES text
	Output: return new String(text) - this is unencrypted text
	Discription: Converts an array of encrypted AES characters back into plane text using the AES cipher and the AES password key
	*/
    public static String decrypt(byte[] cipherText, SecretKey key) throws Exception {
		//Java 7 uses AES with PKCS5Padding
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] text = cipher.doFinal(cipherText);
        return new String(text);
    } //END decrypt
} //END aes