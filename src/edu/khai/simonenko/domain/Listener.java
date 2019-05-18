package edu.khai.simonenko.domain;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Listener extends WindowAdapter {

    public boolean exit = false;

    @Override
    public void windowClosing(WindowEvent e) {
        exit = true;
    }
}