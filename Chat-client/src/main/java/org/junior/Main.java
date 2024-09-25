package org.junior;

import org.junior.controller.Client;
import org.junior.view.ViewSwing;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client(new ViewSwing());
            }
        });

    }
}