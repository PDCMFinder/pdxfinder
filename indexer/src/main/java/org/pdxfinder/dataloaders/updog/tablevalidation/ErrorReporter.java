package org.pdxfinder.dataloaders.updog.tablevalidation;

import org.apache.commons.collections4.CollectionUtils;
import org.pdxfinder.dataloaders.updog.tablevalidation.error.ValidationError;
import org.slf4j.*;

import java.util.*;

public class ErrorReporter {

    private List<ValidationError> errors;
    private static final Logger log = LoggerFactory.getLogger(ErrorReporter.class);

    public ErrorReporter(List<ValidationError> errors) {
        this.errors = errors;
    }

    public int  count() {
        return errors.size();
    }

    public void logErrors() {
        if (CollectionUtils.isNotEmpty(errors)) {
            log.error("{} validation errors found:", errors.size());
            for (ValidationError error : errors) {
                log.error(error.message());
            }
        } else {
            log.info("There were no validation errors raised, great!");
        }
    }

    public ErrorReporter truncate(int limit) {
        log.info("Limiting output to the first {} errors:", limit);
        return new ErrorReporter(truncateList(errors, limit));
    }

    private <E> List<E> truncateList(List<E> list, int size) {
        if (list.size() > size) {
            return list.subList(0, size);
        } else {
            return list;
        }
    }

}
