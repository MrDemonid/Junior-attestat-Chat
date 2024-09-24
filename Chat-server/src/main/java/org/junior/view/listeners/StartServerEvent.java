package org.junior.view.listeners;

import java.util.EventObject;

public class StartServerEvent extends EventObject {

    public StartServerEvent(Object source)
    {
        super(source);
    }

    @Override
    public String toString() {
        return "StartServerEvent{}";
    }
}
