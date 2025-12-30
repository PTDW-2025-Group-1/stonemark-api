package pt.estga.chatbot.features.proposal;

public final class ProposalCallbackData {

    private ProposalCallbackData() {
    }

    public static final String START_SUBMISSION = "start_submission";
    public static final String PROPOSE_NEW_MONUMENT = "propose_new_monument";
    public static final String PROPOSE_NEW_MARK = "propose_new_mark";
    public static final String SELECT_MONUMENT_PREFIX = "select_monument:";
    public static final String SELECT_MARK_PREFIX = "select_mark:";
    public static final String CONFIRM_MONUMENT_PREFIX = "confirm_monument:";
    public static final String CONFIRM_MARK_PREFIX = "confirm_mark:";
    public static final String LOOP_REDO_LOCATION = "loop_redo_location";
    public static final String LOOP_REDO_IMAGE_UPLOAD = "loop_redo_image_upload";
    public static final String LOOP_CONTINUE = "loop_continue";
    public static final String SUBMISSION_LOOP_OPTIONS = "submission_loop_options";
    public static final String SUBMISSION_LOOP_START_OVER = "submission_loop_start_over";
    public static final String SUBMISSION_LOOP_START_OVER_CONFIRMED = "submission_loop_start_over_confirmed";
    public static final String SUBMISSION_LOOP_CONTINUE = "submission_loop_continue";
    public static final String SKIP_NOTES = "skip_notes";
    public static final String CONTINUE_PROPOSAL = "continue_proposal";
    public static final String DELETE_AND_START_NEW = "delete_and_start_new";
    public static final String SHARE_LOCATION = "share_location";
}
