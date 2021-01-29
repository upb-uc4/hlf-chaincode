package de.upb.cs.uc4.chaincode.exceptions.serializable;

import de.upb.cs.uc4.chaincode.exceptions.SerializableError;

public class ParticipationError extends SerializableError {

    public ParticipationError(String jsonError) {
        super(jsonError);
    }
}
