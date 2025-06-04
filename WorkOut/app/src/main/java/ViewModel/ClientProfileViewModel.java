package ViewModel; // Assuming your ViewModel package

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import Model.Client; // Your Client model
import Model.Repository.ClientRepository;

public class ClientProfileViewModel extends ViewModel {

    private final ClientRepository clientRepository;

    // LiveData for fetching client profile data
    private final MutableLiveData<GenericUiState<Client>> _profileDataState =
            new MutableLiveData<>(GenericUiState.idle());
    public LiveData<GenericUiState<Client>> profileDataState = _profileDataState;

    // LiveData for the result of updating client profile
    // Reusing the existing one, assuming T is String for a success/error message
    private final MutableLiveData<GenericUiState<String>> _updateProfileState =
            new MutableLiveData<>(GenericUiState.idle());
    public LiveData<GenericUiState<String>> updateProfileState = _updateProfileState;


    public ClientProfileViewModel(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Fetches the client profile data.
     * @param clientUsername The ID of the client to fetch.
     */
    public void fetchClientProfile(String clientUsername) {
        _profileDataState.postValue(GenericUiState.loading());

        clientRepository.getClientByUsername(clientUsername) // Assuming this method exists
                .addOnSuccessListener(client -> {
                    if (client != null) {
                        _profileDataState.postValue(GenericUiState.success(client));
                    } else {
                        _profileDataState.postValue(GenericUiState.error("Client not found."));
                    }
                })
                .addOnFailureListener(e -> {
                    _profileDataState.postValue(GenericUiState.error("Failed to fetch profile: " + e.getMessage()));
                });
    }

    /**
     * Updates the client's profile information.
     * @param client The Client object with updated information.
     */
    public void updateClientProfile(Client client) { // Renamed from updateClient for clarity
        _updateProfileState.postValue(GenericUiState.loading());

        // Validate client object if necessary before sending to repository
        if (client == null || client.getId() == null || client.getId().isEmpty()) {
            _updateProfileState.postValue(GenericUiState.error("Invalid client data for update."));
            return;
        }

        clientRepository.updateClientByID(client) // Your existing method
                .addOnSuccessListener(aVoid -> { // Assuming success returns void or some simple confirmation
                    _updateProfileState.postValue(GenericUiState.success("Profile updated successfully."));
                })
                .addOnFailureListener(e -> {
                    _updateProfileState.postValue(GenericUiState.error("Failed to update profile: " + e.getMessage()));
                });
    }

    // Getter for the profile data state
    public LiveData<GenericUiState<Client>> getProfileDataState() {
        return profileDataState;
    }

    // Getter for the update profile operation state
    public LiveData<GenericUiState<String>> getUpdateProfileState() {
        return updateProfileState;
    }

    /**
     * Resets the update profile state, e.g., after the message has been shown to the user.
     */
    public void resetUpdateState() {
        _updateProfileState.postValue(GenericUiState.idle());
    }
}