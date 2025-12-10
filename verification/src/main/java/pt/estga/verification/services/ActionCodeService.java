package pt.estga.verification.services;

import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;
import pt.estga.user.entities.User;

import java.util.Optional;

public interface ActionCodeService {

    ActionCode createAndSave(User user, ActionCodeType type);

    Optional<ActionCode> findByCode(String code);

    boolean isCodeValid(String code);

    void consumeCode(ActionCode code);
}
