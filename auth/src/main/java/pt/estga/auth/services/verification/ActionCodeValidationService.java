package pt.estga.auth.services.verification;

import pt.estga.auth.entities.ActionCode;

public interface ActionCodeValidationService {

    ActionCode getValidatedActionCode(String value);
}
