
import java.io.*; 
import java.util.logging.Level;  
import java.util.logging.Logger;  
import javax.comm.*;  
import java.util.*; 

public class sender extends Thread implements Runnable {
    static Enumeration portList;
    static CommPortIdentifier portId;
    static String messageString;
    public static SerialPort serialPort;
    static OutputStream outputStream;
    InputStream inputStream;
    static boolean outputBufferEmptyFlag = false;
    String sbuf;
    static String rc;
    
    private void open(String port_name,int baud) throws Exception {
        Enumeration port_list = CommPortIdentifier.getPortIdentifiers();
        
        while (port_list.hasMoreElements()) {
            CommPortIdentifier port_id = (CommPortIdentifier) port_list.nextElement();
            if (port_id.getName().equals(port_name)) {
            println(port_id.getName()+" : TRYING TO CONNECT\n.\n.");
                    SerialPort port = (SerialPort) port_id.open("PortListOpen",200);
                            int baudRate = baud;
                            port.setSerialPortParams(baudRate,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                        port.notifyOnDataAvailable(false);
                        inputStream = port.getInputStream();
                        outputStream = (OutputStream) port.getOutputStream();
                        serialPort = port;
            }
        }
        
}
    
    public static void print(String s){
    Rc_car.output.setText(Rc_car.output.getText()+s);
    Rc_car.output.setCaretPosition(Rc_car.output.getDocument().getLength());
}

    public static void println(String s){
    Rc_car.output.setText(Rc_car.output.getText()+'\n'+s);
    Rc_car.output.setCaretPosition(Rc_car.output.getDocument().getLength());
}

    public static void println(){
    Rc_car.output.setText(Rc_car.output.getText()+'\n');
    Rc_car.output.setCaretPosition(Rc_car.output.getDocument().getLength());
}

    public static void println(Exception s){
    Rc_car.output.setText(Rc_car.output.getText()+'\n'+s);
    Rc_car.output.setCaretPosition(Rc_car.output.getDocument().getLength());
}

    private void sendMessage_mode1() throws Exception {
        int data[] = Rc_car.send;
        for(int i=0;i<Rc_car.number_of_bits;i++)
        {
            if(true)
            {
                to_bin(data[i]);
                OutputStreamWriter w;
                w = new OutputStreamWriter(outputStream) {};
                BufferedWriter p = new BufferedWriter(w);
                if (serialPort != null) {
                        p.write(data[i]);
                        Thread.sleep(Rc_car.bit_timeout);
                System.out.println("check - 1");
                p.flush();
                System.out.println("check - 2");
                }
            }
        }
    }
    
    private void sendMessage_mode2() {
        OutputStreamWriter w;
        w = new OutputStreamWriter(outputStream) {};
        BufferedWriter p = new BufferedWriter(w);
        if (serialPort != null) {
            try {
                p.write(Rc_car.mode2_byte);
//                to_bin(Rc_car.mode2_byte);
            } catch (Exception ex) {
                    println(ex);
            }
                try {
                System.out.println("check - 1");
                p.flush();
                System.out.println("check - 2");
                } catch (Exception e) {
                }
        }
    }
    
    public void to_bin(int n){
        println();
        int i,j;
        for(i=n,j=0;j<8;i=i/2,j++)
        {
            print(i%2+"");
        }
    }
    
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            /*
            case SerialPortEvent.BI:

            case SerialPortEvent.OE:

            case SerialPortEvent.FE:

            case SerialPortEvent.PE:

            case SerialPortEvent.CD:

            case SerialPortEvent.CTS:

            case SerialPortEvent.DSR:

            case SerialPortEvent.RI:


            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            println("event.getEventType()");
            break;
             *
             */

            case SerialPortEvent.DATA_AVAILABLE:
                println("inside event handler data available");
                byte[] readBuffer = new byte[20];
                try {
                    while (inputStream.available() > 0) {
                        int numBytes = inputStream.read(readBuffer);
                    }
                    print(new String(readBuffer));
                    sbuf = new String(readBuffer);
                    System.exit(1);
                } catch (IOException e) {
                    println(e);
                }

                break;
        }
}
    
    public sender(String port_name,int baud)throws Exception{
        rc = port_name;
        open(rc,baud);
        start();
    }
    
    @Override
    public void run(){
        int i=1;
        while(Rc_car.Thread_running)
        {
            if(Rc_car.should_send)
            {
                try{
                    switch(Rc_car.mode)
                    {
                        case 1 :
                            sendMessage_mode1();
                        break;
                        case 2 :
                            sendMessage_mode2();
                            Rc_car.should_send = false;
                        break;
                    }
                }
                catch(Exception e){
                    println(e.getMessage());
                }
            }
        }
        try{
            serialPort.close();
            outputStream.close();
            inputStream.close();
        }
        catch(Exception ec){
            println(ec);
        }
    }
}