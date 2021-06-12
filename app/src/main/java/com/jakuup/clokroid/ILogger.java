package com.jakuup.clokroid;

import java.io.IOException;

public interface ILogger {

    void clear();

    String dump();

    void write(String tag, String msg) throws IOException;

    void kick(String tag);

}
