For MIME types, see the following:
	https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types
To install gradle in a BASH/linux environment:
	sudo apt install gradle
	sudo add-apt-repository ppa:cwchien/gradle
	sudo apt-get update
	sudo apt upgrade gradle
To create a gradle folder:
	First:
		Java 1.8 or greater
		Gradle 2.3 or greater.
		A Google account with Google Drive enabled
		
		Go to: https://developers.google.com/drive/api/v3/quickstart/java
		Click enable the Drive API
		Download the credentials.json from the popup window
	Second: (from your desired working directory in BASH)
		gradle init --type basic
		mkdir -p src/main/java src/main/resources 
		Copy the credentials.json file you downloaded in the above step to src/main/resources/
	Third:
		Move the DriveQuickstart.java code into src/main/java
	Fourth:
		gradle run
			Go to the hyperlink that the terminal displays, click accept
Version this was made under:
	Gradle 6.2.1
	openjdk version "1.8.0_242"
	OpenJDK Runtime Environment (build 1.8.0_242-8u242-b08-0ubuntu3~16.04-b08)
	OpenJDK 64-Bit Server VM (build 25.242-b08, mixed mode)
	Ubuntu 16.04.6 LTS