package org.junior;

import org.junior.controller.Server;
import org.junior.view.*;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Server server = new Server(new ViewSwing());
            }
        });
    }
}