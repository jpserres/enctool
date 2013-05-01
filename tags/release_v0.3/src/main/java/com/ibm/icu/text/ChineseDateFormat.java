/*********************************************************************
 * Copyright (C) 2000-2012, International Business Machines Corporation and
 * others. All Rights Reserved.
 *********************************************************************
 */
package com.ibm.icu.text;

import java.io.InvalidObjectException;
import java.text.FieldPosition;
import java.util.Locale;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ChineseCalendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

/**
 * A concrete {@link DateFormat} for {@link com.ibm.icu.util.ChineseCalendar}.
 * This class handles a <code>ChineseCalendar</code>-specific field,
 * <code>ChineseCalendar.IS_LEAP_MONTH</code>.  It also redefines the
 * handling of two fields, <code>ERA</code> and <code>YEAR</code>.  The
 * former is displayed numerically, instead of symbolically, since it is
 * the numeric cycle number in <code>ChineseCalendar</code>.  The latter is
 * numeric, as before, but has no special 2-digit Y2K behavior.
 *
 * <p>With regard to <code>ChineseCalendar.IS_LEAP_MONTH</code>, this
 * class handles parsing specially.  If no string symbol is found at all,
 * this is taken as equivalent to an <code>IS_LEAP_MONTH</code> value of
 * zero.  This allows formats to display a special string (e.g., "*") for
 * leap months, but no string for normal months.
 *
 * <p>Summary of field changes vs. {@link SimpleDateFormat}:<pre>
 * Symbol   Meaning                 Presentation        Example
 * ------   -------                 ------------        -------
 * G        cycle                   (Number)            78
 * y        year of cycle (1..60)   (Number)            17
 * l        is leap month           (Text)              4637
 * </pre>
 *
 * @see com.ibm.icu.util.ChineseCalendar
 * @see ChineseDateFormatSymbols
 * @author Alan Liu
 * @stable ICU 2.0
 */
public class ChineseDateFormat extends SimpleDateFormat {
    // Generated by serialver from JDK 1.4.1_01
    static final long serialVersionUID = -4610300753104099899L;
    
    // TODO Finish the constructors

    /**
     * Construct a ChineseDateFormat from a date format pattern and locale
     * @param pattern the pattern
     * @param locale the locale
     * @stable ICU 2.0
     */
   public ChineseDateFormat(String pattern, Locale locale) {
       this(pattern, ULocale.forLocale(locale));
    }

    /**
     * Construct a ChineseDateFormat from a date format pattern and locale
     * @param pattern the pattern
     * @param locale the locale
     * @stable ICU 3.2
     */
   public ChineseDateFormat(String pattern, ULocale locale) {
       this(pattern, null, locale);
    }

    /**
     * Construct a ChineseDateFormat from a date format pattern, numbering system override and locale
     * @param pattern the pattern
     * @param override The override string.  A numbering system override string can take one of the following forms:
     *     1). If just a numbering system name is specified, it applies to all numeric fields in the date format pattern.
     *     2). To specify an alternate numbering system on a field by field basis, use the field letters from the pattern
     *         followed by an = sign, followed by the numbering system name.  For example, to specify that just the year
     *         be formatted using Hebrew digits, use the override "y=hebr".  Multiple overrides can be specified in a single
     *         string by separating them with a semi-colon. For example, the override string "m=thai;y=deva" would format using
     *         Thai digits for the month and Devanagari digits for the year.
     * @param locale the locale
     * @stable ICU 4.2
     */
   public ChineseDateFormat(String pattern, String override, ULocale locale) {
       super(pattern, new ChineseDateFormatSymbols(locale), 
               new ChineseCalendar(TimeZone.getDefault(), locale), locale, true, override);
    }

// NOTE: This API still exists; we just inherit it from SimpleDateFormat
// as of ICU 3.0
//  /**
//   * @stable ICU 2.0
//   */
//  protected String subFormat(char ch, int count, int beginOffset,
//                             FieldPosition pos, DateFormatSymbols formatData,
//                             Calendar cal)  {
//      switch (ch) {
//      case 'G': // 'G' - ERA
//          return zeroPaddingNumber(cal.get(Calendar.ERA), 1, 9);
//      case 'l': // 'l' - IS_LEAP_MONTH
//          {
//              ChineseDateFormatSymbols symbols =
//                  (ChineseDateFormatSymbols) formatData;
//              return symbols.getLeapMonth(cal.get(
//                             ChineseCalendar.IS_LEAP_MONTH));
//          }
//      default:
//          return super.subFormat(ch, count, beginOffset, pos, formatData, cal);
//      }
//  }    

