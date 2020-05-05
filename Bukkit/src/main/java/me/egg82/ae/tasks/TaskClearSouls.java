package me.egg82.ae.tasks;

import me.egg82.ae.services.CollectionProvider;

public class TaskClearSouls implements Runnable {
    public void run() {
        CollectionProvider.getSouls().clear();
    }
}
