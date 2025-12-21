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
    public static final String CONFIRM_MARK_MATCH_PREFIX = "confirm_mark_match:";
    public static final String SEND_LOCATION_MANUALLY = "send_location_manually";
    public static final String USE_DETECTED_COORDINATES = "use_detected_coordinates";
    public static final String CONFIRM_YES = "yes";
    public static final String CONFIRM_NO = "no";
    public static final String LOOP_OPTIONS = "loop_options";
    public static final String LOOP_REDO_MONUMENT = "loop_redo_monument";
    public static final String LOOP_REDO_IMAGE_UPLOAD = "loop_redo_image_upload";
    public static final String LOOP_CONTINUE = "loop_continue";

}
