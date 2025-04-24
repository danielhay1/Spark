package com.example.spark.untils.input_validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberValidator {
    private static final String PHONE_NUMBER_REGEX = "^(\\+\\d{1,3}[- ]?)?\\d{10}$"; // Define the regex pattern
    private static boolean regexTest(String input){
        // Compile the regex pattern
        Pattern pattern = Pattern.compile(PHONE_NUMBER_REGEX);
        // Match the pattern with the input phone number
        Matcher matcher = pattern.matcher(input);
        // Return whether the phone number is valid or not
        return matcher.matches();
    }

    public static boolean isValid(String input) {
        return regexTest(input);
    }
}
