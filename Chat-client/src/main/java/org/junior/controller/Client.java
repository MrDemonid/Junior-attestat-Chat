package org.junior.controller;

import org.junior.Account;
import org.junior.ConnectConfig;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Account account;
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Thread threadRead;

    public Client(Account account) {
        this.account = account;
        connect(account);
    }

    public void run()
    {
        Scanner input = new Scanner(System.in);
        try {
            while (!socket.isClosed())
            {
                String message = input.nextLine();
                sendMessage(account.getName() + ": " + message);
            }

        } catch (Exception e) {
        }
        close();
    }

    public void sendMessage(String message) throws IOException {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }


    private void connect(Account account)
    {
        try {
            socket = new Socket(InetAddress.getLocalHost(), ConnectConfig.getPort());
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sendMessage(account.getName());
            sendMessage(account.getPassword());

            threadRead = new Thread(this::readerThread);
            threadRead.start();
        } catch (IOException e) {
            System.out.println("Client: server not found! Connect refused.");
            close();
        }
    }


    private void close() {
        System.out.println("Client: close()");
        try {
            if (socket != null)
                socket.close();
            if (writer != null)
                writer.close();
            if (reader != null)
                reader.close();
        } catch(IOException e){}
    }

    private void readerThread()
    {
        try
        {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed())
            {
                String message = reader.readLine();
                if (message == null)
                    continue;
                System.out.println("Client: " + message);
            }
        } catch (Exception e) {
        }
        System.out.println("Client: close read thread!");
        close();
    }

}
