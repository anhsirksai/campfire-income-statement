package com.campfire.ledger.exception;

import java.util.List;

/** ledger.json could not be read or failed integrity rules. Thrown at startup, so the app refuses to start. */
public class LedgerDataException extends RuntimeException {

    public LedgerDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public LedgerDataException(List<String> errors) {
        super("Ledger data failed validation: " + String.join("; ", errors));
    }
}
