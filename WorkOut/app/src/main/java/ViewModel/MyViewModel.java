package ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

public class MyViewModel extends ViewModel{

    private final MutableLiveData<String> myText = new MutableLiveData<>();

    // this allows modification via the UI
    public MutableLiveData<String> getMyText() {
        return myText;
    }

//    public void setMyText(String text) {
//        myText.setValue(text);
//    }

    // this is a read only which will be represented in the UI
    public LiveData<String> getTextData() {
        return myText;
    }

    public void updateText(String text) {
        myText.setValue(text);
    }
}
