package bluesmirfxx.root.bluesmirf;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Button On,Off,Visible,list,send;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice>pairedDevices;
    private ListView lv;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        On = (Button)findViewById(R.id.button1);
        Off = (Button)findViewById(R.id.button2);
        Visible = (Button)findViewById(R.id.button3);
        list = (Button)findViewById(R.id.button4);
        lv = (ListView)findViewById(R.id.listView1);
        send = (Button)findViewById(R.id.send);

        BA = BluetoothAdapter.getDefaultAdapter();

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage(btSocket, "A");
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView <?> adapter, View v, int position, long id) {
                String name = (String) adapter.getItemAtPosition(position);
                String[] sp = name.split(",");
                if(sp[0]!=""&&sp[1]!="") {
                    connect(position,sp[0],sp[1]);
                }
            }
        });
    }

    private void sendMessage(BluetoothSocket socket, String msg) {
        OutputStream outStream;
        try {
            outStream = socket.getOutputStream();
            System.out.println("out stream::::::::::::::::::"+outStream.toString());
            //byte[] byteString = (msg).getBytes();
            byte[] byteString = stringToBytesUTFCustom(msg);
            outStream.write(byteString);
        } catch (IOException e) {
            Log.d("BLUETOOTH_COMMS", e.getMessage());
        }
    }

    public byte[] stringToBytesUTFCustom(String str) {
        char[] buffer = str.toCharArray();
        byte[] b = new byte[buffer.length << 1];
        for (int i = 0; i < buffer.length; i++) {
            int bpos = i << 1;
            b[bpos] = (byte) ((buffer[i]&0xFF00)>>8);
            b[bpos + 1] = (byte) (buffer[i]&0x00FF);
        }
        return b;
    }

    private static final UUID XXUUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    public void connect(int id, String name, String address) {
        super.onResume();
        BluetoothDevice device =BA.getRemoteDevice(address);
        try {
            btSocket = device.createRfcommSocketToServiceRecord(XXUUID);
        } catch (IOException e) {
            System.out.println("Fatal Error create socket failed: " + e.getMessage());
        }
        BA.cancelDiscovery();
        try {
            btSocket.connect();
            System.out.println("Connection established");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                System.out.println("unable to close socket connection failure" + e2.getMessage() + ".");
            }
        }
    }

    public void on(View view){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(),"Turned on",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Already on",Toast.LENGTH_LONG).show();
        }
    }

    public void list(View view){
        pairedDevices = BA.getBondedDevices();

        ArrayList list = new ArrayList();
        for(BluetoothDevice bt : pairedDevices)
            list.add(bt.getName()+","+bt.getAddress());

        Toast.makeText(getApplicationContext(),"Showing Paired Devices", Toast.LENGTH_SHORT).show();
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);
    }

    public void off(View view){
        BA.disable();
        Toast.makeText(getApplicationContext(),"Turned off" ,
                Toast.LENGTH_LONG).show();
    }

    public void visible(View view){
        Intent getVisible = new Intent(BluetoothAdapter.
                ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}