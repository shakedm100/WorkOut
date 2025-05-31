// GenericUiState.java
// Can be placed in a common 'ui' or 'util' package, or a base 'viewmodel' package
package ViewModel; // Or your preferred package

// com.example.workout.ui.common
public class GenericUiState<T>
{

    public enum Status
    {
        IDLE,    // Initial state or after a task is reset
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final T data; // Data associated with SUCCESS state
    private final String errorMessage; // Message for ERROR state
    private GenericUiState(Status status, T data, String errorMessage)
    {
        this.status = status;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public Status getStatus()
    {
        return status;
    }

    public T getData()
    {
        // Only return data if status is SUCCESS, otherwise, it might be stale or irrelevant
        if (status == Status.SUCCESS)
        {
            return data;
        }
        return null; // Or throw an exception if accessing data in a non-success state is an error
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public boolean isLoading()
    {
        return status == Status.LOADING;
    }

    public boolean isSuccess()
    {
        return status == Status.SUCCESS;
    }

    public boolean isError()
    {
        return status == Status.ERROR;
    }

    // Static factory methods
    public static <T> GenericUiState<T> idle()
    {
        return new GenericUiState<>(Status.IDLE, null, null);
    }

    public static <T> GenericUiState<T> loading()
    {
        return new GenericUiState<>(Status.LOADING, null, null);
    }

    public static <T> GenericUiState<T> loading(String message)
    {
        return new GenericUiState<>(Status.LOADING, null, message);
    }

    public static <T> GenericUiState<T> success(T data)
    {
        return new GenericUiState<>(Status.SUCCESS, data, null);
    }

    public static <T> GenericUiState<T> error(String errorMessage)
    {
        // For error, the data type T is often irrelevant, so null is passed.
        return new GenericUiState<>(Status.ERROR, null, errorMessage);
    }
}