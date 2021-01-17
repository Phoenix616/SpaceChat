package dev.spaceseries.spacechat.manager;

import java.util.*;

public interface IManager<K, V> {

    Map<K, V> getAll();

    V get(K k);

    V get(K k, V def);

    void add(K k, V v);

}