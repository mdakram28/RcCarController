import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class Settings_Frame extends JFrame implements ActionListener {
    
    String[] nos = {"4","5","6","7","8"};
    JComboBox list = new JComboBox(nos);
    JTextField tlt_b = new JTextField(4);
    JTextField trt_b = new JTextField(4);
    JTextField rt_b = new JTextField(4);
    JTextField bt_b = new JTextField(4);
    JButton ok = new JButton("OK") , apply = new JButton("APPLY") , cancel = new JButton("CANCEL") ;
    ButtonGroup mode = new ButtonGroup();
    JRadioButton mode1 = new JRadioButton();
    JLabel mode1_l = new JLabel("3-add 1-data mode");
    JRadioButton mode2 = new JRadioButton();
    JLabel mode2_l = new JLabel("4-data mode");
    
    public Settings_Frame(){
        setTitle("Serial Data Remote : SETTINGS");
	setSize(325,320);
	setLocation(400,400);
//        setResizable(false);
        setLayout(new BorderLayout());
        init_gui();
        
        list.setSelectedItem(Rc_car.number_of_bits+"");
        tlt_b.setText(Rc_car.turn_left_timeout+"");
        trt_b.setText(Rc_car.turn_right_timeout+"");
        rt_b.setText(Rc_car.reverse_timeout+"");
        bt_b.setText(Rc_car.bit_timeout+"");
    }
    
    void init_gui(){
        JPanel mode_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mode.add(mode1);
        mode.setSelected((Rc_car.mode==1?mode1.getModel():mode2.getModel()),true);
        mode.add(mode2);
        mode_panel.add(mode1);
        mode_panel.add(mode1_l);
        mode_panel.add(mode2);
        mode_panel.add(mode2_l);
        
        JPanel p1 = new JPanel(new GridLayout(5,2,10,10));
        
        p1.add(new JLabel("Bits Length        :     "));
        p1.add(list);
        
        p1.add(new JLabel("Turn Left Timeout  :     "));
        p1.add(tlt_b);
        
        p1.add(new JLabel("Turn Right Timeout :     "));
        p1.add(trt_b);
        
        p1.add(new JLabel("Revers Timeout     :     "));
        p1.add(rt_b);
        
        p1.add(new JLabel("Bit Timeout     :     "));
        p1.add(bt_b);
        p1.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p2.add(ok);
        p2.add(apply);
        p2.add(cancel);
        p2.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        
        ok.addActionListener(this);
        apply.addActionListener(this);
        cancel.addActionListener(this);
        mode1.addActionListener(this);
        mode2.addActionListener(this);
        
        add(mode_panel,BorderLayout.NORTH);
        add(p1,BorderLayout.CENTER);
        add(p2,BorderLayout.SOUTH);
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        if(o.equals(apply))
        {
            Rc_car.number_of_bits = Integer.parseInt(list.getSelectedItem()+"");
            Rc_car.turn_left_timeout = Integer.parseInt(tlt_b.getText());
            Rc_car.turn_right_timeout = Integer.parseInt(trt_b.getText());
            Rc_car.reverse_timeout = Integer.parseInt(rt_b.getText());
            Rc_car.bit_timeout = Integer.parseInt(bt_b.getText());
            
            print_settings();
        }
        else if(o.equals(ok))
        {
            Rc_car.number_of_bits = Integer.parseInt(list.getSelectedItem()+"");
            Rc_car.turn_left_timeout = Integer.parseInt(tlt_b.getText());
            Rc_car.turn_right_timeout = Integer.parseInt(trt_b.getText());
            Rc_car.reverse_timeout = Integer.parseInt(rt_b.getText());
            Rc_car.bit_timeout = Integer.parseInt(bt_b.getText());
            
            setVisible(false);
            print_settings();
        }
        else if(o.equals(cancel))
        {
            setVisible(false);
        }
        else if(o.equals(mode1))
        {
            Rc_car.mode = 1;
            list.removeAllItems();
            list.addItem("4");
            list.addItem("5");
            list.addItem("6");
            list.addItem("7");
            list.addItem("8");
            list.setBackground(Color.WHITE);
        }
        else if(o.equals(mode2))
        {
            Rc_car.mode = 2;
            list.removeAllItems();
            list.addItem("4");
            list.setBackground(Color.LIGHT_GRAY);
        }
        
    }
    
    void print_settings(){
        sender.print("\n                  SETTINGS");
        sender.println("Data Transfer Mode : "+(mode.getSelection().equals(mode1.getModel())?"1":"")+(mode.getSelection().equals(mode2.getModel())?"2":""));
        sender.println("number_of_bits     : "+list.getSelectedItem());
        sender.println("turn_left_timeout  : "+tlt_b.getText());
        sender.println("turn_right_timeout : "+trt_b.getText());
        sender.println("reverse_timeout    : "+rt_b.getText());
        sender.println("bit_timeout    : "+bt_b.getText());
        sender.println("                  SETTINGS\n");
    }
}
