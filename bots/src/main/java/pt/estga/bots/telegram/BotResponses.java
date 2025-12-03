package pt.estga.bots.telegram;

public class BotResponses {

    public static final String GREETING = "Welcome to the Stonemark submission bot.\nPlease send a photo to begin the submission process.";
    public static final String HELP_MESSAGE = "Available commands:\n/start - Start a new submission\n/submit - Finalize a submission\n/cancel - Cancel current operation";
    public static final String CANCEL_MESSAGE = "Operation cancelled.";
    public static final String PHOTO_RECEIVED = "Got the photo! Now send me the location.";
    public static final String PHOTO_ERROR = "Sorry, there was an error processing your photo.";
    public static final String UNEXPECTED_PHOTO = "I wasn't expecting a photo right now.";
    public static final String LOCATION_RECEIVED = "Location received. Your proposal is ready. Use /submit to finalize.";
    public static final String UNEXPECTED_LOCATION = "I wasn't expecting a location right now.";
    public static final String SUBMISSION_SUCCESS = "Proposal submitted successfully!";
    public static final String NOTHING_TO_SUBMIT = "There is nothing to submit. Use /start to create a new proposal.";
    public static final String UNKNOWN_COMMAND = "Sorry, I don't recognize that command.";
    public static final String UNKNOWN_COMMAND_HELP = "Sorry, I don't recognize that command. Use /start to begin a new submission, or /help for a list of commands.";
    public static final String INVALID_INPUT_FOR_STATE = "Sorry, that's not what I was expecting. Please provide the requested information or use /cancel to stop.";
    public static final String SUBMISSION_NOT_READY = "Your submission is not yet complete. Please provide the missing information.";
    public static final String SUBMISSION_AWAITING_MONUMENT_INFO = "Your submission is not yet complete. Please provide the monument information.";
    public static final String SUBMISSION_AWAITING_MARK_INFO = "Your submission is not yet complete. Please provide the mark information.";
    public static final String PHOTO_PROCESSING_ERROR = "Sorry, there was an error processing your photo.";
    public static final String SUGGESTED_MARKS_FOUND = "I found some marks that might match. Please select one or propose a new one:";
    public static final String NO_SUGGESTED_MARKS = "No similar marks found. Please provide a title and description for a new mark.";
    public static final String AWAITING_MARK_SELECTION = "Please select a mark from the options below or choose to propose a new one.";
    public static final String AWAITING_MARK_DETAILS = "Please send the title and description for the new mark, separated by a newline (e.g., '\nTitle\nDescription').";
    public static final String MARK_SELECTED = "Mark selected. Your proposal is ready. Use /submit to finalize.";
    public static final String MARK_PROPOSED = "New mark proposed. Your proposal is ready. Use /submit to finalize.";
    public static final String ERROR_DESERIALIZING_MARK_IDS = "An error occurred while processing suggested marks.";
    public static final String AWAITING_MONUMENT_INFO = "No existing monument found nearby. Please send the location for a new monument.";
    public static final String AWAITING_MARK_INFO = "Please provide details for the mark (title and description).";
    public static final String INVALID_MARK_DETAILS_FORMAT = "Invalid format. Please send the title and description separated by a newline (e.g., '\nTitle\nDescription').";
    public static final String AWAITING_MONUMENT_VERIFICATION = "I've found a location from the photo's metadata. Is this correct?";
    public static final String SUGGESTED_MONUMENTS_FOUND = "I found some monuments that might match. Please select one or propose a new one:";
    public static final String ERROR_DESERIALIZING_MONUMENT_IDS = "An error occurred while processing suggested monuments.";
    public static final String READY_TO_SUBMIT_MESSAGE = "Your proposal is ready for submission. Use /submit to finalize.";
}
