package org.junior.controller;

import org.junior.Account;
import org.junior.Message;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Менеджер подключений клиентов.
 * Для него скорость не критична, поэтому в целях потокобезопасности
 * применяется простой synchronized(clients).
 * Хотя это скорее подстраховка на случай ввода слушателей для
 * нескольких портов.
 * (кроме удаления, которое могут запросить несколько пользователей сразу).
 */
public class ClientManager {

    private static final ClientManager instance = new ClientManager();

    private final List<Client> clients = new ArrayList<>();

    private ArrayBlockingQueue<Message> fromClients = new ArrayBlockingQueue<>(1000);


    private ClientManager() {}

    public static ClientManager getInstance() {
        return instance;
    }


    /**
     * Блокирующая потокобезопасная функция выборки сообщений от клиентов
     * @return
     */
    public Message getMessageFromClient() throws InterruptedException {
        return fromClients.take();
    }

    /**
     * Потокобезопасное помещение сообщения от клиента в очередь
     * @return true - если сообщение удалось поместить в очередь, false - в случае переполненной очереди
     */
    public boolean putMessageFromClient(Message message) {
        System.out.println("- put message: " + message);
        return fromClients.offer(message);
    }

    public void broadcastMessage(Message message)
    {
        clients.forEach(e -> {
            if (!e.getAccount().getName().equals(message.getAuthorName()))
                e.sendMessage(message);
        });
    }

    public void resendPrivateMessage(Message message)
    {
        for (Client client : clients) {
            if (client.getAccount().getName().equals(message.getTargetName()))
            {
                client.sendMessage(message);
                break;
            }
        }
    }

    /**
     * Добавляем нового пользователя, в отдельный поток
     */
    public void addUser(Socket socket)
    {
        boolean found = clients.stream().anyMatch(e -> e.getSocket().equals(socket));
        if (!found)
        {
            Client client = new Client(socket);
            synchronized (clients) {
                clients.add(client);
            }
            client.start();
            // оповещаем сервер о новом подключении
            putMessageFromClient(new Message(client.getAccount(), null, "присоединился к нам"));
        }
    }

    /**
     * Удаляет клиента из списка
     */
    public void unregisterUser(Client client)
    {
        synchronized (clients) {
            if (clients.remove(client))
            {
                Account cl = client.getAccount();
                System.out.println("unregister " + cl);
                putMessageFromClient(new Message(cl, null, "покинул нас!"));
            }
        }
    }

    public void removeAllUsers()
    {
        System.out.println("ClientManager.removeAllUser()");
        synchronized (clients) {
            while (!clients.isEmpty())
            {
                Client client = clients.removeLast();
                System.out.println("  - remove: " + client);
                client.close();
            }
        }
        fromClients.clear();
        System.out.println("ClientManager.removeAllUser() done!");
    }

}
