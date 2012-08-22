/*
 * Copyright 2012 Jean-Philippe Serres
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

import java.awt.event.FocusAdapter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * Implementation of all enctool functions.
 * 
 * @author Jean-Philippe Serres
 * 
 */
public class Engine {

	private static final int MAX_CONFIDENCES = 3;

	/**
	 * Detect a file encoding.
	 * 
	 * @param file
	 *            File.
	 * @param advanced
	 *            Display more informations (3 firsts confidences).
	 * @param allConfidences
	 *            Display all confidences.
	 * @param differentFrom
	 *            Display only file with different encoding that differentFrom.
	 * @return Report.
	 * @throws IOException
	 *             If reading file error.
	 */
	public String detectEncoding(String file, boolean advanced,
			boolean allConfidences, String differentFrom) throws IOException {
		StringBuilder result = new StringBuilder();
		if (file != null) {
			// read test
			File testFile = new File(file);
			if (testFile != null && testFile.isFile() && testFile.canRead()) {

				// read file
				BufferedInputStream streamData = this.inputStreamFromPath(file);

				// analyse
				CharsetDetector detector;
				detector = new CharsetDetector();
				detector.setText(streamData);

				if (!advanced) {
					// character set matches input data with the highest
					// confidence
					try {
						CharsetMatch match = detector.detect();
						result.append(this.generateReport(match, advanced,
								differentFrom));
					} catch (ArrayIndexOutOfBoundsException e) {
						result.append("ERROR => Binary file ? ");
						if (Enctool.DEBUG) {
							e.printStackTrace();
						}
					} finally {
						// close
						if (streamData != null) {
							streamData.close();
						}
					}
				} else {
					// all of the character sets that could match your input
					// data
					// with a non-zero confidence
					try {
						CharsetMatch[] matches = detector.detectAll();
						int confidencesDisplayed = matches.length;
						if (!allConfidences) {
							if (MAX_CONFIDENCES < confidencesDisplayed) {
								confidencesDisplayed = MAX_CONFIDENCES;
							}
						}

						for (int i = 0; i < confidencesDisplayed; i++) {
							result.append(this.generateReport(matches[i],
									advanced, null));
							if (i + 1 < confidencesDisplayed) {
								result.append(" / ");
							}
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						result.append("ERROR => Binary file ? ");
						if (Enctool.DEBUG) {
							e.printStackTrace();
						}
					} finally {
						// close
						if (streamData != null) {
							streamData.close();
						}
					}
				}
				// close
				if (streamData != null) {
					streamData.close();
				}
			} else {
				// error reading
				if (!testFile.isFile()) {
					result.append("ERROR => It's not a file. ");
				} else if (!testFile.canRead()) {
					result.append("ERROR => Can not read file. No permission for current user ?");
				}
			}
		}
		return result.toString();
	}

	/**
	 * Detect files encoding recursively in a directory.
	 * 
	 * @param dir
	 *            Directory.
	 * @param advanced
	 *            Display more informations (3 firsts confidences).
	 * @param allConfidences
	 *            Display all confidences.
	 * @param pattern
	 *            Filename pattern.
	 * @param differentFrom
	 *            Display only file with different encoding that differentFrom.
	 * @return Report.
	 * @throws IOException
	 *             If reading file error.
	 */
	public String detectEncodingRecursive(String dir, boolean advanced,
			boolean allConfidences, String pattern, String differentFrom)
			throws IOException {
		StringBuilder result = new StringBuilder();
		if (dir != null) {
			// init pattern
			Pattern p = null;
			if (pattern != null) {
				p = Pattern.compile(pattern);
			}

			File file = new File(dir);
			if (file != null && file.isDirectory()) {
				File[] currentFiles = file.listFiles();
				for (File fileTmp : currentFiles) {
					if (fileTmp.isFile()) {
						boolean fileTmpMatch = true;
						if (p != null) {
							// filename match pattern ?
							Matcher matcher = p.matcher(fileTmp.getName());
							if (!matcher.matches()) {
								fileTmpMatch = false;
							}
						}

						if (fileTmpMatch) {
							String reportTmp = this.detectEncoding(
									fileTmp.getAbsolutePath(), advanced,
									allConfidences, differentFrom);
							if (!"".equals(reportTmp)) {
								// if different from encoding parameter
								result.append(fileTmp.getAbsolutePath());
								result.append(" : ");
								result.append(reportTmp);
								result.append('\n');
							}
						}
					} else {
						// directory
						result.append(this.detectEncodingRecursive(
								fileTmp.getAbsolutePath(), advanced,
								allConfidences, pattern, differentFrom));
					}
				}
			} else {
				result.append(dir + " : ERROR => It's not a directory.");
				result.append('\n');
			}
		}
		return result.toString();
	}

	/**
	 * Convert a text file to other encoding.
	 * 
	 * @param file
	 *            File to convert.
	 * @param toEncoding
	 *            Target encoding.
	 * @param ouptputLocation
	 *            Output location (optionnal).
	 * @param inputEncoding
	 *            Forced input encoding (optional).
	 * @return Report.
	 * @throws IOException
	 *             In case of IO exception.
	 */
	public String convertEncoding(String file, String toEncoding,
			String ouptputLocation, String inputEncoding) throws IOException {
		StringBuilder result = new StringBuilder();
		File inputFile = null;
		if (file != null) {

			// NEW TARGET ENCODING SUPPORTED ?
			if (Charset.isSupported(toEncoding)) {

				// INPUT ENCODING SUPPORTED ?
				if (inputEncoding == null || Charset.isSupported(inputEncoding)) {

					inputFile = new File(file);
					// READ / WRITE TESTS :
					boolean canReadWriteFile = false;
					// boolean canReadWriteParentDir = false;
					boolean canReadWriteParentDir = true;
					boolean canReadWriteOutputLocationDir = false;
					if (ouptputLocation == null) {
						// no necessary because output is same directory :
						canReadWriteOutputLocationDir = true;
						if (inputFile != null && inputFile.isFile()
								&& inputFile.canRead() && inputFile.canWrite()) {
							canReadWriteFile = true;
							// File parent = inputFile.getParentFile();
							// if (parent == null) {
							// parent = new File(File.separator);
							// }
							// if (parent != null && parent.isDirectory()
							// && parent.canRead() && parent.canWrite()) {
							// canReadWriteParentDir = true;
							// }
						}
					} else {
						// no necessary because output is different :
						canReadWriteParentDir = true;
						// test :
						if (inputFile != null && inputFile.isFile()
								&& inputFile.canRead()) {
							canReadWriteFile = true;
							File output = new File(ouptputLocation);
							if (output != null && output.isDirectory()
									&& output.canRead() && output.canWrite()) {
								canReadWriteOutputLocationDir = true;
							}
						}
					}

					// CONVERT :

					// init
					BufferedInputStream inputStreamData = null;
					Writer out = null;
					Reader in = null;
					File outFile = null;
					try {

						if (canReadWriteFile && canReadWriteParentDir
								&& canReadWriteOutputLocationDir) {

							String encoding = inputEncoding;
							if (encoding == null) {
								// detect encoding
								inputStreamData = this
										.inputStreamFromPath(file);
								encoding = this
										.simpleDetectEncoding(inputStreamData);
								if (inputStreamData != null) {
									// close
									inputStreamData.close();
								}
							}

							// open a new stream on the source file to prevent
							// use
							// of mark method by ICU
							inputStreamData = this.inputStreamFromPath(file);

							// reader
							in = new InputStreamReader(inputStreamData,
									encoding);

							if (encoding != null) {
								if (ouptputLocation == null) {
									// new output file (tmp file)
									outFile = new File(file + ".enctool");
									// writer
									out = new OutputStreamWriter(
											new FileOutputStream(outFile),
											toEncoding);
								} else {
									// new output file (tmp file)
									outFile = new File(ouptputLocation
											+ File.separator
											+ inputFile.getName());

									// writer
									out = new OutputStreamWriter(
											new FileOutputStream(outFile),
											toEncoding);
								}

								// writting
								int c;
								while ((c = in.read()) != -1) {
									out.write(c);
								}
								in.close();
								out.close();

								if (ouptputLocation == null) {
									// delete source file
									if (inputFile.delete()) {
										// rename new file
										if (!outFile.renameTo(new File(file))) {
											result.append("ERROR : target file can not be renamed.");
										} else {
											result.append("Successful encoded from "
													+ encoding
													+ " to "
													+ toEncoding);
										}
									} else {
										result.append("ERROR : source file can not be deleted.");
									}
								} else {
									result.append("Successful encoded from "
											+ encoding + " to " + toEncoding);
								}

							} else {
								result.append("ERROR : this file can not be converted (encoding can not be determined).");
							}
						} else {
							if (!canReadWriteFile) {
								result.append("ERROR : this file can not be read and/or write.");
							} else if (!canReadWriteParentDir) {
								result.append("ERROR : parent directory can not be read and/or write.");
							} else if (!canReadWriteOutputLocationDir) {
								result.append("ERROR : output directory can not be read and/or write.");
							}
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						result.append("ERROR => this file can not be converted (binary file ?). ");
						if (Enctool.DEBUG) {
							e.printStackTrace();
						}
					} finally {
						// close
						if (inputStreamData != null) {
							inputStreamData.close();
						}
						if (in != null) {
							in.close();
						}
						if (out != null) {
							out.close();
						}
					}

				} else {
					result.append("ERROR : input encoding \"" + inputEncoding
							+ "\" is not supported. ");
				}
			} else {
				result.append("ERROR : output encoding \"" + toEncoding
						+ "\" is not supported. ");
			}
		}
		return result.toString();
	}

	/**
	 * Convert text files recursively to other encoding.
	 * 
	 * @param dir
	 *            Base directory for files search.
	 * @param subDir
	 *            Name of current sub directory. Used by recursively method
	 *            call.
	 * @param pattern
	 *            Filename pattern.
	 * @param toEncoding
	 *            Target encoding.
	 * @param ouptputLocation
	 *            Output location (optionnal).
	 * @param inputEncoding
	 *            Forced input encoding (optional).
	 * @return Report.
	 * @throws IOException
	 *             In case of IO exception.
	 */
	public String convertEncodingRecursive(String dir, String subDir,
			String pattern, String toEncoding, String ouptputLocation,
			String inputEncoding) throws IOException {
		StringBuilder result = new StringBuilder();
		if (dir != null) {
			// init pattern
			Pattern p = null;
			if (pattern != null) {
				p = Pattern.compile(pattern);
			}

			// NEW ENCODING SUPPORTED ?
			if (Charset.isSupported(toEncoding)) {

				// INPUT ENCODING SUPPORTED ?
				if (inputEncoding == null || Charset.isSupported(inputEncoding)) {

					File file = new File(dir);
					if (file != null && file.isDirectory()) {
						File[] currentFiles = file.listFiles();
						for (File fileTmp : currentFiles) {
							if (fileTmp.isFile()) {
								// FILE
								boolean fileTmpMatch = true;
								if (p != null) {
									// filename match pattern ?
									Matcher matcher = p.matcher(fileTmp
											.getName());
									if (!matcher.matches()) {
										fileTmpMatch = false;
									}
								}

								if (fileTmpMatch) {
									// determine output location and do mkdirs
									// if do
									// not exist
									StringBuilder outputLocationTmp = null;
									if (ouptputLocation != null) {
										outputLocationTmp = new StringBuilder(
												ouptputLocation);
										if (subDir != null) {
											outputLocationTmp
													.append(File.separator);
											outputLocationTmp.append(subDir);
											File fileOutputLocationTmp = new File(
													outputLocationTmp
															.toString());
											if (!fileOutputLocationTmp.exists()) {
												if (!fileOutputLocationTmp
														.mkdirs()) {
													if (Enctool.DEBUG) {
														System.out
																.println("ERROR when try to create output directory : "
																		+ outputLocationTmp
																				.toString());
													}
												}
											}
										}
									}

									String outputLocationTmpString = null;
									if (outputLocationTmp != null) {
										outputLocationTmpString = outputLocationTmp
												.toString();
									}

									String reportTmp = this.convertEncoding(
											fileTmp.getAbsolutePath(),
											toEncoding,
											outputLocationTmpString,
											inputEncoding);
									result.append(fileTmp.getAbsolutePath());
									result.append(" : ");
									result.append(reportTmp);
									result.append('\n');
								}
							} else {
								// DIRECTORY
								// genrate new sub directory path
								StringBuilder newSubDir = new StringBuilder();
								if (subDir != null) {
									newSubDir.append(subDir);
									newSubDir.append(File.separator);
								}
								newSubDir.append(fileTmp.getName());

								// recursive call
								result.append(this.convertEncodingRecursive(
										fileTmp.getAbsolutePath(),
										newSubDir.toString(), pattern,
										toEncoding, ouptputLocation,
										inputEncoding));
							}
						}
					} else {
						result.append(dir + " : ERROR => It's not a directory.");
						result.append('\n');
					}
				} else {
					result.append("ERROR : input encoding \"" + inputEncoding
							+ "\" is not supported. ");
				}
			} else {
				result.append("ERROR : output encoding \"" + toEncoding
						+ "\" is not supported. ");
			}
		}
		return result.toString();
	}

	/**
	 * Detect encoding.
	 * 
	 * @param bis
	 *            File to analyse.
	 * @return Encoding Name if found, or null.
	 * @throws IOException
	 *             In case of reading error.
	 */
	private String simpleDetectEncoding(BufferedInputStream bis)
			throws IOException {
		String result = null;

		if (bis != null) {
			// analyse
			CharsetDetector detector;
			detector = new CharsetDetector();
			detector.setText(bis);
			CharsetMatch match = detector.detect();
			result = match.getName();
		}

		return result;
	}

	/**
	 * Generate report from a CharsetMatch.
	 * 
	 * @param match
	 *            Source of the generated report.
	 * @param advanced
	 *            true to add confidence and language in report.
	 * @param differentFrom
	 *            Display only file with different encoding that differentFrom.
	 * @return Small report.
	 */
	private String generateReport(CharsetMatch match, boolean advanced,
			String differentFrom) {
		StringBuilder report = new StringBuilder();

		if (match != null) {
			String name = match.getName();
			if (differentFrom == null || !differentFrom.equals(name)) {
				if (!advanced) {
					report.append(name);
				} else {
					int confidence = match.getConfidence();
					String language = match.getLanguage();
					// example : UTF-8,65,fr
					report.append(name);
					report.append(",");
					report.append(confidence);
					report.append(",");
					if (language != null) {
						report.append(language);
					} else {
						report.append("-");
					}
				}
			}

		} else {
			report.append("Encoding can not be determined :(");
		}

		return report.toString();
	}

	/**
	 * Return a BufferedInputStream from a path file.
	 * 
	 * @param path
	 *            Path to a file.
	 * @return BufferedInputStream or null if path is null.
	 * @throws FileNotFoundException
	 *             File not found.
	 */
	private BufferedInputStream inputStreamFromPath(String path)
			throws FileNotFoundException {
		BufferedInputStream bis = null;
		if (path != null) {
			FileInputStream fis = new FileInputStream(path);
			bis = new BufferedInputStream(fis);
		}

		return bis;
	}

}
