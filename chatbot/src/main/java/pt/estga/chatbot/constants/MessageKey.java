package pt.estga.chatbot.constants;

public class MessageKey {

    // General
    public static final String WELCOME = "welcome";
    public static final String WELCOME_BACK = "welcome_back";
    public static final String ERROR_GENERIC = "error_generic";
    public static final String ERROR_PROCESSING_PHOTO = "error_processing_photo";
    public static final String ERROR_PROCESSING_SUBMISSION = "error_processing_submission";
    public static final String INVALID_SELECTION = "invalid_selection";
    public static final String FALLBACK_UNHANDLED_INPUT = "fallback_unhandled_input";
    public static final String HELP_OPTIONS_TITLE = "help_options_title";

    // Proposal Flow
    public static final String PROPOSE_MARK_BTN = "propose_mark_btn";
    public static final String INCOMPLETE_SUBMISSION_TITLE = "incomplete_submission_title";
    public static final String CONTINUE_SUBMISSION_BTN = "continue_submission_btn";
    public static final String START_NEW_SUBMISSION_BTN = "start_new_submission_btn";
    
    public static final String REQUEST_PHOTO_PROMPT = "request_photo_prompt";
    public static final String EXPECTING_PHOTO_ERROR = "expecting_photo_error";
    
    public static final String REQUEST_LOCATION_PROMPT = "request_location_prompt";
    public static final String EXPECTING_LOCATION_ERROR = "expecting_location_error";
    public static final String LOCATION_RECEIVED = "location_received";
    
    public static final String LOOP_OPTIONS_TITLE = "loop_options_title";
    public static final String CHANGE_LOCATION_BTN = "change_location_btn";
    public static final String CHANGE_PHOTO_BTN = "change_photo_btn";
    public static final String CONTINUE_BTN = "continue_btn";

    public static final String SELECT_MARK_PROMPT = "select_mark_prompt";
    public static final String FOUND_MARKS_TITLE = "found_marks_title";
    public static final String FOUND_SINGLE_MARK_TITLE = "found_single_mark_title";
    public static final String MATCH_CONFIRMATION_TITLE = "match_confirmation_title";
    public static final String CONFIRM_MARK_MATCH_PROMPT = "confirm_mark_match_prompt";
    public static final String PROPOSE_NEW_MARK_BTN = "propose_new_mark_btn";
    public static final String MARK_CAPTION = "mark_caption";
    public static final String IF_NONE_OF_ABOVE_OPTIONS_MATCH = "if_none_of_above_options_match";
    public static final String NO_MARKS_FOUND_PROMPT = "no_marks_found_prompt";
    public static final String SKIP_BTN = "skip_btn";

    public static final String PROVIDE_NEW_MARK_DETAILS_PROMPT = "provide_new_mark_details_prompt";

    public static final String SELECT_MONUMENT_PROMPT = "select_monument_prompt";
    public static final String CONFIRM_MONUMENT_MATCH_PROMPT = "confirm_monument_match_prompt";
    public static final String MONUMENT_CONFIRMATION_TITLE = "monument_confirmation_title";
    public static final String NO_MONUMENTS_FOUND_PROMPT = "no_monuments_found_prompt";
    public static final String PROVIDE_NEW_MONUMENT_NAME_PROMPT = "provide_new_monument_name_prompt";
    
    public static final String SUBMISSION_LOOP_TITLE = "submission_loop_title";
    public static final String CHANGE_MARK_BTN = "change_mark_btn";
    public static final String CHANGE_MONUMENT_BTN = "change_monument_btn";
    public static final String DISCARD_SUBMISSION_BTN = "discard_submission_btn";
    public static final String CONTINUE_TO_SUBMIT_BTN = "continue_to_submit_btn";
    
    public static final String DISCARD_CONFIRMATION_TITLE = "discard_confirmation_title";
    public static final String YES_DISCARD_BTN = "yes_discard_btn";
    public static final String NO_GO_BACK_BTN = "no_go_back_btn";
    
    public static final String ADD_NOTES_PROMPT = "add_notes_prompt";
    public static final String SUBMISSION_SUCCESS = "submission_success";

    public static final String SELECT_BTN = "select_btn";

    public static final String YES_BTN = "yes_btn";
    public static final String NO_BTN = "no_btn";

    // Authentication & Verification
    public static final String AUTH_REQUIRED_TITLE = "auth_required_title";
    public static final String VERIFY_ACCOUNT_BTN = "verify_account_btn";
    public static final String SHARE_PHONE_NUMBER_PROMPT = "share_phone_number_prompt";
    public static final String VERIFICATION_SUCCESS = "verification_success";
    public static final String VERIFICATION_SUCCESS_CODE = "verification_success_code";
    public static final String USER_NOT_FOUND_ERROR = "user_not_found_error";
    public static final String VERIFICATION_ERROR_GENERIC = "verification_error_generic";
    public static final String INVALID_CODE_ERROR = "invalid_code_error";
    
    public static final String ENTER_VERIFICATION_CODE_PROMPT = "enter_verification_code_prompt";
    public static final String SHARE_CONTACT_PROMPT = "share_contact_prompt";
    public static final String ENTER_CODE_AFTER_CONTACT_PROMPT = "enter_code_after_contact_prompt";
    
    public static final String CHOOSE_VERIFICATION_METHOD_PROMPT = "choose_verification_method_prompt";
    public static final String VERIFY_WITH_CODE_BTN = "verify_with_code_btn";
    public static final String VERIFY_WITH_PHONE_BTN = "verify_with_phone_btn";
    public static final String VERIFICATION_SUCCESS_PHONE = "verification_success_phone";
    public static final String PROMPT_CONNECT_PHONE = "prompt_connect_phone";
    public static final String PHONE_CONNECTION_SUCCESS = "phone_connection_success";


    private MessageKey() {}
}