    /**
     * {@inheritDoc}
     * @internal
     * @deprecated This API is ICU internal only.
     */
    protected void subFormat(StringBuffer buf,
                             char ch, int count, int beginOffset,
                             int fieldNum, ContextValue capitalizationContext,
                             FieldPosition pos,
                             Calendar cal) {

        // Logic to handle 'G' for chinese calendar is moved into SimpleDateFormat,
        // and obsolete pattern char 'l' is now ignored in SimpleDateFormat, so we
        // just use its implementation
        super.subFormat(buf, ch, count, beginOffset, fieldNum, capitalizationContext, pos, cal);

        // The following is no longer an issue for this subclass...
        // TODO: add code to set FieldPosition for 'G' and 'l' fields. This
        // is a DESIGN FLAW -- subclasses shouldn't have to duplicate the
        // code that handles this at the end of SimpleDateFormat.subFormat.
        // The logic should be moved up into SimpleDateFormat.format.
    }

    /**
     * {@inheritDoc}
     * 
     * @stable ICU 2.0
     */
    protected int subParse(String text, int start, char ch, int count, boolean obeyCount, boolean allowNegative,
            boolean[] ambiguousYear, Calendar cal) {
        // Logic to handle numeric 'G' eras for chinese calendar, and to skip special 2-digit year
        // handling for chinese calendar, is moved into SimpleDateFormat, so delete here.
        // Obsolete pattern char 'l' is now ignored for parsing in SimpleDateFormat, no handling
        // needed here.
        // So just use SimpleDateFormat implementation for this.
        // just use its implementation
        return super.subParse(text, start, ch, count, obeyCount, allowNegative, ambiguousYear, cal);
    }

    /**
     * {@inheritDoc}
     * 
     * @stable ICU 3.8
     */
    protected DateFormat.Field patternCharToDateFormatField(char ch) {
        // no longer any field corresponding to pattern char 'l'
        return super.patternCharToDateFormatField(ch);
    }

    /**
     * The instances of this inner class are used as attribute keys and values
     * in AttributedCharacterIterator that
     * ChineseDateFormat.formatToCharacterIterator() method returns.
     * <p>
     * There is no public constructor to this class, the only instances are the
     * constants defined here.
     * <p>
     * @stable ICU 3.8
     */
    public static class Field extends DateFormat.Field {

        private static final long serialVersionUID = -5102130532751400330L;

        /**
         * Constant identifying the leap month marker.
         * @stable ICU 3.8
         */
        public static final Field IS_LEAP_MONTH = new Field("is leap month", ChineseCalendar.IS_LEAP_MONTH);

        /**
         * Constructs a <code>ChineseDateFormat.Field</code> with the given name and
         * the <code>ChineseCalendar</code> field which this attribute represents.
         * Use -1 for <code>calendarField</code> if this field does not have a
         * corresponding <code>ChineseCalendar</code> field.
         * 
         * @param name          Name of the attribute
         * @param calendarField <code>Calendar</code> field constant
         * 
         * @stable ICU 3.8
         */
        protected Field(String name, int calendarField) {
            super(name, calendarField);
        }

        /**
         * Returns the <code>Field</code> constant that corresponds to the <code>
         * ChineseCalendar</code> field <code>calendarField</code>.  If there is no
         * corresponding <code>Field</code> is available, null is returned.
         * 
         * @param calendarField <code>ChineseCalendar</code> field constant
         * @return <code>Field</code> associated with the <code>calendarField</code>,
         * or null if no associated <code>Field</code> is available.
         * @throws IllegalArgumentException if <code>calendarField</code> is not
         * a valid <code>Calendar</code> field constant.
         * 
         * @stable ICU 3.8
         */
        public static DateFormat.Field ofCalendarField(int calendarField) {
            // Should we remove the following, since there is no longer a specific
            // date format field for leap month (since 'l' pattern char is obsolete)?
            if (calendarField == ChineseCalendar.IS_LEAP_MONTH) {
                return IS_LEAP_MONTH;
            }
            return DateFormat.Field.ofCalendarField(calendarField);
        }

        /**
         * {@inheritDoc}
         * 
         * @stable ICU 3.8
         */
        ///CLOVER:OFF
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != ChineseDateFormat.Field.class) {
                throw new InvalidObjectException("A subclass of ChineseDateFormat.Field must implement readResolve.");
            }
            if (this.getName().equals(IS_LEAP_MONTH.getName())) {
                return IS_LEAP_MONTH;
            } else {
                throw new InvalidObjectException("Unknown attribute name.");
            }
        }
        ///CLOVER:ON
    }
}
