import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.awt.*;
import java.net.*;

class MultiChat implements KeyListener {
    static MulticastSocket mcs, mcs2, mcs3;
    DatagramPacket in , out;
    static String[] arr = new String[100];
    static Set hs = new LinkedHashSet();
    static JFrame f = new JFrame("채팅프로그램");
    static String name, name2;
    JTextArea ta = new JTextArea();
    JTextField tf = new JTextField();
    JTextArea ta2 = new JTextArea("[접속자 명단]");
    InetAddress SIp = InetAddress.getByName("230.100.50.5");
    int SPort = 7777, tPort = 8888, ePort = 9999;

    MultiChat() throws IOException {
        StrSet();
        name = JOptionPane.showInputDialog("닉네임을 정하세요.");
        name2 = name;
        name = name + " 님이 입장하셨습니다.\n";
        mcs = new MulticastSocket(SPort);
        mcs2 = new MulticastSocket(tPort);
        mcs3 = new MulticastSocket(ePort);
        mcs.joinGroup(SIp);
        mcs2.joinGroup(SIp);
        mcs3.joinGroup(SIp);
        DatagramPacket temp = new DatagramPacket(name.getBytes(), name.getBytes().length, SIp, SPort);
        DatagramPacket temp2 = new DatagramPacket(name2.getBytes(), name2.getBytes().length, SIp, ePort);
        mcs.send(temp);
        mcs3.send(temp2);
        new m3().start();

        new ATR().start();
        new ATS().start();

        f.setSize(600, 500);
        f.add(ta, "Center");
        ta.setBackground(Color.gray);
        ta.setFont(new Font("굴림체", Font.BOLD, 15));
        tf.addKeyListener(this);
        f.add(tf, "South");
        f.add(ta2, "East");
        f.setVisible(true);
        f.addWindowListener(new FrameListener());

        service sv = new service();
        sv.start();
    }

    public void StrSet() {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = "";
        }
    }

    public static void main(String args[]) throws IOException {
        new MultiChat();
    }

    public void keyPressed(KeyEvent ke) {
        String t;
        // TODO Auto-generated method stub
        if (ke.getKeyCode() == 10) {
            t = tf.getText();
            t = "[" + name2 + "] : " + t;
            out = new DatagramPacket(t.getBytes(), t.getBytes().length, SIp, SPort);
            try {
                mcs.send(out);
            } catch (IOException e) {}

            tf.setText("");
        }
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    class FrameListener extends WindowAdapter {
        Iterator it;
        public void windowClosing(WindowEvent e) {
            String t = name2 + "님이 나가셨습니다.";
            try {
                InetAddress SIp = InetAddress.getByName("230.100.50.5");
                out = new DatagramPacket(t.getBytes(), t.getBytes().length, SIp, SPort);
                mcs.send(out);
                hs.remove(name2);

                mcs.leaveGroup(SIp);
            } catch (IOException e1) {}
            System.exit(0);
        }
    }


    class service extends Thread {
        byte[] msg = new byte[65000];
        String temp;

        public void run() {
            in = new DatagramPacket(msg, msg.length);

            while (true) {
                try {
                    Arrays.fill(msg, (byte) 0);
                    mcs.receive( in );
                    temp = new String( in .getData()).trim();
                    ta.append(temp + "\n");
                } catch (IOException e) {}
            }
        }
    }

    class ATS extends Thread {
        byte[] msg = new byte[65000];
        Iterator it;
        public void run() {

            while (true) {
                String temp = "";
                DatagramPacket out = new DatagramPacket(msg, msg.length, SIp, tPort);
                it = hs.iterator();

                int i = 1;
                while (it.hasNext()) {
                    if (i == hs.size()) {
                        temp = temp + (String) it.next();
                        break;
                    }
                    temp = temp + (String) it.next() + ",";
                    i++;
                }
                msg = temp.getBytes();
                try {
                    sleep(5000);
                    mcs2.send(out);
                } catch (InterruptedException ie) {} catch (IOException e) {}
            }
        }
    }

    class ATR extends Thread {
        byte msg[] = new byte[65000];

        public void run() {
            DatagramPacket in = new DatagramPacket(msg, msg.length);
            String temp;
            while (true) {
                try {
                    Arrays.fill(msg, (byte) 0);
                    mcs2.receive( in );
                    temp = new String( in .getData()).trim();
                    arr = temp.split(",");
                    hs.clear();
                    int i = 0;
                    while (i < arr.length) {
                        hs.add(arr[i]);
                        i++;
                    }
                    check();
                } catch (IOException e) {}

            }
        }
    }

    public void set() {
        String temp = "[접속자 명단]\n"; //textArea로 바꿔보자
        for (int i = 0; i < arr.length; i++) {
            temp = temp + arr[i] + "\n";
        }
        ta2.setText(temp);
    }

    class m3 extends Thread {
        byte msg[] = new byte[65000];
        DatagramPacket dp;
        Iterator it;
        String temp;
        public void run() {
            dp = new DatagramPacket(msg, msg.length);
            while (true) {
                Arrays.fill(msg, (byte) 0);
                try {
                    mcs3.receive(dp);
                    temp = new String(msg);
                    hs.add(temp);
                    check();
                } catch (IOException e) {}
            }
        }
    }

    public void check() {
        Iterator it = hs.iterator();

        int i = 0;
        while (it.hasNext()) {
            arr[i] = (String) it.next();
            i++;
        }
        set();
    }

}