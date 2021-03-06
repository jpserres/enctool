/*
 * Copyright 2012-2013 Jean-Philippe Serres
 * 
 *   This file is part of EncTool.
 *
 *   EncTool is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   EncTool is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with EncTool.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package fr.serres.enctool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import com.ibm.icu.text.CharsetDetector;

import fr.serres.enctool.enums.CLOptions;

/**
 * Main class of Enctool project. This class offer an unified command line
 * interface for this encoding tool.
 * 
 * @author Jean-Philippe Serres
 * 
 */
public class Enctool {

	public static final boolean DEBUG = false;

	public static void main(String[] args) {
		boolean syntaxError = false;
		boolean help = false;

		// options
		CLOptions primaryOption = null;
		String differentFrom = null;
		String ouptputLocation = null;
		String patternFilename = null;
		String targetEncoding = null;
		String inputEncoding = null;
		String path = null;
		Boolean bom = null;
		boolean allConfidences = false;

		if (args != null && args.length > 0) {
			boolean commandComplete = false;
			boolean currentOptionIsDisplayOnlyFilesWithDifferentEncoding = false;
			boolean currentOptionIsChangeOutputLocation = false;
			boolean currentOptionIsPatternFilename = false;
			boolean currentOptionIsConvert = false;
			boolean currentOptionIsForceInputEncoding = false;
			boolean currentOptionIsWithBOM = false;
			boolean currentOptionIsWithoutBOM = false;

			for (int i = 0; i < args.length; i++) {

				if (args[i].startsWith("-")) {
					switch (CLOptions.getCLOptionFromSyntax(args[i])) {
					case DETECT_ENCODING_FILE:
						if (primaryOption != null) {
							syntaxError = true;
						} else {
							primaryOption = CLOptions.DETECT_ENCODING_FILE;
						}
						break;
					case DETECT_ENCODING_FILE_ADVANCED:
						if (primaryOption != null) {
							syntaxError = true;
						} else {
							primaryOption = CLOptions.DETECT_ENCODING_FILE_ADVANCED;
						}
						break;
					case DETECT_ENCODING_DIR:
						if (primaryOption != null) {
							syntaxError = true;
						} else {
							primaryOption = CLOptions.DETECT_ENCODING_DIR;
						}
						break;
					case DETECT_ENCODING_DIR_ADVANCED:
						if (primaryOption != null) {
							syntaxError = true;
						} else {
							primaryOption = CLOptions.DETECT_ENCODING_DIR_ADVANCED;
						}
						break;
					case CONVERT_FILE:
						if (primaryOption != null) {
							syntaxError = true;
						} else {
							currentOptionIsConvert = true;
							primaryOption = CLOptions.CONVERT_FILE;
						}
						break;
					case CONVERT_FILES_DIR:
						if (primaryOption != null) {
							syntaxError = true;
						} else {
							currentOptionIsConvert = true;
							primaryOption = CLOptions.CONVERT_FILES_DIR;
						}
						break;
					case FORCE_INPUT_ENCODING:
						if (primaryOption == null
								|| (primaryOption != CLOptions.CONVERT_FILE && primaryOption != CLOptions.CONVERT_FILES_DIR)) {
							syntaxError = true;
						} else {
							currentOptionIsForceInputEncoding = true;
						}
						break;
					case DISPLAY_ONLY_FILES_WITH_DIFFERENT_ENCODING:
						if (primaryOption == null
								|| (primaryOption != CLOptions.DETECT_ENCODING_DIR && primaryOption != CLOptions.DETECT_ENCODING_DIR_ADVANCED)) {
							syntaxError = true;
						} else {
							currentOptionIsDisplayOnlyFilesWithDifferentEncoding = true;
						}
						break;
					case CHANGE_OUTPUT_LOCATION:
						if (primaryOption == null
								|| (primaryOption != CLOptions.CONVERT_FILE && primaryOption != CLOptions.CONVERT_FILES_DIR)) {
							syntaxError = true;
						} else {
							currentOptionIsChangeOutputLocation = true;
						}
						break;
					case PATTERN_FILENAME:
						if (primaryOption == null
								|| (primaryOption != CLOptions.DETECT_ENCODING_DIR
										&& primaryOption != CLOptions.DETECT_ENCODING_DIR_ADVANCED && primaryOption != CLOptions.CONVERT_FILES_DIR)) {
							syntaxError = true;
						} else {
							currentOptionIsPatternFilename = true;
						}
						break;
					case ADD_BOM:
						if (primaryOption == null
								|| (primaryOption != CLOptions.CONVERT_FILES_DIR && primaryOption != CLOptions.CONVERT_FILE)
								|| bom != null) {
							syntaxError = true;
						} else {
							bom = true;
						}
						break;
					case NO_BOM:
						if (primaryOption == null
								|| (primaryOption != CLOptions.CONVERT_FILES_DIR && primaryOption != CLOptions.CONVERT_FILE)
								|| bom != null) {
							syntaxError = true;
						} else {
							bom = false;
						}
						break;
					case ALL_CONFIDENCES:
						if (primaryOption == null) {
							syntaxError = true;
						} else {
							allConfidences = true;
						}
						break;
					case SUPPORTED_ENCODINGS:
						if (primaryOption != null) {
							syntaxError = true;
						} else {
							primaryOption = CLOptions.SUPPORTED_ENCODINGS;
						}
						break;
					case VERSION:
						if (primaryOption != null) {
							syntaxError = true;
						} else {
							primaryOption = CLOptions.VERSION;
						}
						break;
					default:
						// undefined
						syntaxError = true;
						break;
					}
				} else {
					if (primaryOption == null) {
						// default primary option :
						primaryOption = CLOptions.DETECT_ENCODING_FILE;
					}
					// no option syntax parameter
					if (currentOptionIsDisplayOnlyFilesWithDifferentEncoding) {
						differentFrom = args[i];
						currentOptionIsDisplayOnlyFilesWithDifferentEncoding = false;
					} else if (currentOptionIsChangeOutputLocation) {
						ouptputLocation = args[i];
						currentOptionIsChangeOutputLocation = false;
					} else if (currentOptionIsForceInputEncoding) {
						inputEncoding = args[i];
						currentOptionIsForceInputEncoding = false;
					} else if (currentOptionIsPatternFilename) {
						patternFilename = args[i];
						currentOptionIsPatternFilename = false;
					} else if (currentOptionIsConvert) {
						targetEncoding = args[i];
						currentOptionIsConvert = false;
					} else {

						// <file> or <path>
						if (commandComplete) {
							syntaxError = true;
						} else {
							path = args[i];
							commandComplete = true;
						}
					}
				}

			}

			if (!commandComplete && primaryOption != CLOptions.VERSION
					&& primaryOption != CLOptions.SUPPORTED_ENCODINGS) {
				syntaxError = true;
			}

		} else {
			// no args => help
			help = true;
		}

		if (syntaxError) {
			showErrorSyntax();
			showUsage();
		} else if (help) {
			showUsage();
		} else {
			// execute
			execute(primaryOption, path, differentFrom, ouptputLocation,
					patternFilename, allConfidences, targetEncoding,
					inputEncoding, bom);
		}
	}

