package com.ashish.rentflatmatefinder.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void blankPhoneNumberShouldNotTriggerValidationError() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");
        request.setPassword("Password1!");
        request.setPhoneNumber("");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void simplePasswordShouldBeAccepted() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane2@example.com");
        request.setPassword("password123");
        request.setPhoneNumber("");

        assertTrue(validator.validate(request).isEmpty());
    }
}
