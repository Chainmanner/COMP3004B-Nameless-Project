package project.comp3004.hyggelig.publickey;

/*
Author Name: Connor Stewart
*/

//imported libraries
import com.didisoft.pgp.*;
import com.didisoft.pgp.exceptions.NoPublicKeyFoundException;

import java.io.File;
import java.util.List;


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
			[3]: checks if output is ASCII or Binary, must be a boolean value (i.e. "true") (Gabriel: true = ASCII)
			[4]: this is the name of the new key file being generated
			[5]: Number of days after which the key expires. (Added by Gabriel)
			[6]: Directory to store the generated public key. MUST end with a trailing slash. (Added by Gabriel)
			[7]: Directory to store the generated private key. MUST end with a trailing slash. (Added by Gabriel)
	Output:
		Outputs a public key and a private key named to the users specifications
	Description:
		Generates a public and a private key pair for future encryption
	*/
	public static void generateKeyPair(String[] args) throws Exception {
		//make a keystore memory object to contain the public and private keys
		KeyStore keyStoreObject = new KeyStore();//new KeyStore(args[4], args[2]);
		
		//symmetric key algorithm being which can be selected from by the caller
		String[] cyphers = new String[] {CypherAlgorithm.AES_128, CypherAlgorithm.AES_192, CypherAlgorithm.AES_256, CypherAlgorithm.CAST5, CypherAlgorithm.TWOFISH};
		
		//hashing algorithm to be used
		String[] hashAlg = new String[] {HashAlgorithm.SHA512};
		//compression algorithm being used
		String[] compressionAlg = new String[] {CompressionAlgorithm.ZIP};
		//cipher the user has chosen
		String[] cypherAlg = new String[] {cyphers[Integer.parseInt(args[0])]};

		long expiryDate = Integer.parseInt(args[5]);//0; //no expiry Date
		int keySize = 4096; //the key size in bytes
		
		//generate the keys
		keyStoreObject.generateKeyPair(keySize, args[1], keyAlgorithm, args[2], compressionAlg, hashAlg, cypherAlg, expiryDate);

		// Gabriel: Export the keys to disk.
		keyStoreObject.exportPublicKey(args[6] + args[4] + ".pub", args[1], Boolean.parseBoolean(args[3]));
		keyStoreObject.exportPrivateKey(args[7] + args[4] + ".priv", args[1], Boolean.parseBoolean(args[3]));
	} //END generateKeyPair
	
	/*
	Name:
		encrypt
	Input:
		args - These are the input arguments provided by the caller
			[0]: this is the name of the file to be encrypted
			[1]: this is the name of the key file to use
			Gabriel: NOT APPLICABLE:
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
		//KeyStore keyStoreObject = new KeyStore(args[1], args[2]);
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

		//pgpEntryPoint.encryptFile(args[0], keyStoreObject, args[3], args[4], Boolean.parseBoolean(args[5]), Boolean.parseBoolean(args[6]));
		// Gabriel: Added this try-catch in here to avoid looking for a NoPublicKeyFoundException in EncryptFilesFragment, to keep independence from the library.
		try
		{
			pgpEntryPoint.encryptFile(args[0], args[1], args[4], Boolean.parseBoolean(args[5]), Boolean.parseBoolean(args[6]));
		}
		catch ( NoPublicKeyFoundException e )
		{
			e.printStackTrace();
			return -3;
		}
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
		//KeyStore keyStoreObject = new KeyStore(args[1], args[2]);
		//check if input is of the correct length
		if (args.length != 4) {
			//return negative one for error
			return -1;
		} //END IF
		
		//decrypt the file
		//pgpEntryPoint.decryptFile(args[0], keyStoreObject, args[2], args[3]);
		pgpEntryPoint.decryptFile(args[0], args[1], args[2], args[3]);
		return 0; //return zero for success
	} //END decrypt

	// Signs a file and stores it in the file referred to by outputPath.
	// Args:
	//	inputPath - Input file path.
	//	privkeyPath - Path to the private key.
	//	privkeyPassword - Private key's password; leave blank ("") if there is none.
	//	outputPath - Path to store the signed file at.
	//	asciiArmor - Use Base64-encoded output instead of binary.
	// Throws an exception on failure.
	public static void sign(String inputPath, String privkeyPath, String privkeyPassword, String outputPath, boolean asciiArmor) throws Exception
	{
		pgpEntryPoint.signFile(inputPath, privkeyPath, privkeyPassword, outputPath, asciiArmor);
	}

	// Verifies a file's signature and extracts its contents to the file referred to by outputPath.
	// Args:
	//	inputPath - Input signed file path.
	//	pubkeyPath - Path to the sender's public key.
	//	outputPath - Path to store the verified file, with the signature and compression stripped.
	// Returns:
	//	0 if the message has a valid signature
	//	-1 if the message is corrupted or the signature is forged
	//	-2 if the signature does not match the provided public key
	//	-3 if there was no signature to begin with
	// Throws an exception on file system failure or other errors of similar nature.
	public static int verify(String inputPath, String pubkeyPath, String outputPath) throws Exception
	{
		SignatureCheckResult result = pgpEntryPoint.verifyAndExtract(inputPath, pubkeyPath, outputPath);
		if ( result == SignatureCheckResult.SignatureVerified )
			return 0;
		else if ( result == SignatureCheckResult.SignatureBroken )
			return -1;
		else if ( result == SignatureCheckResult.PublicKeyNotMatching )
			return -2;
		else	// result == SignatureCheckResult.NoSignatureFound
			return -3;
	}

	// Lists the user IDs of the keys in a directory.
	// So far, used by EncryptFilesFragment and DecryptVerifyFragment.
	// Args:
	//	keysDir - File representing the directory containing the keys to list names of.
	//	secretKeys - Set to false if reading public keys, and true if reading private keys.
	//	out_keyPaths - Output. Will contain the filenames of the keys being read. First element is set to "NONE", to account for prompting the user to select a key.
	//	out_keyNames - Output. Will contain the user IDs of the keys, in the same order as out_keyPaths. First element is set to "Select a Key", to prompt the user to select a key.
	// Returns true if the operation was successful, and false if not.
	//	Key filenames will be returned in out_keyPaths.
	//	Key user IDs will be returned in out_keyNames.
	public static boolean getKeyNamesInDir(File keysDir, boolean secretKeys, List<String> out_keyPaths, List<String> out_keyNames)
	{
		out_keyPaths.add("NONE");	// This is just to keep the two ArrayLists having equal elements, to make indexing items a bit easier.
		out_keyNames.add("Select a Key");

		if ( keysDir.list() == null )
		{
			return false;
		}
		String[] key_candidates = keysDir.list().clone();
		KeyStore tempStore = new KeyStore();
		KeyPairInformation current;
		for ( String curFile : key_candidates )
		{
			try
			{
				if ( secretKeys )
				{
					current = tempStore.importPrivateKey(keysDir.getAbsolutePath() + "/" + curFile)[0];
					out_keyPaths.add(curFile);
					out_keyNames.add(current.getUserID());
				}
				else
				{
					current = tempStore.importPublicKey(keysDir.getAbsolutePath() + "/" + curFile)[0];
					if ( !current.isRevoked() && !current.isExpired() )
					{
						out_keyPaths.add(curFile);
						out_keyNames.add(current.getUserID());
					}
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	// Gets information about a key file.
	// Args:
	//	filePath - Path to the key file.
	//	secretKey - Set to false if the key file is a public key, and true if it's a private key.
	// Returns a string array with the following:
	//	[0] - User ID
	//	[1] - Key fingerprint
	//	[2] - Key algorithm
	//	[3] - Key expiry date (blank = none)
	//	[4] - Has this key expired? Set to "true" if so.
	//	[5] - Has this key been revoked? Set to "true" if so.
	// Throws an exception on system failure.
	public static String[] getKeyInfo(String filePath, boolean secretKey) throws Exception
	{
		String[] output = new String[6];

		KeyStore tempStore = new KeyStore();
		KeyPairInformation keyInfo;
		if ( secretKey )
		{
			keyInfo = tempStore.importPrivateKey(filePath)[0];
		}
		else
		{
			keyInfo = tempStore.importPublicKey(filePath)[0];
		}

		output[0] = keyInfo.getUserID();
		output[1] = keyInfo.getFingerprint();
		output[2] = keyInfo.getAlgorithm();
		output[3] = keyInfo.getExpirationDate().toString();
		output[4] = "" + keyInfo.isExpired();
		output[5] = "" + keyInfo.isRevoked();

		return output;
	}
	
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
