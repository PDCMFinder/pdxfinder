package org.pdxfinder.dataloaders.updog.tablevalidation.error;

public class GenericError implements ValidationError {
    @Override public String message() {
        return "Generic error";
    }
}
