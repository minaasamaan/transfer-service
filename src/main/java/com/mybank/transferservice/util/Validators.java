package com.mybank.transferservice.util;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public class Validators {
    /**
     * A convenient method to define business rules and exception to throw if the rule has been violated in one liner.
     *
     * @param constraint to validate
     * @param ex         to throw if constraint has been violated
     */
    public static void validateOrThrow(BooleanSupplier constraint, RuntimeException ex) {
        Objects.requireNonNull(constraint);
        Objects.requireNonNull(ex);

        if (!constraint.getAsBoolean()) {
            throw ex;
        }
    }
}
