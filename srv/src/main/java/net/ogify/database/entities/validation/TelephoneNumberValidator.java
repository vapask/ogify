package net.ogify.database.entities.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Simple validator for strings which represents telephone number.
 *
 * @author Morgen Matvey
 */
public class TelephoneNumberValidator implements ConstraintValidator<TelephoneNumber, String> {
    protected static final Pattern telephoneNumberPattern = Pattern.compile("[+]\\d{11}");

    @Override
    public void initialize(TelephoneNumber telephoneNumber) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value == null || telephoneNumberPattern.matcher(value).matches();
    }
}
