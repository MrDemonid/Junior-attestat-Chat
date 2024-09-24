package org.junior;

import org.junior.controller.Client;

public class Main {
    public static void main(String[] args) {

        Client client = new Client(new Account("Ivan", "12345", "127.0.0.1", ""));
        client.run();
    }
}