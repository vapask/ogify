package net.ogify.database.entities.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used for flag that field or smth should be valid telephone number.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TelephoneNumberValidator.class)
public @interface TelephoneNumber {
    String message() default "Wrong telephone number format";

    Class <?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