	/**
	 * Print usage manual.
	 */
	private static void showUsage() {
		StringBuilder man = new StringBuilder("Usage :");
		man.append('\n');
		man.append("enctool [-e|-E|-c] [options] <file>");
		man.append('\n');
		man.append("enctool [-r|-R|-cr] [options] <path>");
		man.append('\n');
		man.append('\n');
		man.append("This tool allows to detect and convert text files encoding.");
		man.append('\n');
		man.append('\n');
		man.append("Parameters :");
		man.append('\n');
		man.append('\n');
		man.append("DETECT MODS :");
		man.append('\n');
		man.append(
				"-e :             DEFAULT option. Detects encoding of a text file.")
				.append('\n');
		man.append("                 Display only encoding with the highest confidence.");
		man.append('\n');
		man.append('\n');
		man.append("-E :             Detects encoding of a text file. ")
				.append('\n');
		man.append(
				"                 Display the 3 firsts encodings that could match with a non-zero confidence ")
				.append('\n');
		man.append(
				"                 and display the language (3 letters ISO code) when determinated. ")
				.append('\n');
		man.append("                 Use -a to display all.").append('\n');
		man.append("                 Format : encoding,confidence[0-100],language");
		man.append('\n');
		man.append('\n');
		man.append(
				"-r :             Detects encoding of text files recursively in a directory. ")
				.append('\n');
		man.append("                 Display only encoding with the highest confidence.");
		man.append('\n');
		man.append('\n');
		man.append(
				"-R :             Detects encoding of text files recursively in a directory. ")
				.append('\n');
		man.append(
				"                 Display the 3 firsts encodings that could match with a non-zero confidence ")
				.append('\n');
		man.append(
				"                 and display the language (3 letters ISO code) when determinated. ")
				.append('\n');
		man.append("                 Use -a to display all.");
		man.append('\n');
		man.append("                 Format : encoding,confidence[0-100],language");
		man.append('\n');
		man.append('\n');
		man.append("DETECT OPTIONS :");
		man.append('\n');
		man.append(
				"-d <encoding> :  Display only files with different encoding that <encoding>. ")
				.append('\n');
		man.append("                 Work only with -r option.");
		man.append('\n');
		man.append('\n');
		man.append("-a :             Display all confidences. Work only with -E or -R options.");
		man.append('\n');
		man.append('\n');
		man.append("CONVERT MODS :");
		man.append('\n');
		man.append(
				"-c <encoding> :  Detects encoding of a text file and convert it to <encoding>.")
				.append('\n');
		man.append("                 Use -f option to bypass encoding detection.");
		man.append("                 WARNING : File is overwritten. Use -o to change output location.");
		man.append('\n');
		man.append('\n');
		man.append(
				"-cr <encoding> : Detects encoding of text files recursively in a directory ")
				.append('\n');
		man.append("                 and convert them to <encoding>. ").append(
				'\n');
		man.append("                 WARNING : Files are overwritten. ")
				.append('\n');
		man.append("                 Use -o to change output location.");
		man.append('\n');
		man.append('\n');
		man.append("CONVERT OPTIONS :");
		man.append('\n');
		man.append("-o <path> :      Change output location.");
		man.append('\n');
		man.append('\n');
		man.append("-f <encoding> :  Force input encoding.").append('\n');
		man.append("                 Work only with -c and -cr options.");
		man.append('\n');
		man.append('\n');
		man.append("-bom :           Force adding BOM to output UTF-8 file.").append(
				'\n');
		man.append("                 Work only with -c and -cr options.");

		man.append('\n');
		man.append('\n');
		man.append("-nobom :         No BOM to output UTF-8 file.")
				.append('\n');
		man.append("                 Work only with -c and -cr options.");

		man.append('\n');
		man.append('\n');
		man.append("COMMONS OPTIONS :");
		man.append('\n');
		man.append("-p <regexp> :    Pattern to filter files names. Must be a regexp.");
		man.append('\n');
		man.append('\n');
		man.append("OTHERS :");
		man.append('\n');
		man.append("-se :            Display names of all supported encodings.");
		man.append('\n');
		man.append("-v :             Display the version of this EncTool.");
		man.append('\n');
		man.append('\n');
		// man.append("Examples :");

		System.out.println(man.toString());
	}

