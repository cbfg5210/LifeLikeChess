package com.ue.resource.db;

/**
 * Created by hawk on 2017/1/4.
 */

public interface DataParser<T> {
    T parse(long id, String data);
}
