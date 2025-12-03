package pt.estga.bots.telegram.state.factory;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pt.estga.bots.telegram.state.*;
import pt.estga.proposals.enums.ProposalStatus;

import java.util.EnumMap;
import java.util.Map;

@Component
public class StateFactoryImpl implements StateFactory {

    private final AwaitingMarkInfoState awaitingMarkInfoState;
    private final AwaitingMarkSelectionState awaitingMarkSelectionState;
    private final AwaitingMonumentInfoState awaitingMonumentInfoState;
    private final AwaitingMonumentSelectionState awaitingMonumentSelectionState;
    private final AwaitingMonumentVerificationState awaitingMonumentVerificationState;
    private final ReadyToSubmitState readyToSubmitState;
    private final InitialState initialState;

    private Map<ProposalStatus, ConversationState> stateMap;

    public StateFactoryImpl(@Lazy AwaitingMarkInfoState awaitingMarkInfoState,
                            @Lazy AwaitingMarkSelectionState awaitingMarkSelectionState,
                            @Lazy AwaitingMonumentInfoState awaitingMonumentInfoState,
                            @Lazy AwaitingMonumentSelectionState awaitingMonumentSelectionState,
                            @Lazy AwaitingMonumentVerificationState awaitingMonumentVerificationState,
                            @Lazy ReadyToSubmitState readyToSubmitState,
                            @Lazy InitialState initialState) {
        this.awaitingMarkInfoState = awaitingMarkInfoState;
        this.awaitingMarkSelectionState = awaitingMarkSelectionState;
        this.awaitingMonumentInfoState = awaitingMonumentInfoState;
        this.awaitingMonumentSelectionState = awaitingMonumentSelectionState;
        this.awaitingMonumentVerificationState = awaitingMonumentVerificationState;
        this.readyToSubmitState = readyToSubmitState;
        this.initialState = initialState;
    }

    @PostConstruct
    public void init() {
        stateMap = new EnumMap<>(ProposalStatus.class);
        stateMap.put(ProposalStatus.IN_PROGRESS, initialState);
        stateMap.put(ProposalStatus.AWAITING_MARK_INFO, awaitingMarkInfoState);
        stateMap.put(ProposalStatus.AWAITING_MARK_SELECTION, awaitingMarkSelectionState);
        stateMap.put(ProposalStatus.AWAITING_MONUMENT_INFO, awaitingMonumentInfoState);
        stateMap.put(ProposalStatus.AWAITING_MONUMENT_SELECTION, awaitingMonumentSelectionState);
        stateMap.put(ProposalStatus.AWAITING_MONUMENT_VERIFICATION, awaitingMonumentVerificationState);
        stateMap.put(ProposalStatus.READY_TO_SUBMIT, readyToSubmitState);
    }

    @Override
    public ConversationState createState(ProposalStatus status) {
        return stateMap.get(status);
    }
}
