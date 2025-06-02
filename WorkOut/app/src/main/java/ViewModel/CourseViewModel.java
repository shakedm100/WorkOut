package ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.CourseType;
import Model.Repository.CourseRepository;
import Model.Schedule;

// TODO: change to the correct repository and add the desired functions
public class CourseViewModel extends ViewModel {

    private final CourseRepository courseRepository;

    private final MutableLiveData<GenericUiState<String>> _courseUiState = new MutableLiveData<>(GenericUiState.idle());
    public LiveData<GenericUiState<String>> courseUiState = _courseUiState;

    public CourseViewModel() {
        // It's better to inject the repository via constructor
        this.courseRepository = new CourseRepository();
    }

    // Constructor for Dependency Injection
    public CourseViewModel(CourseRepository clientRepository) {
        this.courseRepository = clientRepository;
    }

    public void insertCourse(@NonNull Business business, String name, Schedule schedule, int capacity,
                             CourseType type, AgeRange ageRange, Category category, String description, int duration) {
        _courseUiState.postValue(GenericUiState.loading("Inserting course..."));

        courseRepository.insertCourse(business, name, schedule, capacity, type, ageRange, category, description, duration)
                .addOnSuccessListener(client -> {
                    _courseUiState.postValue(GenericUiState.success("Course inserted!"));

                })
                .addOnFailureListener(e -> {
                    _courseUiState.postValue(GenericUiState.error(e.getMessage()));

                });
    }

//    public LiveData<GenericUiState> getLoginUiState() {
//        return loginUiState;
//    }

}
