# enctool
Command line tool to detect and convert files encoding.

## HOW TO
Display embedded manual
java -jar enctool.jar

## How to detect encoding of one file
java -jar enctool.jar myFile.txt
OR
java -jar enctool.jar -e myFile.txt

This command will display encoding with the highest level of confidence.

## How to detect encoding of multiple files
java -jar enctool.jar -r myDirectory

This command work recursively in the directory.

## How to detect encoding of multiple files with a file name filter
java -jar enctool.jar -r -p .*\.java myDirectory

In this example, only *.java files are analyzed.
The pattern filter must be a regular Expression.

## How to display files that have a different encoding from one selected
java -jar enctool.jar -r -d ISO-8859-1 myDirectory

## How to convert encoding of one file
java -jar enctool.jar -c UTF-8 myFile.txt

Encoding of "myFile.txt" is detected and converted to UTF-8.

To force input encoding, use "-f" option :
java -jar enctool.jar -c UTF-8 -f ISO-8859-1 myFile.txt

## How to convert encoding of multiple files with a file name filter
java -jar enctool.jar -cr UTF-8 -p .*\.java myDirectory
