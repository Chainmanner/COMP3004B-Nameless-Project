//Author: Connor Raymond Stewart
//this was made using information from: https://developers.google.com/drive/api/v3/quickstart/java, https://developers.google.com/drive/api/v3/manage-uploads, https://developers.google.com/drive/api/v3/manage-downloads

//included packages within the google drive API interface
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.client.http.FileContent;

//included packages within the java interface
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

/*
Name:
	DriveQuickstart
Input:
	We need a credentials.json file from the google drive
		This file must be downloaded externally and placed within the /src/main/resources folder directly by the system or the user
Output:
	Various requests which modify a google drive account
	Various requests to download and upload to google drive accounts
Description:
	This application is used to connect to the google drive
*/
public class DriveQuickstart {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
	
	private static Drive service; //the current service for the google drive
	private static List<File> googleDriveMetadata; //the current metadata for the google drive
	
	public static String fileToFind; //the current file we are looking for within the google drive
	public static String outputFileName = "default"; //the name of any output files we download in the future
	public static String inputFileName; //this is the name of the file we are looking to download from the google drive
	public static String fileToDelete; //this is the name of the file we're looking to delete
	public static String inputFileMIMEType; //this is the meme type of the input file

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    } //END getCredentials

    public static void main(String... args) throws IOException, GeneralSecurityException {
        //Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

		//Example code
		//To get google drive metadata, use the following command
		//getFiles();
		
		//to download a file from the google drive, use the following command
		//fileToFind = "Getting started";
		//downloadFilesByName();
		//Alternatively, if the file id is already known, you can use the following
		//downloadFiles(ID_VARIABLE);
		//all downloaded files appear in the Download folder in the working directory
		
		//to upload a file to the google drive, use the following command
		//for meme types, some common ones include:
		//	image/png, image/jpeg, audio/ogg, video/mp4, text/plain
		//inputFileName = "im.png";
		//inputFileMIMEType = "image/png";
		//uploadFiles();
		//the upload files should be within the Uploads folder of the current directory
		/*
		inputFileName = "im.png";
		inputFileMIMEType = "image/png";
        uploadFiles();
		*/
		//tests the deletion method, deletes all files with the given name
		/*
		fileToDelete = "photo.jpg";
		deleteByName();
		*/
    } //END main

	/*
	Name:
		deleteByID
	Input:
		fileID - this is the file ID of the google drive file were trying to delete
	Output:
		return - integer value 1 represents success
		Google drive has the given file deleted
	Description:
		Deletes a file in the google drive with the ID matching the input string
	*/
	public static int deleteByID(String fileID) throws IOException, GeneralSecurityException {
		//request to delete the file with the given ID, and execute the request
		service.files().delete(fileID).execute();
		return 1; //one for success
	} //END deleteByID

	/*
	Name:
		deleteByName
	Input:
		fileToDelete - this is the name of the file(s) we are trying to delete
	Output:
		return - integer value 1 represents success
		Google drive has all files with the given name deleted
	Description:
		Deletes all files in the google drive with the name matching the input string
	*/
	public static int deleteByName() throws IOException, GeneralSecurityException {
		//get the file metadata
		getFiles(); //update our local copy of the google drives metadata
		for (File driveFile : googleDriveMetadata) { //look at the google drives metadata
			if ((driveFile.getName()).equals(fileToDelete)) { //look for a filename that matches
				deleteByID(driveFile.getId()); //get the files ID string and delete the file with that ID
			} //END IF
		} //END FOR
		return 1; //one for success
	} //END deleteByName

	/*
	Name:
		getFiles
	Input:
		None
	Output:
		return - integer value 1 represents success
		all metadata from google drive printed to the console
		all metadata from google drive stored within the googleDriveMetadata global variable
	Description:
		gets the metadata from the currently active google drive
		metadata includes file names and ids
	*/
	public static int getFiles() throws IOException, GeneralSecurityException {
		//get the first 1000 file names in the google drive
        FileList fileNamesInDrive = service.files().list().setPageSize(1000).setFields("nextPageToken, files(id, name)").execute();
        googleDriveMetadata = fileNamesInDrive.getFiles(); //save metadata to global variable
        if (googleDriveMetadata == null || googleDriveMetadata.isEmpty()) {
            System.out.println("No files found in Google Drive\n");
			googleDriveMetadata = null; //set to null, there is nothing in the drive
			return -1; //no files found error
        }
		else { //Print and Store the names of the files in the google drive
            System.out.println("Google Drive Files:");
            for (File driveFile : googleDriveMetadata) {
                System.out.printf("%s (%s)\n", driveFile.getName(), driveFile.getId());
            } //END FOR
			
        } //END IF
		return 1; //files found exit
	} //END getFiles

	/*
	Name:
		downloadFilesByName
	Input:
		fileToFind file name
		googleDriveMetadata from the getFiles function
	Output:
		return - integer value 1 represents success
		Downloaded file from the google drive with the matching name
	Description:
		Downloads files from the google drive which have the matching name as the one specified in fileToFind
		The file is then saved within the Download folder of the working directory
	*/
	public static int downloadFilesByName() throws IOException, GeneralSecurityException {
		//get the file metadata
		getFiles(); //update our local copy of the google drives metadata
		String id = null; //the id of the file we are looking for
		for (File driveFile : googleDriveMetadata) { //look at the google drives metadata
			if ((driveFile.getName()).equals(fileToFind)) { //look for a filename that matches
				id = driveFile.getId(); //get the files ID string
			} //END IF
		} //END FOR 
		downloadFiles(id); //get the file
		return 1; //success code
	} //END downloadFilesByName

	/*
	Input:
		uploadFiles
		This uploads a file with the name specified under inputFileName
		The MIME type for the upload request is specified under inputFileMIMEType
	Output:
		return - integer value 1 represents success
	Description:
		Uploads the specified file to the google drive
		Make sure the MIME type matches the file type
	*/
	public static int uploadFiles() throws IOException, GeneralSecurityException {
		File fileMetadata = new File(); //create a new file data structure
		fileMetadata.setName(inputFileName); //the name of the file we are to upload
		java.io.File filePath = new java.io.File("Uploads/" + inputFileName); //the path to the file we wish to upload
		FileContent mediaContent = new FileContent(inputFileMIMEType, filePath); //this is the MIME type of the file we are uploading
		File file = service.files().create(fileMetadata, mediaContent).setFields("id").execute(); //create file upload request
		System.out.println("File ID: " + file.getId()); //print the resulting ID of the of the upload request to the console
		return 1; //success code
	} //END uploadFiles

	/*
	Name:
		downloadFilesByName
	Input:
		fileId - this is the file ID of the google drive file were trying to download
	Output:
		return - integer value 1 represents success
		Google drive file is downloaded then saved to the working directories Download folder
	Description:
		If the fileID of the file we are looking for in the google drive is known, we use this method to download that file
	*/
	public static int downloadFiles(String fileID) throws IOException, GeneralSecurityException {
		String fileId = fileID; //this is the file ID of the file we are to download
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); //this is the bytesteam which will contain our downloaded file
		service.files().get(fileId).executeMediaAndDownloadTo(outputStream); //send a request to download the specified file
		try(OutputStream outputFile = new FileOutputStream("Downloads/"+outputFileName)) { //try and write the bytestream to a file
			outputStream.writeTo(outputFile); //write the downloaded bytestream to a file with the specified name
		} //END TRY
		return 1; //success code
	} //END downloadFiles
} //END DriveQuickstart
