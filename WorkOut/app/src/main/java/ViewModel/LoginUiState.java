package ViewModel;

// instead of LiveDate
// better encapsulation
public class LoginUiState {
    public enum Status {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final String data; // Could be a user token on success
    private final String errorMessage;

    private LoginUiState(Status status, String data, String errorMessage) {
        this.status = status;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public Status getStatus() {
        return status;
    }

    public String getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static LoginUiState idle() {
        return new LoginUiState(Status.IDLE, null, null);
    }

    public static LoginUiState loading() {
        return new LoginUiState(Status.LOADING, null, null);
    }

    public static LoginUiState success(String data) {
        return new LoginUiState(Status.SUCCESS, data, null);
    }

    public static LoginUiState error(String errorMessage) {
        return new LoginUiState(Status.ERROR, null, errorMessage);
    }
}
