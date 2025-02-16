package org.example;

public interface IObservable {
    void broadcast(String message);
    void addObserver(IObserver observer);
    void removeObserver(IObserver observer);
}
