package org.junior.controller;

import org.junior.Account;
import org.junior.ConnectConfig;
import org.junior.ConnectStatus;
import org.junior.Message;
import org.junior.view.View;
import org.junior.view.listeners.DisconnectListener;
import org.junior.view.listeners.LoginListener;
import org.junior.view.listeners.SendMessageListener;

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


    public Client(Account account, View view) {
        this.account = account;
        connectStatus = ConnectStatus.DISCONNECTED;
        view.setAccount(account);
        setListeners();

        connect(account);
    }

    /**
     * Регистрация слушателей от View
     */
    private void setListeners()
    {
        view.addListener(LoginListener.class, event -> connectToServer());
//        view.addListener(SendMessageListener.class, event -> sendMessage(event.getMessage()));
        view.addListener(DisconnectListener.class, event -> disconnectFromUser());
    }

    /**
     * Коннектимся к серверу
     */
    private void connectToServer()
    {
    }

    /**
     * Пользователь сам прерывает сеанс
     */
    private void disconnectFromUser()
    {
    }

    public void run()
    {
        Scanner input = new Scanner(System.in);
        try {
            while (!socket.isClosed())
            {
                String message = input.nextLine();
                sendMessage(message);
            }

        } catch (Exception e) {
        }
        close();
    }

    public void sendMessage(String message) throws IOException {
        if (message != null && !message.isEmpty())
        {
            String to = null;
            if (message.charAt(0) == '@')
            {
                // извлекаем имя адресата
                to = getTargetName(message);
                message = getBodyMessage(to, message);
                System.out.println("Personal. to: " + to + ", message: '" + message + "'");
            }
            Message msg = new Message(account, to, message);
            System.out.println("Send: " + msg);
            writer.writeObject(msg);
            writer.flush();
        }
    }


    private void connect(Account account)
    {
        try {
            socket = new Socket(InetAddress.getLocalHost(), ConnectConfig.getPort());
            writer = new ObjectOutputStream(socket.getOutputStream());
            reader = new ObjectInputStream(socket.getInputStream());
            sendMessage("connect");
            threadRead = new Thread(this::readerThread);
            threadRead.start();
        } catch (IOException e) {
            System.out.println("Client: server not found! Connect refused.");
            close();
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
            System.out.println("close(): " + e.getMessage());
        }
        System.out.println("Client: close()");
    }

    private void readerThread()
    {
        try
        {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed())
            {
                Message message = (Message) reader.readObject();
                if (message == null)
                    continue;
                System.out.println("from server: " + message);
            }
        } catch (Exception e) {
        }
        System.out.println("Client: close read thread!");
        close();
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
