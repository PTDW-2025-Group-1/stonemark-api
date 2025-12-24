package pt.estga.chatbots.core.shared;

public class Messages {

    // General
    public static final String WELCOME = "Welcome! 👋";
    public static final String WELCOME_BACK = "Welcome back, %s! 👋";
    public static final String ERROR_GENERIC = "An error occurred. Please try again. ⚠️";
    public static final String ERROR_PROCESSING_PHOTO = "Error processing photo. Please try again. ⚠️";
    public static final String ERROR_PROCESSING_SUBMISSION = "Error processing submission. ⚠️";
    public static final String INVALID_SELECTION = "Invalid selection. Please try again. ⚠️";

    // Proposal Flow
    public static final String INCOMPLETE_SUBMISSION_TITLE = "You have an incomplete submission. What would you like to do?";
    public static final String CONTINUE_SUBMISSION_BTN = "Continue Submission ➡️";
    public static final String START_NEW_SUBMISSION_BTN = "Start New (Deletes Old) 🗑️";
    
    public static final String SEND_PHOTO_PROMPT = "Please send a clear photo of the mark. 📸";
    public static final String EXPECTING_PHOTO_ERROR = "I was expecting a photo. Please upload an image to continue. 📸";
    
    public static final String PROVIDE_LOCATION_PROMPT = "Thank you. Now, please provide the location of the mark. 📍";
    public static final String LOCATION_REQUEST_MESSAGE = "Please provide the location.\n\nTap the button to send your **current location**, or use the attachment menu (📎) to **pick a location on the map**. 📍";
    public static final String EXPECTING_LOCATION_ERROR = "I was expecting a location. Please share your location to continue. 📍";
    
    public static final String LOOP_OPTIONS_TITLE = "What would you like to do next?";
    public static final String CHANGE_LOCATION_BTN = "Change Location 📍";
    public static final String CHANGE_PHOTO_BTN = "Change Photo 📸";
    public static final String CONTINUE_BTN = "Continue ➡️";
    public static final String SEND_NEW_LOCATION_PROMPT = "Please send the new location.\n\nTap the button to send your **current location**, or use the attachment menu (📎) to **pick a location on the map**. 📍";
    public static final String UPLOAD_NEW_IMAGE_PROMPT = "Please upload a new image. 📸";

    public static final String SELECT_MARK_PROMPT = "Please select a mark from the list or propose a new one. 🔍";
    public static final String FOUND_MARKS_TITLE = "I found some marks that might match. Please select one or propose a new one: 🔍";
    public static final String FOUND_SINGLE_MARK_TITLE = "I found a mark that looks similar:";
    public static final String MATCH_CONFIRMATION_TITLE = "Does it match with the one that you uploaded?";
    public static final String CONFIRM_MARK_MATCH_PROMPT = "Please confirm if the mark matches by clicking Yes or No.";
    public static final String PROPOSE_NEW_MARK_BTN = "Propose New Mark 🆕";
    public static final String NO_MARKS_FOUND_PROMPT = "No existing marks found. Please enter the details for this new mark, or skip.";
    public static final String SKIP_BTN = "Skip ➡️";
    public static final String SKIP_MARK_DETAILS_BTN = "Skip";
    
    public static final String PROVIDE_NEW_MARK_DETAILS_PROMPT = "Understood. Please provide additional details for this new mark. 📝";

    public static final String SELECT_MONUMENT_PROMPT = "Please select a monument from the list. 🏛️";
    public static final String CONFIRM_MONUMENT_MATCH_PROMPT = "Please confirm if the monument matches by clicking Yes or No.";
    public static final String MONUMENT_CONFIRMATION_TITLE = "Was this photo taken at %s?";
    public static final String NO_MONUMENTS_FOUND_PROMPT = "No nearby monuments found. Please enter the monument name.";
    public static final String PROVIDE_NEW_MONUMENT_NAME_PROMPT = "Understood. Please provide the name of the new monument. 🏛️";
    
    public static final String SUBMISSION_LOOP_TITLE = "Review your submission. What would you like to do next?";
    public static final String CHANGE_MARK_BTN = "Change Mark 🔄";
    public static final String CHANGE_MONUMENT_BTN = "Change Monument 🏛️";
    public static final String DISCARD_SUBMISSION_BTN = "Discard Submission 🗑️";
    public static final String CONTINUE_TO_SUBMIT_BTN = "Continue to Submit ➡️";
    
    public static final String DISCARD_CONFIRMATION_TITLE = "Are you sure you want to discard this submission? ⚠️";
    public static final String YES_DISCARD_BTN = "Yes, Discard 🗑️";
    public static final String NO_GO_BACK_BTN = "No, Go Back 🔙";
    
    public static final String ADD_NOTES_PROMPT = "Please add any notes for this proposal. 📝";
    public static final String SUBMISSION_SUCCESS = "Thank you for your submission! 🎉";
    
    public static final String YES_BTN = "✅ Yes";
    public static final String NO_BTN = "❌ No";

    // Authentication & Verification
    public static final String AUTH_REQUIRED_TITLE = "To use this chatbot, you need to verify your account. 🔒";
    public static final String VERIFY_ACCOUNT_BTN = "Verify Account 🔐";
    public static final String SHARE_PHONE_NUMBER_PROMPT = "To begin, please share your phone number. 📱";
    public static final String VERIFICATION_SUCCESS = "Thank you, %s! Your account is now verified. ✅";
    public static final String VERIFICATION_SUCCESS_CODE = "Thank you, %s! Your account has been successfully verified. ✅";
    public static final String USER_NOT_FOUND_ERROR = "Sorry, we couldn't find an account associated with that phone number. Please try again or contact support. ⚠️";
    public static final String VERIFICATION_ERROR_GENERIC = "An unexpected error occurred. Please start the verification process again. ⚠️";
    public static final String INVALID_CODE_ERROR = "That code is invalid or has expired. Please try again. ⚠️";
    
    public static final String ENTER_VERIFICATION_CODE_PROMPT = "Please enter the verification code from the website. 🔢";
    public static final String SHARE_CONTACT_PROMPT = "Please share your contact information to verify your account. 📱";
    public static final String ENTER_CODE_AFTER_CONTACT_PROMPT = "Thank you. Now, please enter the verification code from the website. 🔢";

    private Messages() {}
}
