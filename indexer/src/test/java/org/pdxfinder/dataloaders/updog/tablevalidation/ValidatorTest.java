package org.pdxfinder.dataloaders.updog.tablevalidation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ValidatorTest {

    @InjectMocks private Validator validator;

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test public void validate_givenNoValidation_producesEmptyErrorList() {
        assertThat(validator.getValidationErrors().isEmpty(), is(true));
    }

}