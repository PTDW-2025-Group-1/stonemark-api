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
    public static final String PHOTO_PROCESSING_ERROR = "Sorry, there was an error processing your photo.";

}
