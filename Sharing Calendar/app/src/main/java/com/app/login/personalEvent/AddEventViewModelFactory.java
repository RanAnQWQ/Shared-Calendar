package com.app.login.personalEvent;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.app.login.dao.EventDAO;

public class AddEventViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final EventDAO dataSource;

    public AddEventViewModelFactory(Application application, EventDAO dataSource) {
        this.application = application;
        this.dataSource = dataSource;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AddEventViewModel.class)) {
                return (T) new AddEventViewModel(application, dataSource);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
