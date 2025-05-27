package ViewModel;

public class CourseUiState {

    public enum Status {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR
    }

    private final CourseUiState.Status status;
    private final String data;
    private final String errorMessage;

    private CourseUiState(CourseUiState.Status status, String data, String errorMessage) {
        this.status = status;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public CourseUiState.Status getStatus() {
        return status;
    }

    public String getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static CourseUiState idle() {
        return new CourseUiState(CourseUiState.Status.IDLE, null, null);
    }

    public static CourseUiState loading() {
        return new CourseUiState(CourseUiState.Status.LOADING, null, null);
    }

    public static CourseUiState success(String data) {
        return new CourseUiState(CourseUiState.Status.SUCCESS, data, null);
    }

    public static CourseUiState error(String errorMessage) {
        return new CourseUiState(CourseUiState.Status.ERROR, null, errorMessage);
    }
}