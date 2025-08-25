package ViewModel;

import androidx.lifecycle.ViewModelProvider;

public final class ViewModelFactoryProvider
{
    private ViewModelFactoryProvider()
    {
    }

    public static ViewModelProvider.Factory factory = new ViewModelProvider.NewInstanceFactory();
}