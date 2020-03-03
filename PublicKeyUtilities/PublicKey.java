/*
Author Name: Connor Stewart
*/

//imported libraries
import com.didisoft.pgp.*;
import java.io.File;  

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
				0: AES_128
				1: AES_192
				2: AES_256
				3: TWOFISH
				4: BLOWFISH
				5: CAST5
				6: DES
				7: IDEA
				8: SAFER
				9: TRIPLE_DES
				10: CAMELLIA_128
				11: CAMELLIA_192
				12: CAMELLIA_256
				13: NONE
			[1]: this the users id
			[2]: this is the users private key password
			[3]: checks if output is ASCII or Binary, must be a boolean value (i.e. "true")
			[4]: this is the name of the new key file being generated
			[5]: This is the hashing algorithm to be used
				0: SHA512
				1: SHA384
				2: SHA256
				3: SHA1
				4: MD5
				5: RIPEMD160
				6: MD2
			[6]: This is the compression method to use
				0: ZIP
				1: UNCOMPRESSED
	Output:
		Outputs a public key and a private key named to the users specifications
	Description:
		Generates a public and a private key pair for future encryption
	*/
	public static int generateKeyPair(String[] args) throws Exception {
		//check the correct number of arguments were used
		if (args.length != 7) {
			return -1; //incorrect number of arguments error
		} //END IF
		//check formatting of true/false indexes
		if (checkInput(args, 3) == false) {
			return -2; //incorrect formatting of arguments error
		} //END IF

		//make a keystore memory object to contain the public and private keys
		KeyStore keyStoreObject = new KeyStore(args[4], args[2]);
		
		//symmetric key algorithm being which can be selected from by the caller
		String[] cyphers = new String[] {CypherAlgorithm.AES_128,
			CypherAlgorithm.AES_192, CypherAlgorithm.AES_256,
			CypherAlgorithm.TWOFISH, CypherAlgorithm.BLOWFISH,
			CypherAlgorithm.CAST5, CypherAlgorithm.DES,
			CypherAlgorithm.IDEA, CypherAlgorithm.SAFER,
			CypherAlgorithm.TRIPLE_DES, CypherAlgorithm.CAMELLIA_128,
			CypherAlgorithm.CAMELLIA_192, CypherAlgorithm.CAMELLIA_256,
			CypherAlgorithm.NONE};
		
		//hashing algorithms which can be used
		String[] hashAlgs = new String[] {HashAlgorithm.SHA512, 
			HashAlgorithm.SHA384, HashAlgorithm.SHA256, 
			HashAlgorithm.SHA1, HashAlgorithm.MD5,
			HashAlgorithm.RIPEMD160, HashAlgorithm.MD2};
		//hashing algorithm selected to use
		String[] hashAlg = new String[] {hashAlgs[Integer.parseInt(args[5])]};
		//compression algorithms
		String[] compressionAlgs = new String[] {CompressionAlgorithm.ZIP, CompressionAlgorithm.UNCOMPRESSED};
		//compression algorithm being used
		String[] compressionAlg = new String[] {compressionAlgs[Integer.parseInt(args[6])]};
		//cipher the user has chosen
		String[] cypherAlg = new String[] {cyphers[Integer.parseInt(args[0])]};

		long expiryDate = 0; //no expiry Date
		int keySize = 4096; //the key size in bytes
		
		//generate the keys
		keyStoreObject.generateKeyPair(keySize, args[1], keyAlgorithm, args[2], compressionAlg, hashAlg, cypherAlg, expiryDate);
		
		//export the public/private key to the filesystem
		//make a filename for the public key
		String publicKeyName = args[4]+"Public.pkr";
		//now we export the public key with its sub keys with the specified user id
		keyStoreObject.exportPublicKey(publicKeyName, args[1], Boolean.parseBoolean(args[3]));
		//make a filename for the private key
		String privateKeyName = args[4]+"Private.skr";
		//now we export the private key with its sub keys with the specified user
		keyStoreObject.exportPrivateKey(privateKeyName, args[1], Boolean.parseBoolean(args[3]));

		//remove the keystore object, it is no longer needed
		//specify the name of the keystore object in the current working directory (i.e. add './' to the start of the filename)
		String s = "./" + args[4];
		//open a file pointer for the keystore object
		File filePointer = new File(s);
		//delete the file for the file pointer
		filePointer.delete();
		return 0; //return zero for success
	} //END generateKeyPair
	
	/*
	Name:
		encrypt
	Input:
		args - These are the input arguments provided by the caller
			[0]: this is the name of the file to be encrypted
			[1]: this is the name of the public key file to use
			[2]: this is the name of the output file being created
			[3]: checks if output is ASCII or Binary, must be a boolean value (i.e. "true")
			[4]: checks if integrity checking information should be added for GnuPG compatibility, must be a boolean value
	Output:
		An encrypted version of the input file made with the users public key
	Description:
		Allows for the encryption of files using a public key
	*/
	public static int encrypt(String args[]) throws Exception {
		//create a library instance for encryption
		PGPLib library = new PGPLib();
		//check if input is of the correct length
		if (args.length != 5) {
			//return negative one error
			return -1;
		}
		//check if input is formatted correctly
		else if ((checkInput(args, 3) && checkInput(args, 4)) == false) {
			//return negative two error
			return -2;
		} //END IF
		library.encryptFile(args[0], args[1], args[2], Boolean.parseBoolean(args[3]), Boolean.parseBoolean(args[4]));
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
		//create a library instance for encryption
		PGPLib library = new PGPLib();
		//check if input is of the correct length
		if (args.length != 4) {
			//return negative one for error
			return -1;
		} //END IF
		//decrypt the file with the private key file
		String decryptedFileNume = library.decryptFile(args[0], args[1], args[2], args[3]);
		return 0; //return zero for success
	} //END decrypt
	
	/*
	Name:
		checkInput
	Input:
		args - contains the users input parameters from entryPoint
		index - the index to be verified
	Output:
		A boolean value telling us if the input is formatted correctly
	Description:
		Checks if the last two elements in the string array are formatted correctly
	*/
	private static boolean checkInput(String[] args, int index) {
		return ("true".equals(args[index].toLowerCase()) || "false".equals(args[index].toLowerCase()));
	} //END checkInput
	
	public static void main(String args[]) throws Exception {
		//Example code
		
		//To Compile from BASH, type:
		//javac -cp .:library/\* PublicKey.java
		//To run from BASH, type:
		//java -cp .:library/\* PublicKey [Args]
		
		//To run, use the following command: java -cp .:library/\* PublicKey 0 Connor Password27 false encryptedKeyStore 0 0
		//generateKeyPair(args);
		
		//To run, use the following command: java -cp .:library/\* PublicKey tester.txt encryptedKeyStorePublic.pkr encryptedOutput false false
		//encrypt(args);
		
		//To run, use the following command: java -cp .:library/\* PublicKey encryptedOutput encryptedKeyStorePrivate.skr Password27 decryptedOutput.txt
		//decrypt(args);
	} //END main
} //END PublicKey
