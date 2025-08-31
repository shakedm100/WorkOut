package ViewModel.Business;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;

import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.Course;
import Model.CourseType;
import Model.Repository.CourseRepository;
import Model.Schedule;
import ViewModel.GenericUiState;

public class AddOrEditClassViewModel  extends ViewModel
{
    // LiveData for fetching client profile data
    private final MutableLiveData<GenericUiState<String>> addOrEditClassUiState;
    private final CourseRepository courseRepository = new CourseRepository();

    private TaskCompletionSource<Boolean> taskCompletionSource;

    public AddOrEditClassViewModel()
    {
        // Initialize the LiveData here if needed
        addOrEditClassUiState = new MutableLiveData<>(GenericUiState.idle());
    }

    /**
     * This function checks if all the given arguments are valid.
     * @param business the business of the class
     * @param name the name of the class
     * @param schedule the occurrence of the class
     * @param capacity the amount of participants in the class
     * @param type the type of the class
     * @param ageRange the possible ages of the participants
     * @param category the category of the class
     * @param description the description of the class
     * @param startTime the time that the class starts
     * @param endTime the time that the class ends
     * @return true if all the arguments are valid, false otherwise.
     */
    private boolean basicCheck(Business business, String name, Schedule schedule, String capacity,
                               CourseType type, AgeRange ageRange, Category category, String description,
                               LocalTime startTime, LocalTime endTime)
    {
        // check if all the given arguments are valid
        if (business == null) {
            addOrEditClassUiState.postValue(GenericUiState.error("Business can not be null!"));
            return false;
        }

        if (name == null || name.trim().isEmpty()) {
            addOrEditClassUiState.postValue(GenericUiState.error("Please enter a name for the class."));
            return false;
        }

        if (schedule == null)
        {
            addOrEditClassUiState.postValue(GenericUiState.error("Schedule can not be null!"));
            return false;
        }

        if (startTime == null || endTime == null)
        {
            addOrEditClassUiState.postValue(GenericUiState.error("Please pick a start time and and end time."));
            return false;
        }

        int duration = (int)Duration.between(startTime, endTime).toMinutes();
        if (duration <= 0)
        {
            addOrEditClassUiState.postValue(GenericUiState.error("Duration must be greater than 0."));
            return false;
        }

        if (capacity.isEmpty() || !TextUtils.isDigitsOnly(capacity))
        {
            addOrEditClassUiState.postValue(GenericUiState.error("Capacity must contain numbers only and can not be empty!"));
            return false;
        }


        if (Integer.parseInt(capacity) <= 0) {
            addOrEditClassUiState.postValue(GenericUiState.error("Capacity must be greater than 0."));
            return false;
        }

        if (type == null) {
            addOrEditClassUiState.postValue(GenericUiState.error("Course type can not be null!"));
            return false;
        }

        if (ageRange == null) {
            addOrEditClassUiState.postValue(GenericUiState.error("Age range can not be null!"));
            return false;
        }

        if (category == null) {
            addOrEditClassUiState.postValue(GenericUiState.error("Category can not be null!"));
            return false;
        }

        if (description == null || description.trim().isEmpty()) {
            addOrEditClassUiState.postValue(GenericUiState.error("Please enter a description for the class."));
            return false;
        }

        return true;
    }

    /**
     * Inserts a course into the database if all the given arguments are valid.
     * @param business the business of the class
     * @param name the name of the class
     * @param schedule the occurrence of the class
     * @param capacity the amount of participants in the class
     * @param type the type of the class
     * @param ageRange the possible ages of the participants
     * @param category the category of the class
     * @param description the description of the class
     * @param startTime the time that the class starts
     * @param endTime the time that the class ends
     * @return an asynchronous task that returns true if the insertion was successful, false otherwise
     */
    public Task<Boolean> insertCourse(Business business, String name, Schedule schedule, String capacity,
                                      CourseType type, AgeRange ageRange, Category category, String description,
                                      LocalTime startTime, LocalTime endTime) {

        taskCompletionSource = new TaskCompletionSource<>();

        // check if all the arguments are valid
        if (!basicCheck(business, name, schedule, capacity, type, ageRange, category, description, startTime, endTime))
            taskCompletionSource.setResult(false); // at least one argument is not valid -> can not insert

        else // all the arguments are valid -> try to insert
        {
            // parse some arguments
            int parsedCapacity = Integer.parseInt(capacity);
            int duration = (int) Duration.between(startTime, endTime).toMinutes();

            courseRepository.insertCourse(business, name, schedule,
                            parsedCapacity, type, ageRange, category, description, duration)
                    .addOnSuccessListener(result -> {
                        addOrEditClassUiState.postValue(GenericUiState.success("Class inserted successfully!"));
                        taskCompletionSource.setResult(true);
                    })
                    .addOnFailureListener(insertException -> {
                        addOrEditClassUiState.postValue(GenericUiState.error("Class insertion failed: " + insertException.getMessage()));
                        taskCompletionSource.setResult(false);
                    });
        }

        return taskCompletionSource.getTask();
    }

