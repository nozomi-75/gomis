/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Utility class for common validation methods.
 */
public class ValidationUtils {
    /**
     * Checks if a text field is empty.
     * @param field the JTextField to check
     * @return true if empty, false otherwise
     */
    public static boolean isFieldEmpty(JTextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    /**
     * Checks if a text area is empty.
     * @param area the JTextArea to check
     * @return true if empty, false otherwise
     */
    public static boolean isFieldEmpty(JTextArea area) {
        return area.getText() == null || area.getText().trim().isEmpty();
    }

    /**
     * Checks if a string is a valid date in yyyy-MM-dd format.
     * @param dateStr the date string
     * @return true if valid, false otherwise
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Checks if a string is a valid email address.
     * @param email the email string
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    /**
     * Checks if a string is a valid phone number (basic check).
     * @param phone the phone string
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        return phone.matches("^\\+?[0-9 .-]{7,15}$");
    }
} 