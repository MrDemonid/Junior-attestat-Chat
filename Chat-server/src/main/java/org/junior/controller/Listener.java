package org.junior.controller;

import org.junior.Account;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Слушатель подключений пользователей
 */
public class Listener extends Thread {

    private ServerSocket listener;

    public Listener(int port) {
        try {
            listener = new ServerSocket(port);
        } catch (Exception e)
        {
            close();
        }
    }

    @Override
    public void run() {
        while (!isInterrupted() && !listener.isClosed())
        {
            try {
                Socket socket = listener.accept();
                ClientManager.getInstance().addUser(socket);    // подключаем нового пользователя
            } catch (IOException e) {
                break;
            }
        }
        closeSocket();
    }

    public void close()
    {
        try {
            interrupt();
            closeSocket();
            if (this.isAlive())
                join();
        } catch (Exception ignored) {
            System.out.println("Listener.close(): " + ignored.getMessage());
        }
        System.out.println("Listener.close()");
    }

    private void closeSocket()
    {
        try {
            if (listener != null)
                listener.close();
        } catch (IOException ignored) {}
        listener = null;
    }
}
