package org.junior.controller;

import org.junior.Account;
import org.junior.ConnectConfig;
import org.junior.ConnectStatus;
import org.junior.Message;
import org.junior.view.View;
import org.junior.view.listeners.DisconnectListener;
import org.junior.view.listeners.LoginListener;
import org.junior.view.listeners.SendMessageListener;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Account account;
    private Socket socket;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;
    private Thread threadRead;

    View view;
    ConnectStatus connectStatus;


    public Client(View view) {
        this.account = new Account("Anon", "12345", "localhost", "4310");
        this.view = view;
        connectStatus = ConnectStatus.DISCONNECTED;
        view.setAccount(account);
        setListeners();

//        connect(account);
    }

    /**
     * Регистрация слушателей от View
     */
    private void setListeners()
    {
        view.addListener(LoginListener.class, event -> connectToServer());
        view.addListener(SendMessageListener.class, event -> sendMessage(event.getMessage()));
        view.addListener(DisconnectListener.class, event -> disconnectFromUser());
    }

    private void removeListeners()
    {
        view.removeListeners(LoginListener.class, event -> connectToServer());
        view.removeListeners(DisconnectListener.class, event -> disconnectFromUser());
    }

    /**
     * Коннектимся к серверу
     */
    private void connectToServer()
    {
        if (connectStatus == ConnectStatus.DISCONNECTED)
        {
            view.showMessage("Connect...");
        }
        account = view.getAccount();
        threadRead = new Thread(this::readerThread);
        threadRead.start();
    }

    /**
     * Пользователь сам прерывает сеанс
     */
    private void disconnectFromUser()
    {
        close();
        view.setConnectStatus(connectStatus);
        System.out.println("Disconnect");
    }

//    public void run()
//    {
//        Scanner input = new Scanner(System.in);
//        try {
//            while (!socket.isClosed())
//            {
//                String message = input.nextLine();
//                sendMessage(message);
//            }
//
//        } catch (Exception e) {
//        }
//        close();
//    }

    public void sendMessage(String message) {
        if (message != null && !message.isEmpty())
        {
            String to = null;
            if (message.charAt(0) == '@')
            {
                // извлекаем имя адресата
                to = getTargetName(message);
                message = getBodyMessage(to, message);
            }
            Message msg = new Message(account, to, message);
            try {
                writer.writeObject(msg);
                writer.flush();
            } catch (Exception e)
            {
                close();
            }
        }
    }

    private void close() {
        try {
            if (socket != null)
                socket.close();
            if (writer != null)
                writer.close();
            if (reader != null)
                reader.close();
        } catch(IOException e){
            System.out.println("close() error: " + e.getMessage());
        }
        System.out.println("Client: close()");
    }

    private void readerThread()
    {
        if (!connect(account))
        {
            switchConnectedStatus(ConnectStatus.DISCONNECTED, "Connect error: server not found!");
            close();
            return;
        }
        switchConnectedStatus(ConnectStatus.CONNECTED, "Connected!");
        try
        {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed())
            {
                Message message = (Message) reader.readObject();
                if (message == null)
                    break;
                SwingUtilities.invokeLater(() -> {
                    view.showMessage(message.getAuthorName() + ": " + message.getMessage());
                });
            }
        } catch (Exception e) {
        }
        switchConnectedStatus(ConnectStatus.DISCONNECTED, "Connect closed.");
        close();
    }

    /**
     * Переключение статуса клиента. Версия для отдельного от Swing потока.
     * @param message Сообщение пользователю.
     */
    private void switchConnectedStatus(ConnectStatus newStatus, String message)
    {
        connectStatus = newStatus;
        SwingUtilities.invokeLater(() -> {
            view.setConnectStatus(connectStatus);
            view.showMessage(message);
        });

    }

    private boolean connect(Account account)
    {
        try
        {
            int port = Integer.parseInt(account.getPort());
            socket = new Socket(InetAddress.getLocalHost(), port);
            writer = new ObjectOutputStream(socket.getOutputStream());
            reader = new ObjectInputStream(socket.getInputStream());
            sendMessage("connect");
        } catch (Exception e)
        {
            return false;
        }
        return true;
    }

    private String getTargetName(String message)
    {
        StringBuilder name = new StringBuilder();
        for (int i = 1; i < message.length(); i++)
        {
            char ch = message.charAt(i);
            if (Character.isWhitespace(ch))
                return name.toString();
            name.append(ch);
        }
        return name.toString();
    }

    private String getBodyMessage(String name, String source)
    {
        return source.replaceFirst("@" + name, "").trim();
    }
}
