package pt.estga.chatbots.telegram;

public class BotResponses {

    public static final String GREETING = "Welcome to the Stonemark submission bot.\nPlease send a photo to begin the submission process.";
    public static final String HELP_MESSAGE = "Available commands:\n/start - Start a new submission\n/submit - Finalize a submission\n/cancel - Cancel current operation\n/skip - Skip the current optional step";
    public static final String CANCEL_MESSAGE = "Operation cancelled.";
    public static final String PHOTO_RECEIVED = "Got the photo! Now send me the location.";
    public static final String PHOTO_ERROR = "Sorry, there was an error processing your photo.";
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
    public static final String NO_SUGGESTED_MARKS = "No similar marks found. Please provide a description for a new mark, or use /skip to skip.";
    public static final String AWAITING_MARK_SELECTION = "Please select a mark from the options below or choose to propose a new one.";
    public static final String AWAITING_MARK_DETAILS = "Please send a description for the new mark, or use /skip to skip.";
    public static final String MARK_SELECTED = "Mark selected. Your proposal is ready. Use /submit to finalize.";
    public static final String MARK_PROPOSED = "New mark proposed. Your proposal is ready. Use /submit to finalize.";
    public static final String ERROR_DESERIALIZING_MARK_IDS = "An error occurred while processing suggested marks.";
    public static final String AWAITING_MONUMENT_INFO = "No existing monument found nearby. Please send the location for a new monument.";
    public static final String AWAITING_MARK_INFO = "Please provide a description for the mark, or use /skip to skip.";
    public static final String INVALID_MARK_DETAILS_FORMAT = "Invalid format. Please send the description for the new mark.";
    public static final String AWAITING_MONUMENT_VERIFICATION = "I've found a location from the photo's metadata. Is this correct?";
    public static final String SUGGESTED_MONUMENTS_FOUND = "I found some monuments that might match. Please select one or propose a new one:";
    public static final String READY_TO_SUBMIT_MESSAGE = "Your proposal is ready for submission. Use /submit to finalize.";
    public static final String AWAITING_NOTES_MESSAGE = "Please add any notes for your proposal, or use /skip to skip.";
    public static final String NOTHING_TO_SKIP_MESSAGE = "There is nothing to skip at this moment.";
    public static final String AWAITING_MONUMENT_NAME = "Location received. What is the name of the monument?";
    public static final String SEARCHING_FOR_MATCHES = "Got it. Searching for matches...";
    public static final String AUTHENTICATION_REQUEST = "To get started, please share your phone number so I can authenticate you.";
    public static final String AUTHENTICATION_SUCCESS = "Welcome, %s! You are now authenticated. To start a new submission, please send a photo.";
    public static final String AUTHENTICATION_FAILED = "Sorry, I couldn't find an account with that phone number. Please register on our website to use the bot.";
    public static final String WELCOME_BACK = "Welcome back, %s! To start a new submission, please send a photo.";
    public static final String AWAITING_PHOTO = "Please send a photo of the monument.";
    public static final String AWAITING_COORDINATES = "Please send the coordinates of the monument.";
    public static final String AWAITING_COORDINATES_CONFIRMATION = "Do you want to use the coordinates from the photo?";
    public static final String INVALID_INPUT = "Invalid input. Please try again.";
    public static final String AWAITING_MONUMENT_SELECTION = "Please select a monument from the list or propose a new one.";
    public static final String MAIN_MENU = "What would you like to do?";
}
