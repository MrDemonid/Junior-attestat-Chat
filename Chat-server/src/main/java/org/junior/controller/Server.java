package org.junior.controller;

import org.junior.ConnectConfig;
import org.junior.view.View;

import java.util.ArrayList;
import java.util.List;

public class Server {

    private View view;
    private List<Client> users;

    private boolean isWorked;

    private Listener connectListener;
    private Thread threadRead;

    public Server(View view) {
        this.view = view;
        users = new ArrayList<>();
        isWorked = false;
    }

    public void run()
    {
        startServer();
        while (true)
        {

        }
//        stopServer();
    }

    /**
     * Начало работы сервера
     */
    private void startServer()
    {
        if (!isWorked)
        {
            connectListener = new Listener(ConnectConfig.getPort());
            connectListener.start();
            isWorked = true;
            ClientManager.getInstance().removeAllUsers();
            // запускаем отдельный поток чтения и обработки сообщений от пользователей
            threadRead = new Thread(this::readThread);
            threadRead.start();
        }
    }

    private void stopServer()
    {
        if (isWorked)
        {
            threadRead.interrupt();
            connectListener.close();
            ClientManager.getInstance().removeAllUsers();
            try {
                threadRead.join();
            } catch (InterruptedException ignored) {}
            isWorked = false;
        }
    }

    /**
     * Поток чтения сообщений от пользователей
     */
    private void readThread()
    {
        try {
            while (!Thread.currentThread().isInterrupted())
            {
                String message = ClientManager.getInstance().getMessageFromClient();
                System.out.println("Server: get message: " + message);
                ClientManager.getInstance().broadcastMessage("[Server to] " + message);
            }

        } catch (InterruptedException e) {
        }

    }

}
