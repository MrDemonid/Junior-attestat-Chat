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
                if (message == null || message.getMessage().isEmpty())
                    break;                                  // на том конце разорвали связь
                ClientManager.getInstance().putMessageFromClient(message);
            }
        } catch (Exception ignored) {
            System.out.println("Client.run(): " + ignored.getMessage());
        }
        System.out.println("Client.run() done!");
        ClientManager.getInstance().unregisterUser(this);
        closeResource();
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
            ClientManager.getInstance().unregisterUser(this);
            closeResource();
        }
    }



    public void closeResource()
    {
        try {
            if (socket != null && !socket.isClosed())
                socket.close();
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
        } catch (IOException ignored) {}
        System.out.println("Client.closeResource()");
    }

    public void close()
    {
        interrupt();
        closeResource();
        System.out.println("Client.close()");
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
            ClientManager.getInstance().unregisterUser(this);
            closeResource();
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
