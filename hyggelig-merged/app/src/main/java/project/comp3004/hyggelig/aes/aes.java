// Gabriel: Commented out some code, since it needs a later version of the SDK and I don't have time to make it work.

package project.comp3004.hyggelig.aes;

//imported libraries
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.spec.KeySpec;
import java.security.SecureRandom;
import java.security.*;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File; 
import java.io.FileOutputStream; 
import java.io.OutputStream; 
import java.nio.file.*;
import java.io.File;

/*
Name: aes
Description:
	Contains a collection of functions used to encrypt and decrypt various forms of data using the AES cipher
*/
public class aes {

	//stores in global private encapsulated variables for extra security
	private static byte[] secretMessage;
	private static SecretKey secKey;

	/*
	Name: clearMemory
	Input: None
	Output: sets secret message and secret key to null
	Description:
		A simple method to clear variables which contain encrypted information
	*/
	// Gabriel: This actually does NOT clear the variables from memory, it just clears the references to them.
	public static void clearMemory() {
		secretMessage = null;
		secKey = null;
	} //END clearMemory

	/*
	Name: Main
	Input: 
		String[] args - user defined set of command line arguments
			[0] - contains the number of bits for the AES cipher (128, 192, 256)
			[1] - contains the user key for the data
			[2] - contains the file to be encrypted
			[3] - Request save to file or save to byte array
	Output: None
	Description: The entry point function for the program
		First step is to convert the users password into valid password the AES cipher can use
		The second step is to convert that password into an AES key
		The final step is to use the key to convert the data
	*/
    /*public static void main(String[] args) throws Exception {
		//encrypt the data
		secretMessage = encryptFile(args, System.getProperty("user.dir"));
		//write the data
		writeFiles(2, secretMessage, System.getProperty("user.dir")+ "\\" + args[2]);
		//the new file has .AES appended to it
		args[2] += ".AES";
		//open and decrypt the new file
		secretMessage = decryptFile(args, System.getProperty("user.dir"));
		//the appended ending .AES is removed so remove it from the file name string
		args[2] = args[2].substring(0, args[2].length() - 4);
		//write the decrypted data and prepend New to its file name
		writeFiles(1, secretMessage, System.getProperty("user.dir") + "\\New_" + args[2]);
		
		//clear the unencrypted data
		clearMemory();
    }*/ //END main

	/*
	Name: decryptFile
	Input:
		String[] args - user defined set of command line arguments
			[0] - contains the number of bits for the AES cipher (128, 192, 256)
			[1] - contains the user key for the data
			[2] - contains the file to be encrypted
			[3] - Request save to file or save to byte array
		String path - path to the file were looking to decrypt
			For example: C:/users/billy/encriptedStuffFolder
	Output:
		returns a byte array representation of the decrypted data
	Description:
		Takes in a file name, a path, a bit size, and a password and decrypts the file
	*/
	/*public static byte[] decryptFile(String[] args, String path) throws Exception {
		//open the file and get its contents
		Path fileDirectory = Paths.get(path + "\\" + args[2]);
		byte[] cipherText = Files.readAllBytes(fileDirectory);
		//generate a hash key using the password and bit length
		secKey = generateHash(Integer.parseInt(args[0]), args[1]);
		//run the AES cipher using the given parameters to get the secret message in plane text
		return decrypt(cipherText, secKey);
	}*/ //END decryptFile

