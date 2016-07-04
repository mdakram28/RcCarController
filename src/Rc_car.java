import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import javax.imageio.ImageIO;
public class Rc_car extends JFrame implements ActionListener ,AWTEventListener {
    public static java.util.Timer timer = new java.util.Timer();
    public static JTextArea output = new JTextArea(20,20);
    public static JComboBox port = new JComboBox();
    public static int mode=1;
    public static boolean should_send = true;
    public static int bit_timeout = 75;
    public JTextField baud_rate = new JTextField(13);
    JButton connect = new JButton(" CONNECT ");
    JButton settings_b = new JButton("SETTINGS");
    JButton up = new JButton("FORWARD");
    JButton down = new JButton("BACK");
    JButton right = new JButton("RIGHT");
    JButton left = new JButton("LEFT");
    JButton stop = new JButton("STOP");
    boolean up_pressed = false,down_pressed = false,left_pressed = false,right_pressed = false;
    public static int turn_left_timeout = 250;
    public static int turn_right_timeout = 250;
    public static int reverse_timeout = 50;
    Container main_pane;
    JButton[] bits = new JButton[8];
    JTextField[] box = new JTextField[8];
    static int number_of_bits = 4;
    public static int[] send = new int[8];
    public static int mode2_byte = 0;
    String temp_b0,temp_b1,temp_b2,temp_b3;
    Thread t;
    public static boolean Thread_running=false;
    int default_index=1;
    Settings_Frame settings;
    
