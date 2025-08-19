package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;

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

    public Task<Business> addRating(float stars, String comment, Client client, Business business)
    {
        // check validation
        if (stars < 0.5)
        {
            ratingsUiState.postValue(GenericUiState.error("Rating must be at least 0.5 stars."));
            return null;
        }

        if (comment.trim().isEmpty())
        {
            ratingsUiState.postValue(GenericUiState.error("Please enter a comment."));
            return null;
        }

        Rating rating = new Rating(stars, comment, client);

        return businessRepository.addRatingToBusiness(business, rating)
                .addOnSuccessListener(task ->
                {
                    ratingsUiState.postValue(GenericUiState.success("Rating success!"));
                })
                .addOnFailureListener(insertException ->
                {
                    ratingsUiState.postValue(GenericUiState.error("Rating failed: " + insertException.getMessage()));
                });
    }

    public Task<Business> updateRating(Business business, Rating rating)
    {
        if(business == null || rating == null)
        {
            ratingsUiState.postValue(GenericUiState.error("Error receiving business or rating"));
            return null;
        }

        return businessRepository.updateRatingFromBusiness(business, rating).addOnSuccessListener(task ->
        {
            ratingsUiState.postValue(GenericUiState.success("Rating update success!"));
        }).addOnFailureListener(insertException ->
        {
            ratingsUiState.postValue(GenericUiState.error("Rating update failed: " + insertException.getMessage()));
        });
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
            if(task != null)
                ratingsUiState.postValue(GenericUiState.idle());
            else
                ratingsUiState.postValue(GenericUiState.idle());
        }).addOnFailureListener(insertException ->
        {
            ratingsUiState.postValue(GenericUiState.error("Checking for existing rating failed: " + insertException.getMessage()));
        });
    }
}
