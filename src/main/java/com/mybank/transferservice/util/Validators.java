package com.mybank.transferservice.util;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public class Validators {
    public static void validateOrThrow(BooleanSupplier constraint, RuntimeException ex) throws RuntimeException {
        Objects.requireNonNull(constraint);
        Objects.requireNonNull(ex);

        if (!constraint.getAsBoolean()) {
            throw ex;
        }
    }
}
