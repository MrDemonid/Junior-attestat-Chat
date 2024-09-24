package org.junior.view;

import org.junior.view.listeners.*;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;

public class ViewSwing extends JFrame implements View {

    private static final int WINDOW_WIDTH = 300;
    private static final int WINDOW_HEIGHT = 200;
    private static final int WINDOW_POS_X = 0;
    private static final int WINDOW_POS_Y = 0;

    private final EventListenerList listenerList;

    JButton btnStart;
    JButton btnStop;
    JTextArea logPanel;

    public ViewSwing() throws HeadlessException {
        setTitle("Chat server");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocation(WINDOW_POS_X, WINDOW_POS_Y);
        createGUI();
        listenerList = new EventListenerList();
        setVisible(true);
    }

    @Override
    public void showMessage(String message) {
        logPanel.append(message);
        logPanel.append("\n");
    }

    @Override
    public <T extends ServerEventListener> void removeListeners(Class<T> t, T l)
    {
            listenerList.remove(t, l);
    }

    @Override
    public <T extends ServerEventListener> void addListener(Class<T> t, T l) {
        listenerList.add(t, l);
    }


        /*
       =======================================================================
        Создание интерфейса
       =======================================================================
    */

    private void createGUI()
    {
        add(createLogPanel());
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    private Component createLogPanel()
    {
        logPanel = new JTextArea();
        return new JScrollPane(logPanel);
    }

    private Component createControlPanel()
    {
        JPanel pan = new JPanel(new GridLayout(1, 2, 5, 5));
        btnStart = new JButton("Start");
        btnStop = new JButton("Stop");
        pan.add(btnStart);
        pan.add(btnStop);

        // Слушатель кнопки "Start server", просто уведомляет слушателей
        btnStart.addActionListener(e -> fireStartServer(new StartServerEvent(e.getSource())));

        // Слушатель кнопки "Stop server", просто уведомляет слушателей
        btnStop.addActionListener(e -> fireStopServer(new StopServerEvent(e.getSource())));

        return  pan;
    }


        /*===========================================================================
     *
     * Реализация подсистемы слушателей от контролов
     *
     ===========================================================================*/


    private void fireStartServer(StartServerEvent event)
    {
        StartServerListener[] listeners = listenerList.getListeners(StartServerListener.class);
        for (int i = listeners.length-1; i>=0; i--)
            (listeners[i]).actionPerformed(event);
    }

    private void fireStopServer(StopServerEvent event)
    {
        StopServerListener[] listeners = listenerList.getListeners(StopServerListener.class);
        for (int i = listeners.length-1; i>=0; i--)
            (listeners[i]).actionPerformed(event);
    }


}