	/**
	 * Print version.
	 */
	private static void showVersion() {
		StringBuilder version = new StringBuilder(">> Enctool <<");
		version.append('\n');
		version.append("Version : 0.3");
		version.append('\n');
		version.append("Year : 2013");
		version.append('\n');
		version.append("Author : Jean-Philippe Serres (enctool@serres.fr)");
		version.append('\n');
		version.append("--");
		version.append('\n');
		version.append("ICU version : 49.1");
		System.out.println(version.toString());
	}

	/**
	 * Print message command line syntax error.
	 */
	private static void showErrorSyntax() {
		StringBuilder error = new StringBuilder("Command line syntax error.");
		error.append('\n');
		System.out.println(error.toString());
	}

	/**
	 * Display input and output supported encoding.
	 */
	private static void showSupportedEncodings() {
		// ICU encodings
		String[] icuEncodings = CharsetDetector.getAllDetectableCharsets();

		// JVM encoding
		Map<String, Charset> JVMEncodings = Charset.availableCharsets();

		StringBuilder inputEncodings = new StringBuilder(
				"Encodings that can be recognized : ");

		boolean first = true;
		for (String tmp : icuEncodings) {
			if (!first) {
				inputEncodings.append(", ");
			} else {
				first = false;
			}
			inputEncodings.append(tmp);
		}
		inputEncodings.append('\n');

		StringBuilder outputEncodings = new StringBuilder(
				"Output encodings supported : ");
		first = true;
		for (String tmp : JVMEncodings.keySet()) {
			if (!first) {
				outputEncodings.append(", ");
			} else {
				first = false;
			}
			outputEncodings.append(tmp);
		}
		outputEncodings.append('\n');

		System.out.println(inputEncodings.toString());
		System.out.println(outputEncodings.toString());
	}

