/*
 *   Copyright 2012-2013 Jean-Philippe Serres
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

package fr.serres.enctool.enums;

/**
 * Command line options.
 * 
 * @author Jean-Philippe Serres
 * 
 */
public enum CLOptions {
	DETECT_ENCODING_FILE("-e"), //
	DETECT_ENCODING_FILE_ADVANCED("-E"), //
	DETECT_ENCODING_DIR("-r"), //
	DETECT_ENCODING_DIR_ADVANCED("-R"), //
	DISPLAY_ONLY_FILES_WITH_DIFFERENT_ENCODING("-d"), //
	CONVERT_FILE("-c"), //
	CONVERT_FILES_DIR("-cr"), //
	FORCE_INPUT_ENCODING("-f"),//
	CHANGE_OUTPUT_LOCATION("-o"),//
	ADD_BOM("-bom"),//
	NO_BOM("-nobom"),//
	PATTERN_FILENAME("-p"), //
	ALL_CONFIDENCES("-a"), //
	SUPPORTED_ENCODINGS("-se"), //
	VERSION("-v"), //
	UNDEFINED("");

	private String syntax;

	private CLOptions(String syntax) {
		this.syntax = syntax;
	}

	public String getSyntax() {
		return this.syntax;

	}

	public static CLOptions getCLOptionFromSyntax(String syntax) {
		CLOptions result = UNDEFINED;
		if (syntax != null) {
			for (CLOptions o : CLOptions.values()) {
				if (o.getSyntax().equals(syntax)) {
					result = o;
					break;
				}
			}
		}
		return result;
	}

}