    /**
     * Updates a course into the database if all the given arguments are valid.
     * @param business the business of the class
     * @param name the name of the class
     * @param schedule the occurrence of the class
     * @param capacity the amount of participants in the class
     * @param type the type of the class
     * @param ageRange the possible ages of the participants
     * @param category the category of the class
     * @param description the description of the class
     * @param startTime the time that the class starts
     * @param endTime the time that the class ends
     * @return an asynchronous task that returns true if the update was successful, false otherwise
     */
    public Task<Boolean> updateCourse(Business business, String name, Schedule schedule, String capacity,
                                      CourseType type, AgeRange ageRange, Category category, String description,
                                      LocalTime startTime, LocalTime endTime, Course currentCourse)
    {
        taskCompletionSource = new TaskCompletionSource<>();

        // check if all the arguments are valid
        if (!basicCheck(business, name, schedule, capacity, type, ageRange, category, description, startTime, endTime))
            taskCompletionSource.setResult(false); // one argument is not valid -> can not update

        // all the arguments are valid -> try to update
        else
        {
            // parse some arguments
            int parsedCapacity = Integer.parseInt(capacity);
            int duration = (int)Duration.between(startTime, endTime).toMinutes();

            boolean isChanged = false; // flag to check if any changes were made
            if (type != currentCourse.getType())
            {
                currentCourse.setType(type);
                isChanged = true;
            }

            if (!name.equals(currentCourse.getName()) && !name.isEmpty())
            {
                currentCourse.setName(name);
                isChanged = true;
            }

            if (parsedCapacity != currentCourse.getCapacity())
            {
                currentCourse.setCapacity(parsedCapacity);
                isChanged = true;
            }

            if (!schedule.equals(currentCourse.getSchedule()))
            {
                currentCourse.setSchedule(schedule);
                isChanged = true;
            }
            if (!ageRange.equals(currentCourse.getAgeRange()))
            {
                isChanged = true;
                currentCourse.setAgeRange(ageRange);
            }

            if (category != currentCourse.getCategory())
            {
                currentCourse.setCategory(category);
                isChanged = true;
            }

            if (!description.equals(currentCourse.getDescription()))
            {
                currentCourse.setDescription(description);
                isChanged = true;
            }

            if (duration != currentCourse.getDuration())
            {
                currentCourse.setDuration(duration);
                isChanged = true;
            }

            // if no changes occurred -> don't update
            if (!isChanged)
            {
                addOrEditClassUiState.postValue(GenericUiState.error("No changes were made."));
                taskCompletionSource.setResult(false);
            }

            // changes occurred -> update
            else
            {
                courseRepository.updateCourse(currentCourse, business).addOnSuccessListener(result ->
                        {
                            addOrEditClassUiState.postValue(GenericUiState.success("Class updated successfully!"));
                            taskCompletionSource.setResult(true);
                        })
                        .addOnFailureListener(insertException ->
                        {
                            addOrEditClassUiState.postValue(GenericUiState.error("Class update failed: " + insertException.getMessage()));
                            taskCompletionSource.setResult(false);
                        });
            }
        }

        return taskCompletionSource.getTask();
    }

    /**
     * Deletes a course from the database if all the given arguments are valid.
     * @param course
     * @param business
     * @return
     */
    public Task<Boolean> deleteCourse(Course course, Business business)
    {
        taskCompletionSource = new TaskCompletionSource<>();

        // if one of them is null -> can not delete
        if (course == null || business == null)
        {
            addOrEditClassUiState.postValue(GenericUiState.error("Course or business can not be null!"));
            taskCompletionSource.setResult(false);
        }

        // all the arguments are valid -> try to delete
        else
        {
            courseRepository.deleteCourse(course, business)
                    .addOnSuccessListener(result -> {
                        addOrEditClassUiState.postValue(GenericUiState.success("Class deleted successfully!"));
                        taskCompletionSource.setResult(true);
                    })
                    .addOnFailureListener(insertException ->
                    {
                        addOrEditClassUiState.postValue(GenericUiState.error("Class delete failed: " + insertException.getMessage()));
                        taskCompletionSource.setResult(false);
                    });
        }

        return taskCompletionSource.getTask();
    }

    /**
     * Get the status of the current UI state.
     * @return the current state of the UI.
     */
    public LiveData<GenericUiState<String>> getAddOrEditUiState()
    {
        return addOrEditClassUiState;
    }
}