	/**
	 * Execute command line.
	 * 
	 * @param mod
	 *            Execution mod.
	 * @param path
	 *            Path ro file or dir.
	 * @param differentFrom
	 *            Display only files with encoding different of this.
	 * @param ouptputLocation
	 *            Base path of destination converted files.
	 * @param patternFilename
	 *            Pattern (regexp) to filter filename.
	 * @param allConfidences
	 *            Display all confidences.
	 * @param targetEncoding
	 *            Target encoding (convert function).
	 * @param inputEncoding
	 *            Forced input encoding.
	 * @param bom
	 *            Add BOM to UTF-8 file output.
	 */
	private static void execute(CLOptions mod, String path,
			String differentFrom, String ouptputLocation,
			String patternFilename, boolean allConfidences,
			String targetEncoding, String inputEncoding, Boolean bom) {

		if (mod != null) {
			Engine engine = new Engine();
			switch (mod) {
			case DETECT_ENCODING_FILE:
				try {
					System.out.println(engine.detectEncoding(path, false,
							allConfidences, differentFrom));
				} catch (FileNotFoundException e) {
					System.out.println("ERROR => File not found : " + path);
					if (DEBUG) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					System.out.println("ERROR => Error reading file : " + path);
					if (DEBUG) {
						e.printStackTrace();
					}
				}
				break;
			case DETECT_ENCODING_FILE_ADVANCED:
				try {
					System.out.println(engine.detectEncoding(path, true,
							allConfidences, null));
				} catch (FileNotFoundException e) {
					System.out.println("ERROR => File not found : " + path);
					if (DEBUG) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					System.out.println("ERROR => Error reading file : " + path);
					if (DEBUG) {
						e.printStackTrace();
					}
				}
				break;
			case DETECT_ENCODING_DIR:
				try {
					System.out.println(engine.detectEncodingRecursive(path,
							false, allConfidences, patternFilename,
							differentFrom));
				} catch (FileNotFoundException e) {
					System.out
							.println("ERROR => Directory not found : " + path);
					if (DEBUG) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					System.out.println("ERROR => Error reading directory : "
							+ path);
					if (DEBUG) {
						e.printStackTrace();
					}
				}
				break;

			case DETECT_ENCODING_DIR_ADVANCED:
				try {
					System.out.println(engine.detectEncodingRecursive(path,
							true, allConfidences, patternFilename,
							differentFrom));
				} catch (FileNotFoundException e) {
					System.out
							.println("ERROR => Directory not found : " + path);
					if (DEBUG) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					System.out.println("ERROR => Error reading directory : "
							+ path);
					if (DEBUG) {
						e.printStackTrace();
					}
				}
				break;
			case CONVERT_FILE:
				try {
					System.out.println(engine
							.convertEncoding(path, targetEncoding,
									ouptputLocation, inputEncoding, bom));
				} catch (FileNotFoundException e) {
					System.out.println("ERROR => File not found : " + path);
					if (DEBUG) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					System.out.println("ERROR => Error reading directory : "
							+ path);
					if (DEBUG) {
						e.printStackTrace();
					}
				}
				break;

			case CONVERT_FILES_DIR:
				try {
					System.out.println(engine.convertEncodingRecursive(path,
							null, patternFilename, targetEncoding,
							ouptputLocation, inputEncoding, bom));
				} catch (FileNotFoundException e) {
					System.out
							.println("ERROR => Directory not found : " + path);
					if (DEBUG) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					System.out.println("ERROR => Error reading file : " + path);
					if (DEBUG) {
						e.printStackTrace();
					}
				}
				break;

			case SUPPORTED_ENCODINGS:
				showSupportedEncodings();
				break;

			case VERSION:
				showVersion();
				break;

			default:
				break;
			}

		}
	}

}
