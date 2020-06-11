package org.pdxfinder.dataloaders.updog.tablevalidation.error;

public interface ValidationError {
    String message();
    default String verboseMessage() {
        return message();
    }

}
