package org.junior.controller;

import org.junior.Account;

import java.io.*;
import java.net.Socket;

public class Client extends Thread {

    private Socket socket;
    private Account account;

    private BufferedReader reader;
    private BufferedWriter writer;

    public Client(Socket socket) {
        this.socket = socket;
        this.account = init();
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted() && !socket.isClosed())
            {
                String message = reader.readLine();
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
    public void sendMessage(String message)
    {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (Exception e)
        {
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
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String name = reader.readLine();
            String password = reader.readLine();
            return new Account(name, password, "127.0.0.1", "");

        } catch (Exception e)
        {
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
