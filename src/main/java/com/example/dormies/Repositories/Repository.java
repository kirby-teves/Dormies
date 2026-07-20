package com.example.dormies.Repositories;

import java.util.List;

public interface Repository<T> {
    List<T> getAll();
    void add(T item);
    void delete(String id);
}