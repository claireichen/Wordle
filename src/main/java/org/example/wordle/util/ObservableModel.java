package org.example.wordle.util;

import java.util.ArrayList;
import java.util.List;


public abstract class ObservableModel {
    private final List<ModelListener> listeners = new ArrayList<>();


    public void addListener(ModelListener listener) { listeners.add(listener); }
    public void removeListener(ModelListener listener) { listeners.remove(listener); }


    protected void notifyListeners() {
        for (ModelListener l : listeners) l.onModelChanged();
    }
}
