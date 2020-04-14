# COMP3004B-Nameless-Project
Repository for our COMP3004B (Object-Oriented Software Engineering) project.

# Description
Our task in COMP3004B, taught by Olga Baysal at Carleton University, was to get into groups of four and create a mobile application. Our group chose to make a privacy- and security-oriented application that allows the user to generate and store passwords; encrypt/decrypt and sign/verify sensitive files; quickly generate a cryptocurrency wallet and check its balance; and access topics on how to keep themselves secure.

Our main project is under "hyggelig-merged".

The project has four groups of tools:
- Password Tools
  - Allows the user to generate a password with variable length and character space.
    - User can store this password in the device's password database.
  - The user can store their passwords in a database.
    - Can view only one password at a time by clicking an entry in the DB.
- Encryption Tools:
  - The user can encrypt and decrypt files using either symmetric or asymmetric cryptography.
    - Input can be a file, or you can directly take a picture or record a video in the application as input.
  - The user can sign and verify files.
  - PGP keys can be generated, imported, exported, and deleted.
  - The user can store sensitive files in an interally-stored folder, encrypted with a user-set password.
    - Files are encrypted with AES-256.
- Cryptocurrency Wallet:
  - The user can generate a Bitcoin address with a specific label, and can view the balance on this address.
    - NOTE: This requires a block.io API key; the one in use has been invalidated and removed from the code.
- Help:
  - The user can view brief topics about how certain actions and habits can increase or decrease their security and privacy.
    - For example, the user can learn why they should not reuse passwords.

# Warning
Now that the semester is over, the source code is free; you can study it, or fork it and use it under the conditions of GPLv3.
However, keep the following in mind:
- We may add or change some features to this project in the future.
  - However, don't count on this, since the class we had to make this for is over.
  - For all practical purposes, if you want to work on this, you're on your own.
- At this time at least, it is not recommended to actually use this for protecting sensitive data, especially if you have enemies.
  - Due to the three-month deadline, we had to forsake some security features.
  - We did not have enough time to audit the program.
  - Some of these are covered in the following points.
- Passwords in the password database are currently stored unencrypted.
- The Bitcoin wallet needs a block.io API key, which is currently stored in the source code itself.
  - API keys were pushed to the repo, but have been invalidated and are now useless.
- Signing/verifying and asymmetric encryption/decryption use Didisoft's OpenPGP library for Java.
  - This is a proprietary, closed-source library. Distribution is apparently permitted.
  - The library has not been publicly vetted for vulnerabilities, backdoors, or anti-features.
- Since this application is written in Java, secrets are not scrubbed from memory when no longer in use.
  - Risk unknown due to Android's security measures.
- We were planning to add in the ability to back up the password database, private folder contents, etc. to Google Drive.
  - However, we did not have enough time to implement that.
  - Some example code Conner found is present under googleDriveBackend/.

# Metadata
Team name: Hyggelig Security
Group members:
- Gabriel Valachi (Chainmanner)
  - Implemented the Encryption Tools.
- Kevin Sullival (KRsully)
  - Implemented the Bitcoin Wallet and the Help topics.
- Conner Stewart (Stewarttt)
  - Implemented cryptography functions, and wrappers for some libraries.
- Kavan Salehi (KavanS)
  - Implemented the Password Tools.

Project: hyggelig secure

License: GNU General Public License version 3.0

# Other
See /dev_logs/ for our weekly dev logs.