    public static void main(String[] args) {
        Rc_car f = new Rc_car();
        f.setVisible(true);
    }
    public Rc_car(){
        settings = new Settings_Frame();
        settings.setVisible(false);
        setTitle("Serial Data Remote");
	setSize(600,500);
	setLocation(100,200);
        setResizable(false);
        
        BufferedImage image = null;
        try {
            image = ImageIO.read(
                getClass().getResource("icon.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setIconImage(image);
        
        addWindowListener(new WindowAdapter() {
	  	public void windowClosing(WindowEvent e) {
                    stop_here();
		   System.exit(0);
	  	}
	} );
        
	main_pane = getContentPane();
        main_pane.setLayout(new GridLayout());
        
        sender.println("INIT APPLICATION\nREADY");
        initButton();
        
        getPortList();
    }
    
    public void refreshAll(){
        switch(mode)
        {
            case 1 :    
                sender.print("\nMode 1 RF-DATA : ");
                for(int i=0;i<4;i++)
                {
                    mode1_refresh(i);
                    sender.print("\n\t     ");
                }
            break;
            case 2 :
                sender.print("\nMode 2 RF-DATA : ");
                mode2_refresh();
                should_send = true;
        }
    }
    private void initButton(){
        JScrollPane scroll = new JScrollPane();
        scroll.setAutoscrolls(true);
        scroll.setPreferredSize(new Dimension(200,200));
        JPanel left_pane = new JPanel();
        JPanel pan1 = new JPanel();
        JPanel pan2 = new JPanel();
        JPanel pan3[] = new JPanel[8];
        JPanel pan4 = new JPanel();
        
        left_pane.setLayout(new BoxLayout(left_pane,BoxLayout.PAGE_AXIS));
        pan4.setLayout(new GridLayout(3,3));
        
        port.setPreferredSize(new Dimension(150,20));
        pan1.add(port);
        pan1.add(baud_rate);
        baud_rate.setText("9600");
        
        pan1.add(connect);
        pan1.add(settings_b);
        connect.addActionListener(this);
        settings_b.addActionListener(this);
        
        for(int i=0;i<8;i++)
        {
            pan3[i] = new JPanel();
            pan3[i].setLayout(new BoxLayout(pan3[i],BoxLayout.PAGE_AXIS));
            bits[i] = new JButton("b"+(8-i));
            bits[i].addActionListener(this);
            pan3[i].add(bits[i]);
            box[i] = new JTextField(1);
            box[i].setText("0");
            box[i].addActionListener(this);
            pan3[i].add(box[i]);
            pan2.add(pan3[i]);
        }
        
        pan4.add(new JLabel(""));
        pan4.add(up);
        pan4.add(new JLabel(""));
        pan4.add(left);
        pan4.add(stop);
        pan4.add(right);
        pan4.add(new JLabel(""));
        pan4.add(down);
        pan4.add(new JLabel(""));
        
        up.addActionListener(this);
        down.addActionListener(this);
        left.addActionListener(this);
        right.addActionListener(this);
        stop.addActionListener(this);
        
        left_pane.add(pan1);
        left_pane.add(pan2);
        left_pane.add(pan4);
        
        add(left_pane);
        scroll.getViewport().add(output);
        add(scroll);
        output.setEditable(false);
    }
    public static void getPortList(){
        Enumeration port_list = CommPortIdentifier.getPortIdentifiers();
        port.removeAllItems();
        while (port_list.hasMoreElements()) {
            CommPortIdentifier port_id = (CommPortIdentifier) port_list.nextElement();
            if(port_id.getPortType() == CommPortIdentifier.PORT_SERIAL)
            port.addItem(port_id.getName());
//            if(port_id.getName().equals("COM20"))sender.println("availaible");
        }
        port.setSelectedItem("COM20");
    }
    
    @Override public void eventDispatched(AWTEvent event) {
        if(event instanceof KeyEvent){
            KeyEvent key = (KeyEvent)event;
            
            if(key.getID()==KeyEvent.KEY_PRESSED){
                this.keyPressed(key);
            }
            else if(key.getID()==KeyEvent.KEY_RELEASED){
                this.keyReleased(key);
            }
            
            key.consume();
        }
    }
    public void keyPressed(KeyEvent e){
        int keyCode = e.getKeyCode();
        
        switch( keyCode ) { 
            case KeyEvent.VK_UP:
                    if(up_pressed)break;
                    up_pressed = true;
                    go_forward();
                    
                break;
            case KeyEvent.VK_DOWN:
                    if(down_pressed)break;
                    down_pressed = true;
                    go_back();
                break;
            case KeyEvent.VK_LEFT:
                    if(left_pressed)break;
                    left_pressed = true;
                    sender.println("-------------LEFT-----------");
                    turn_left_key();
                break;
            case KeyEvent.VK_RIGHT :
                    if(right_pressed)break;
                    right_pressed = true;
                    sender.println("-------------RIGHT----------");
                    turn_right_key();

                break;
        }
    }
    public void keyReleased(KeyEvent e){
        int keyCode = e.getKeyCode();
        
        switch( keyCode ) { 
            case KeyEvent.VK_UP:
                    up_pressed = false;
                    if(!right_pressed && !left_pressed)stop_here();
            break;
            case KeyEvent.VK_DOWN:
                    down_pressed = false;
                    if(!right_pressed && !left_pressed)stop_here();
            break;
            case KeyEvent.VK_LEFT:
                left_pressed = false;
                if(up_pressed)go_forward();
                else if(down_pressed)go_back();
                else stop_here();
            break;
            case KeyEvent.VK_RIGHT :
                right_pressed = false;
                if(up_pressed)go_forward();
                else if(down_pressed)go_back();
                else stop_here();
                break;
        }
    }
    @Override public void actionPerformed(ActionEvent e){
        Object o = (JButton)e.getSource();
        char ch=((JButton)o).getText().charAt(1);
        if(ch=='1'||ch=='2'||ch=='3'||ch=='4'||ch=='5'||ch=='6'||ch=='7'||ch=='8')
        {
            int n = 8-Integer.parseInt(ch+"");
            if(box[n].getText().equals("1"))
            {
                box[n].setText("0");
            }
            else
            {
                box[n].setText("1");
            }
            mode1_refresh(n);
        }
            else if(o.equals(up))
            {
                go_forward();
            }
            else if(o.equals(down))
            {
                go_back();
            }
            else if(o.equals(left))
            {
                turn_left();
            }
            else if(o.equals(right))
            {
                turn_right();
            }
            else if(o.equals(stop))
            {
                stop_here();
            }
            else if(o.equals(connect))
            {
                if(!Thread_running)
                {
                    try{
                        int baud = Integer.parseInt(baud_rate.getText());
                        String port_name = ((String)port.getSelectedItem()).toUpperCase();
                            
                        Thread_running = true;
                        t = new sender(port_name,baud);
                        sender.println("SENDER THREAD CONSTRUCTED.....");
                        connect.setText("DISCONNECT");
                        this.getToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
                        refreshAll();
                    }
                    catch(Exception exp)
                    {
                        sender.println("FAILED TO CONNECT");
                    }
                }
                else
                {
                    try{
                        Thread_running = false;
                        
                        sender.println("SENDER THREAD destroyed.....");
//                        port.setEditable(true);
//                        port.setBackground(Color.WHITE);
////                        baud_rate.setEditable(true);
//                        baud_rate.setBackground(Color.WHITE);
                        this.getToolkit().removeAWTEventListener(this);
                        connect.setText(" CONNECT ");
                    }
                    catch(Exception ex)
                    {
                        sender.println(ex);
                    }
                }
            }
            else if(o.equals(settings_b))
            {
                settings.setVisible(true);
            }
    }
    
    public void stop_here(){
            sender.println("-------------STOP-----------");
            box[0].setText("0");
            box[1].setText("0");
            box[2].setText("0");
            box[3].setText("0");
            refreshAll();
    }
    public void turn_left_key(){
        //if(right_pressed || up_pressed || down_pressed)
        if(true)    
        {
            stop_here();
            try{Thread.sleep(reverse_timeout);sender.println("reverse timeout");}catch(Exception eh){}
            box[1].setText("1");
            box[2].setText("1");
            refreshAll();
        }
        else
        {
            box[0].setText("0");
            box[1].setText("1");
            box[2].setText("1");
            box[3].setText("0");
            refreshAll();
        }
    }
    public void turn_right_key(){
        //if(left_pressed || up_pressed || down_pressed)
        if(true)
        {
                stop_here();
                try{
                    Thread.sleep(reverse_timeout);
                    sender.println("reverse timeout");
                }
                catch(Exception eh)
                {}
            box[0].setText("1");
            box[3].setText("1");
            refreshAll();
        }
        
        else
        {
            box[0].setText("1");
            box[1].setText("0");
            box[2].setText("0");
            box[3].setText("1");
            refreshAll();
        }
    }
    public void turn_left(){
            temp_b0 = box[0].getText();
            temp_b1 = box[1].getText();
            temp_b2 = box[2].getText();
            temp_b3 = box[3].getText();
            box[0].setText("0");
            box[1].setText("0");
            box[2].setText("0");
            box[3].setText("0");
            refreshAll();
            try{
                Thread.sleep(reverse_timeout);
                sender.println("reverse timeout");
            }
            catch(Exception eh)
            {}
        box[1].setText("1");
        box[2].setText("1");
        refreshAll();
        try
        {
            Thread.sleep(turn_left_timeout);
            sender.println("turn left timeout");
        }
        catch(Exception e){}
        box[1].setText("0");
        box[2].setText("0");
                try{
                    Thread.sleep(reverse_timeout);
                    sender.println("reverse timeout");
                }
                catch(Exception exp){}
                box[0].setText(temp_b0);;
                box[1].setText(temp_b1);
                box[2].setText(temp_b2);
                box[3].setText(temp_b3);
                refreshAll();
//                left_pressed = false;
    }
    public void turn_right(){
            temp_b0 = box[0].getText();
            temp_b1 = box[1].getText();
            temp_b2 = box[2].getText();
            temp_b3 = box[3].getText();
            box[0].setText("0");
            box[1].setText("0");
            box[2].setText("0");
            box[3].setText("0");
            refreshAll();
            try{
                Thread.sleep(reverse_timeout);
                sender.println("reverse timeout");
            }
            catch(Exception eh)
            {}
        box[0].setText("1");
        box[3].setText("1");
        refreshAll();
        try
        {
            Thread.sleep(turn_right_timeout);
            sender.println("turn right timeout");
        }
        catch(Exception e){}
        box[0].setText("0");
        box[3].setText("0");
                try{
                    Thread.sleep(reverse_timeout);
                    sender.println("reverse timeout");
                }
                catch(Exception exp){}
                box[0].setText(temp_b0);
                box[1].setText(temp_b1);
                box[2].setText(temp_b2);
                box[3].setText(temp_b3);
                refreshAll();
//                right_pressed = false;
    }
    public void go_forward(){
        //if(down_pressed || left_pressed || right_pressed)
        if(true)
        {
            stop_here();
                try{Thread.sleep(reverse_timeout);sender.println("reverse timeout");}catch(Exception eh){}
            box[1].setText("1");
            box[3].setText("1");
            refreshAll();
        }
        else
        {
            box[0].setText("0");
            box[1].setText("1");
            box[2].setText("0");
            box[3].setText("1");
            refreshAll();
        }
    }
    public void go_back(){
        //if(up_pressed || left_pressed || right_pressed)
        if(true)
        {
            stop_here();
            try{Thread.sleep(reverse_timeout);sender.println("reverse timeout");}catch(Exception eh){}
            box[0].setText("1");
            box[2].setText("1");
            refreshAll();
        }
        
        else
        {
            box[0].setText("1");
            box[1].setText("0");
            box[2].setText("1");
            box[3].setText("0");
            refreshAll();
        }
    }
    
    public void mode1_refresh(int pin){
        
        int n=7-pin;
        int r=0;
        r+=Integer.parseInt(box[pin].getText())*4;
        r+=(n%2)*8;
        n/=2;
        r+=(n%2)*16;
        n/=2;
        r+=(n%2)*32;
        n/=2;
        send[pin] = r;
        to_bin(r);
    }
    public void mode2_refresh(){
        mode2_byte=Integer.parseInt(box[0].getText())*4 + Integer.parseInt(box[1].getText())*8 + Integer.parseInt(box[2].getText())*16 + Integer.parseInt(box[3].getText())*32;
        to_bin(mode2_byte);
        sender.println();
    }
    public void to_bin(int n){
        int i,j;
        for(i=n,j=0;j<8;i=i/2,j++)
        {
            sender.print(i%2+"");
        }
    }
}
