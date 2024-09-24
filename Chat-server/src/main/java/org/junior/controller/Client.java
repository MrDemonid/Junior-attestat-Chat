package org.junior.controller;

import org.junior.Account;
import org.junior.Message;

import java.io.*;
import java.net.Socket;

/**
 * Соединение с клиентом. Работает в отдельном потоке.
 */
public class Client extends Thread {

    private Socket socket;
    private Account account;

    private ObjectInputStream reader;
    private ObjectOutputStream writer;

    public Client(Socket socket) {
        this.socket = socket;
        this.account = init();
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted() && !socket.isClosed())
            {
                Message message = (Message) reader.readObject();
                if (message == null)
                    break;                                  // на том конце разорвали связь
                ClientManager.getInstance().putMessageFromClient(message);
            }
        } catch (Exception ignored) {
        }
        ClientManager.getInstance().removeUser(this);
    }

    /**
     * Отправка сообщения клиенту
     */
    public void sendMessage(Message message)
    {
        System.out.println("Client.sendMessage(): " + message);
        try {
            writer.writeObject(message);
            writer.flush();
        } catch (Exception e)
        {
            System.out.println("Client: sendMessage() error: " + e.getMessage());
            ClientManager.getInstance().removeUser(this);
        }
    }

    public void close()
    {
        interrupt();
        try {
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();

            if (!socket.isClosed())
                socket.close();
            join();     // теперь то точно дождемся выхода из основного цикла
        } catch (InterruptedException | IOException ignored) {}
    }

    private Account init()
    {
        try {
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());
            Message msg = (Message) reader.readObject();
            if (msg.getMessage().equalsIgnoreCase("connect"))
            {
                return new Account(msg.getAuthorName(), msg.getAuthorPassword(), "", "");
            }
            throw new IOException("Unexpected body connection message!");
        } catch (Exception e)
        {
            System.out.println("Client error! " + e.getMessage());
            ClientManager.getInstance().removeUser(this);
        }
        return null;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Socket getSocket() {
        return socket;
    }
}
