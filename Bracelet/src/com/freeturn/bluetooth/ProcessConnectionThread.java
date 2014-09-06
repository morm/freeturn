package com.freeturn.bluetooth;

import java.io.InputStream;
import java.net.URLConnection;

import com.freeturn.bracelet.MainActivity;

public class ProcessConnectionThread implements Runnable{
 
 
    // Constant that indicate command from devices
    private static final int EXIT_CMD = -1;
    private static final int MESSAGE = 1;

    URLConnection mConnection;
    
    public ProcessConnectionThread(URLConnection connection)
    {
        mConnection = connection;
    }
 
    @Override
    public void run() {
        try {
            // prepare to receive data
            InputStream inputStream = mConnection.getInputStream();
 
            System.out.println("waiting for input");
 
            while (true) {
                int data = inputStream.read();
 
                if (data == EXIT_CMD)
                {
                    System.out.println("finish process");
                    break;
                }
                MainActivity main_form = new MainActivity();
                main_form.RefreshNavigation(data);
               
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /**
     * Process the command from client
     * @param command the command code
     */
    private void processCommand(int command) {
        try {
            switch (command) {
                case MESSAGE:
                    System.out.println("Right");
                    
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}