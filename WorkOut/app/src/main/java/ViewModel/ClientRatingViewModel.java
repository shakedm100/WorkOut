package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import Model.Business;
import Model.Client;
import Model.Rating;
import Model.Repository.BusinessRepository;
import Model.Repository.ClientRepository;

public class ClientRatingViewModel extends ViewModel
{
    private final ClientRepository clientRepository;
    private final BusinessRepository businessRepository;
    private final MutableLiveData<GenericUiState<String>> ratingsUiState = new MutableLiveData<>(GenericUiState.idle());
    private TaskCompletionSource<Boolean> taskCompletionSource;

    /**
     * Default constructor for the ClientRatingViewModel.
     */
    public ClientRatingViewModel()
    {
        this.clientRepository = new ClientRepository();
        this.businessRepository = new BusinessRepository();
    }

    /**
     * Returns the state of the ratings UI.
     */
    public LiveData<GenericUiState<String>> getRatingsUiState()
    {
        return ratingsUiState;
    }

    public boolean basicCheck(float stars, String comment, Client client, Business business)
    {
        // check validation
        if (stars < 0.5)
        {
            ratingsUiState.postValue(GenericUiState.error("Rating must be at least 0.5 stars."));
            return false;
        }

        if (comment.trim().isEmpty())
        {
            ratingsUiState.postValue(GenericUiState.error("Please enter a comment."));
            return false;
        }

        // TODO: check if the client was registered to this business course?
        if (client == null)
        {
            ratingsUiState.postValue(GenericUiState.error("Please enter a comment."));
            return false;
        }

        if (business == null)
        {
            ratingsUiState.postValue(GenericUiState.error("Please enter a comment."));
            return false;
        }

        return true;
    }
    public Task<Boolean> addRating(float stars, String comment, Client client, Business business)
    {
        taskCompletionSource = new TaskCompletionSource<>();

        if (!basicCheck(stars, comment, client, business))
            taskCompletionSource.setResult(false);

        else
        {
            Rating rating = new Rating(stars, comment, client);
            businessRepository.addRatingToBusiness(business, rating)
                    .addOnSuccessListener(task ->
                    {
                        ratingsUiState.postValue(GenericUiState.success("Rating success!"));
                        taskCompletionSource.setResult(true);
                    })
                    .addOnFailureListener(insertException ->
                    {
                        ratingsUiState.postValue(GenericUiState.error("Rating failed: " + insertException.getMessage()));
                        taskCompletionSource.setResult(false);
                    });
        }

        return taskCompletionSource.getTask();
    }

    public Task<Boolean> updateRating(Business business, Rating rating)
    {
        taskCompletionSource = new TaskCompletionSource<>();

        if(business == null || rating == null)
        {
            ratingsUiState.postValue(GenericUiState.error("Error receiving business or rating"));
            taskCompletionSource.setResult(false);
        }

        else
        {
            businessRepository.updateRatingFromBusiness(business, rating)
                    .addOnSuccessListener(task ->
                    {
                        ratingsUiState.postValue(GenericUiState.success("Rating update success!"));
                        taskCompletionSource.setResult(true);
                    })
                    .addOnFailureListener(insertException ->
                    {
                        ratingsUiState.postValue(GenericUiState.error("Rating update failed: " + insertException.getMessage()));
                        taskCompletionSource.setResult(false);
                    });
        }

        return taskCompletionSource.getTask();
    }

    public Task<Rating> checkIfRatingExists(Business business, Client client)
    {
        if(business == null || client == null)
        {
            ratingsUiState.postValue(GenericUiState.error("Error receiving business or client"));
            return null;
        }

        return businessRepository.checkIfRatingExists(business, client).addOnSuccessListener(task ->
        {
            ratingsUiState.postValue(GenericUiState.idle());
        }).addOnFailureListener(insertException ->
        {
            ratingsUiState.postValue(GenericUiState.error("Checking for existing rating failed: " + insertException.getMessage()));
        });
    }
}
