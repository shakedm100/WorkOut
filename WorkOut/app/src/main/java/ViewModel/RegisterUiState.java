package ViewModel; // Or a common ui.state package

public class RegisterUiState {

    public enum Status {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final String successMessage; // Or a User object if registration returns it
    private final String errorMessage;

    private RegisterUiState(Status status, String successMessage, String errorMessage) {
        this.status = status;
        this.successMessage = successMessage;
        this.errorMessage = errorMessage;
    }

    public Status getStatus() { return status; }
    public String getSuccessMessage() { return successMessage; }
    public String getErrorMessage() { return errorMessage; }

    public static RegisterUiState idle() {
        return new RegisterUiState(Status.IDLE, null, null);
    }
    public static RegisterUiState loading() {
        return new RegisterUiState(Status.LOADING, null, null);
    }
    public static RegisterUiState success(String message) {
        return new RegisterUiState(Status.SUCCESS, message, null);
    }
    public static RegisterUiState error(String errorMessage) {
        return new RegisterUiState(Status.ERROR, null, errorMessage);
    }
}