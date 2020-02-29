package project.comp3004.hyggelig.publickey;

/*
Author Name: Connor Stewart
*/

//imported libraries
import com.didisoft.pgp.*;


/*
Name:
	PublicKey
Input:
	args - string array of arguments, format depends upon method being called
Output:
	Encrypted file, Decrypted file, and public/private key files
Description:
	A set of utilities used for public key encryption
*/
public class PublicKey {
	//open the openPGP library object for the class
	private static PGPLib pgpEntryPoint = new PGPLib();
	//the algorithm for the keys being generated/used
	private static String keyAlgorithm = KeyAlgorithm.RSA;
	
	/*
	Name:
		generateKeyPair
	Input:
		args - These are the input arguments provided by the caller
			[0]: this is the name cipher to be used, must be an integer value
				0: AES128
				1: AES192
				2: AES256
				3: CAT5
				4: TWOFISH
			[1]: this the users id
			[2]: this is the users private key password
			[3]: checks if output is ASCII or Binary, must be a boolean value (i.e. "true")
			[4]: this is the name of the new key file being generated
	Output:
		Outputs a public key and a private key named to the users specifications
	Description:
		Generates a public and a private key pair for future encryption
	*/
	public static void generateKeyPair(String[] args) throws Exception {
		//make a keystore memory object to contain the public and private keys
		KeyStore keyStoreObject = new KeyStore(args[4], args[2]);
		
		//symmetric key algorithm being which can be selected from by the caller
		String[] cyphers = new String[] {CypherAlgorithm.AES_128, CypherAlgorithm.AES_192, CypherAlgorithm.AES_256, CypherAlgorithm.CAST5, CypherAlgorithm.TWOFISH};
		
		//hashing algorithm to be used
		String[] hashAlg = new String[] {HashAlgorithm.SHA512};
		//compression algorithm being used
		String[] compressionAlg = new String[] {CompressionAlgorithm.ZIP};
		//cipher the user has chosen
		String[] cypherAlg = new String[] {cyphers[Integer.parseInt(args[0])]};

		long expiryDate = 0; //no expiry Date
		int keySize = 4096; //the key size in bytes
		
		//generate the keys
		keyStoreObject.generateKeyPair(keySize, args[1], keyAlgorithm, args[2], compressionAlg, hashAlg, cypherAlg, expiryDate);
	} //END generateKeyPair
	
	/*
	Name:
		encrypt
	Input:
		args - These are the input arguments provided by the caller
			[0]: this is the name of the file to be encrypted
			[1]: this is the name of the key file to use
			[2]: this is the name of the password for the key
			[3]: this is the name of the user ID for the recipient
			[4]: this represents the name of the newly encrypted output file being produced
			[5]: checks if output is ASCII or Binary, must be a boolean value (i.e. "true")
			[6]: checks if integrity checking information should be added for GnuPG compatibility, must be a boolean value
	Output:
		An encrypted version of the input file made with the users public key
	Description:
		Allows for the encryption of files using a public key
	*/
	public static int encrypt(String args[]) throws Exception {
		//make a keystore memory object to contain the public and private keys
		KeyStore keyStoreObject = new KeyStore(args[1], args[2]);
		//check if input is of the correct length
		if (args.length != 7) {
			//return negative one error
			return -1;
		}
		//check if input is formatted correctly
		else if (checkInput(args) == false) {
			//return negative two error
			return -2;
		} //END IF

		pgpEntryPoint.encryptFile(args[0], keyStoreObject, args[3], args[4], Boolean.parseBoolean(args[5]), Boolean.parseBoolean(args[6]));
		return 0; //return zero for success
	} //END entryPoint

	/*
	Name:
		decrypt
	Input:
		args - These are the input arguments provided by the caller
			[0]: this is the name of the file to be decrypted
			[1]: this is the name of the key file to use
			[2]: this is the string representation of the passphrase being used with the private key
			[3]: this is name for the new decrypted file being generated
	Output:
		A decrypted version of the inputted encrypted file, decrypted using the inputted private key and passphrase
	Description:
		Allows for the decryption of files using a private key
	*/
	public static int decrypt(String args[]) throws Exception {
		//make a keystore memory object to contain the public and private keys
		KeyStore keyStoreObject = new KeyStore(args[1], args[2]);
		//check if input is of the correct length
		if (args.length != 4) {
			//return negative one for error
			return -1;
		} //END IF
		
		//decrypt the file
		pgpEntryPoint.decryptFile(args[0], keyStoreObject, args[2], args[3]);
		return 0; //return zero for success
	} //END decrypt
	
	/*
	Name:
		checkInput
	Input:
		args - contains the users input parameters from entryPoint
	Output:
		A boolean value telling us if the input is formatted correctly
	Description:
		Checks if the last two elements in the string array are formatted correctly
	*/
	private static boolean checkInput(String[] args) {
		return ("true".equals(args[5].toLowerCase()) || "false".equals(args[5].toLowerCase())) && ("true".equals(args[6].toLowerCase()) || "false".equals(args[6].toLowerCase()));
	} //END checkInput
	
	public static void main(String args[]) throws Exception {
		//Example code
		
		//To Compile from BASH, type:
		//javac -cp .:library/\* PublicKey.java
		//To run from BASH, type:
		//java -cp .:library/\* PublicKey [Args]
		
		//To run, use the following command: java -cp .:library/\* PublicKey 0 Connor Password27 false encryptedKeyStore
		//generateKeyPair(args);
		
		//To run, use the following command: java -cp .:library/\* PublicKey tester.txt encryptedKeyStore Password27 Connor encryptedOutput false false
		//encrypt(args);
		
		//To run, use the following command: java -cp .:library/\* PublicKey encryptedOutput encryptedKeyStore Password27 decryptedOutput.txt
		//decrypt(args);
	} //END main
} //END PublicKey
