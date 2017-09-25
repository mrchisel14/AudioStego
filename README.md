# AudioStego (A Java Command Line Steganography Application)
This was created as a fun example to show how messages could be hidden in audio files.

## To Compile
```
sudo apt-get install ant
ant compile
ant jar
```

## To Run

After you compile run the following:
```
java -jar dist/AudioStego.jar
```

## Help

To get help information run without any arguments. You should see the following.

```
Usage: AudioStego <-d|-e|-g> <option args>
-d:	Decode <encodedFile> <privKeyFile>
-e:	Encode <sourceAudioFile> <outputFile> <pubKeyFile> <"Message">
-g:	Generate Public\Private key pair. 
```
