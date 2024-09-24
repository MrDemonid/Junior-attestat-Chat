package org.junior.controller;

import org.junior.ConnectConfig;
import org.junior.Message;
import org.junior.view.View;
import org.junior.view.listeners.StartServerEvent;
import org.junior.view.listeners.StartServerListener;
import org.junior.view.listeners.StopServerEvent;
import org.junior.view.listeners.StopServerListener;

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

        // регистрируем слушателей от GUI
        view.addListener(StartServerListener.class, startServerListener);
        view.addListener(StopServerListener.class, stopServerListener);
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
                Message message = ClientManager.getInstance().getMessageFromClient();

                if (message.isPrivate())
                {
                    ClientManager.getInstance().resendPrivateMessage(message);
                    view.showMessage(message.getAuthorName() + " to @" + message.getTargetName() + ": " + message.getMessage());
                } else {
                    ClientManager.getInstance().broadcastMessage(message);
                    view.showMessage(message.getAuthorName() + ": " + message.getMessage());
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Server: thread reader stopped.");
        }
    }

        /*===========================================================================
     *
     * Реализация слушателей от контролов View
     *
     ===========================================================================*/

    private final StartServerListener startServerListener = new StartServerListener() {
        @Override
        public void actionPerformed(StartServerEvent event)
        {
            if (isWorked)
            {
                view.showMessage("Warning: сервер уже запущен.");
            } else {
                view.showMessage("Info: сервер запущен.");
                startServer();
                isWorked = true;
            }
        }
    };

    private final StopServerListener stopServerListener = new StopServerListener() {
        @Override
        public void actionPerformed(StopServerEvent event)
        {
            if (!isWorked)
            {
                view.showMessage("Warning: сервер уже остановлен.");
            } else {
                view.showMessage("Info: сервер остановлен.");
                stopServer();
                isWorked = false;
            }
        }
    };

}
