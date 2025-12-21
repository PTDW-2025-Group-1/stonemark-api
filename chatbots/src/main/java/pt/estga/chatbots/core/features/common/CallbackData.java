package pt.estga.chatbots.core.features.common;

public final class CallbackData {

    private CallbackData() {
    }

    public static final String START_SUBMISSION = "start_submission";
    public static final String START_VERIFICATION = "start_verification";
    public static final String SUBMIT_PROPOSAL = "submit_proposal";
    public static final String PROPOSE_NEW_MONUMENT = "propose_new_monument";
    public static final String PROPOSE_NEW_MARK = "propose_new_mark";
    public static final String SELECT_MONUMENT_PREFIX = "select_monument:";
    public static final String SELECT_MARK_PREFIX = "select_mark:";
    public static final String CONFIRM_MONUMENT_PREFIX = "confirm_monument:";
    public static final String CONFIRM_YES = "yes";
    public static final String CONFIRM_NO = "no";
    public static final String LOOP_OPTIONS = "loop_options";
    public static final String LOOP_REDO_LOCATION = "loop_redo_location";
    public static final String LOOP_REDO_IMAGE_UPLOAD = "loop_redo_image_upload";
    public static final String LOOP_CONTINUE = "loop_continue";
    public static final String SUBMISSION_LOOP_OPTIONS = "submission_loop_options";
    public static final String SUBMISSION_LOOP_CHANGE_MARK = "submission_loop_change_mark";
    public static final String SUBMISSION_LOOP_CHANGE_MONUMENT = "submission_loop_change_monument";
    public static final String SUBMISSION_LOOP_START_OVER = "submission_loop_start_over";
    public static final String SUBMISSION_LOOP_START_OVER_CONFIRMED = "submission_loop_start_over_confirmed";
    public static final String SUBMISSION_LOOP_CONTINUE = "submission_loop_continue";
    public static final String BACK_TO_MAIN_MENU = "back_to_main_menu";
}
