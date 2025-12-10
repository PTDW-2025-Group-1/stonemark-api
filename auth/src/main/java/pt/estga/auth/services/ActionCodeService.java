package pt.estga.auth.services;

import pt.estga.auth.entities.ActionCode;
import pt.estga.auth.enums.ActionCodeType;
import pt.estga.user.entities.User;

import java.util.Optional;

public interface ActionCodeService {

    ActionCode createAndSave(User user, ActionCodeType type);

    Optional<ActionCode> findByCode(String code);

    boolean isCodeValid(String code);

    void consumeCode(ActionCode code);
}