	/*
	Name: writeFiles
	Input:
		int type - this is the type of output
			1 - output byte data in file format for decrypted data
			2 - output byte data in file format for encrypted data
		byte[] data - this is the data to be written to a file
		String path - this is the path to the new file to be created/overwritten
	Output:
		Outputs a new file containing information stored in 'data' at specified location 'path'
	Description:
		A simple function which can be called to write files after they are encrypted or decrypted
	*/
	public static void writeFiles(int type, byte[] data, String path) throws Exception {
		if (type == 1) { //write a decrypted byte stream
			try /*(FileOutputStream output = new FileOutputStream(path))*/ {
				FileOutputStream output = new FileOutputStream(path);	// Gabriel: Above try doesn't work in Java 6. Minor fix.
				output.write(data); //write byte data to output stream
			} //END TRY
			catch ( Exception e )
			{
				// Gabriel: TODO: Print an error message or something.
			}
		}
		else if (type == 2) { //write an encrypted byte stream
			//Initialize the file output stream
			OutputStream os = new FileOutputStream(path + ".AES");

			//Write the byte array containing the encrypted data into the above stream
			os.write(data);

			//Close the stream
			os.close();
		} //END IF
		//remove the secret encrypted data from memory
		clearMemory();
	} //END writeFiles
	/*
	Name: encryptFile
	Input:
		String[] args - user defined set of command line arguments
			[0] - contains the number of bits for the AES cipher (128, 192, 256)
			[1] - contains the user key for the data
			[2] - contains the file to be encrypted
			[3] - Request save to file or save to byte array
		String path - path to the file were looking to encrypt
			For example: C:/users/billy/importantStuffFolder
	Output:
		The function itself is of return void type, however the function saves
		information in a file at the location specified by the path variable.
		Please note that the new file has the same name as the old with the appendix .AES attached
			For example, say the original file is: image.png
			Then the new file will be called: image.png.AES
	Description:
		Function takes a bit size, a password, a file name, and a path and
		encrypts the file specified at path and saves it in the same folder.
	*/
	/*public static byte[] encryptFile(String[] args, String path) throws Exception {
		//get the directory to the current file
		Path fileDirectory = Paths.get(path + "\\" + args[2]);
		//open the file and get its contents
		secretMessage = Files.readAllBytes(fileDirectory);
		//generate a hash key using the password and bit length
		secKey = generateHash(Integer.parseInt(args[0]), args[1]);
		//run the AES cipher using the given parameters
		byte[] cipherText = encrypt(secretMessage, secKey);
		//clear the secret message
		clearMemory();

		return cipherText;
	}*/ //END encryptFile

	/*
	Name: generateHash
	Input: 
		int bits - this is the number of bits used in the cipher
			For example: AES-128 would use 128 as its argument for bits
		String password - this is the users password
	Output:
		Creates a AES formatted hash
	Description:
		Converts a user password into an AES hash using PBKDF2 key derivation functions
	*/
	public static SecretKey generateHash(int bits, String password) throws Exception {
		//create a buffer
		byte[] salt = new byte[8];
		//get an instances of PBKDF2 key maker
		SecretKeyFactory keyFactor = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		//input the user password data
		PBEKeySpec specifications = new PBEKeySpec(password.toCharArray(), salt, 8192, 256);
		//generate a hash
		SecretKey tempKey = keyFactor.generateSecret(specifications);
		//convert hash to AES hash
		SecretKey secretCode = new SecretKeySpec(tempKey.getEncoded(), "AES");
		//return the hash
		return secretCode;
	} //END generateHash

    /*
	Name: encrypt
	Input: data - a set of characters in planetext to be converted back to encrypted characters
		   key - the secret key used to convert between AES and non-AES text
	Output: return cipherText - this is encrypted text
	Description: Converts an array of planetext characters back into AES encrypted characters using the password key
	*/
    public static byte[] encrypt(byte[] data,SecretKey key) throws Exception{
		//Java 7 uses AES with PKCS5Padding
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = cipher.doFinal(data);
        return cipherText;
    } //END encrypt

    /*
	Name: decrypt
	Input: cipherText - a set of characters in AES to be converted back to unencrypted characters
		   key - the secret key used to convert between AES and non-AES text
	Output: return new String(text) - this is unencrypted text
	Description: Converts an array of encrypted AES characters back into plane text using the AES cipher and the AES password key
	*/
    public static byte[] decrypt(byte[] cipherText, SecretKey key) throws Exception {
		//Java 7 uses AES with PKCS5Padding
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] text = cipher.doFinal(cipherText);
        return text;
    } //END decrypt


	// Gabriel: Overloading the above functions so that we can en/decrypt straight from an application.
	// Gabriel: Android's pretty picky sometimes about who's writing to which file, among other things.
	// Gabriel: FIXME: Implemented quickly; not audited.
	public static byte[] encrypt(byte[] data, int bits, String password) throws Exception{
		//Java 7 uses AES with PKCS5Padding
		Cipher cipher = Cipher.getInstance("AES");
		secretMessage = data;
		secKey = generateHash(bits, password);
		cipher.init(Cipher.ENCRYPT_MODE, secKey);
		byte[] cipherText = cipher.doFinal(secretMessage);
		clearMemory();
		return cipherText;
	}
	// Gabriel: FIXME: Implemented quickly; not audited.
	public static byte[] decrypt(byte[] cipherText, int bits, String password) throws Exception {
		//Java 7 uses AES with PKCS5Padding
		Cipher cipher = Cipher.getInstance("AES");
		secKey = generateHash(bits, password);
		cipher.init(Cipher.DECRYPT_MODE, secKey);
		byte[] text = cipher.doFinal(cipherText);
		clearMemory();
		return text;
	}
} //END aes