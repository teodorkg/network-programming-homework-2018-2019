# Chat Application

## What is it
Serverless multicast chat app using udp to transfer messages and files

## How to use
Each client compiles all java files and runs MulticastChatClient. Enters command. On new line enters the text to be sent or the full path of the file to be sent. Other users receive the file (hopefully the whole file) saved as .../src/{socketOut port number}/file. Type "exit" to end the program.

### commands (case insensitive)
 - sendText
 - sendImage
 - sendVideo
 - sendFile
 - exit

## Points
 - If the file saved doubles the name of an existing file. The incomming file (name.ext) is renamed (name(n).ext, where n is the first natural number, such that name(n).ext was not existing in the folder untill that moment).