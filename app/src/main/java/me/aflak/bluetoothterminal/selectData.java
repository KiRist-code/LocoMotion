package me.aflak.bluetoothterminal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import me.aflak.bluetooth.Bluetooth;

public class selectData extends AppCompatActivity implements Bluetooth.CommunicationCallback {
    String dbName = "";
    private int changer = 0;
    private int index = 0;
    private boolean end = false;

    ArrayList<Float> floatuX = new ArrayList<Float>();
    ArrayList<Float> floatdX = new ArrayList<Float>();

    private Button stop_btn;
    private Button see_result;
    private Bluetooth b;
    private TextView text;

    private boolean registered=false;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectdata);

        dbName = setDbName();
        //and this is temp code
//        dbName = "sans";
        stop_btn = (Button)findViewById(R.id.stopBtn);
        see_result = (Button)findViewById(R.id.result);
        text = (TextView)findViewById(R.id.text);

        stop_btn.setEnabled(false);
        see_result.setEnabled(false);
        b = new Bluetooth(this);
        b.enableBluetooth();
        b.setCommunicationCallback(this);

        b.connectToDevice(b.getPairedDevices().get(checkBTlist("CA40"))); //Connecting

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        registered=true;

        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("Analyzing. . .");
                b.send("a");
                Log.e("LOG","Pressed the btn");
            }
        });
        see_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(selectData.this, result.class);
                intent.putExtra("dbName",dbName);
                startActivity(intent);
            }
        });
    }

    public String setDbName() {
        String model = Build.MODEL;
//        String temp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
//        temp = temp + model;
        return "1234";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(registered) {
            unregisterReceiver(mReceiver);
            registered=false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.close:
                b.removeCommunicationCallback();
                b.disconnect();
                Intent intent = new Intent(this, Select.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.rate:
                Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnect(final BluetoothDevice device) {
        //Toast
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stop_btn.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Connected to "+device.getName()+" - "+device.getAddress(), Toast.LENGTH_SHORT).show();
                if(changer == 0){
                    text.setText("Walk!!");
                    b.send("c");
                }
                if(changer == 1) {
                    b.send("b");
                    Log.e("LOG", "Connected to other bluetooth");
                }
            }
        });

    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        if(changer == 0){
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(end == false){
                        text.setText("Something went wrong X( ");
                        Toast.makeText(getApplicationContext(), "Disconnected! Connecting again...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            b.connectToDevice(device);
        }
    }

    public int checkBTlist(String name) {
        for(int i=0;i<b.getPairedDevices().size();i++) {
            if(name.equals(b.getPairedDevices().get(i).getName())) return i;
            else Log.e("LOG", i + " has been passed");
        }
        Log.e("LOG", "There is no bluetooth which has a name");
        text.setText("No Device at near");
        return 0;
    }

    @Override
    public void onMessage(String message) {
        Log.e("LOG",message + " / " + changer);
        if (changer == 0){
            if (message.equals("s")) {
                index = 0;
                int check = checkBTlist("CA16");
                if(check != 0) {
                    Log.e("LOG","Disconnect and Change other bluetooth");
                    b.disconnect();
                    Log.e("LOG","Connecting...");
                    b.connectToDevice(b.getPairedDevices().get(check));
                    changer++;
                }
            }
            else{
                index++;
                floatuX.add(Float.parseFloat(message));
                Log.e("LOG", "data: " + floatuX.get(index-1));
            }
        }
        else {
            if (message.equals("e")) {
                index = 0;
//                text.setText("Packing. . .");
                Log.e("LOG","Packing. . .");
                pushData(floatuX,floatdX);
                end = true;
                b.disconnect();
            }
            else {
                index++;
                floatdX.add(Float.parseFloat(message));
//                Log.e("LOG", "" + floatdX.get(index-1));
            }
        }
    }

    public void pushData(ArrayList<Float> ux, ArrayList<Float> dx){
        DataDTO data = new DataDTO(ux, dx); //ChatDTO를 이용하여 데이터를 묶는다.
        databaseReference.child("entry").child(dbName).child("data").push().setValue(data); // 데이터 푸쉬
        databaseReference.child("entry").child(dbName).child("result").setValue(-1); //값 설정
        changer = 0;
        floatuX.clear();
        floatdX.clear();
        Log.e("LOG","dbName: "+dbName);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"Press the 결과보기 button!!",Toast.LENGTH_LONG).show();
                see_result.setEnabled(true);
            }
        });
    }

    @Override
    public void onError(String message) {
        Toast.makeText(getApplicationContext(), "Error: "+message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectError(final BluetoothDevice device, final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Error: "+message + " => Trying again in 3 sec.", Toast.LENGTH_SHORT).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        b.connectToDevice(device);
                    }
                }, 2000);
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("!!!!!!!!","nnn");
            final String action = intent.getAction();


            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                Intent intent1 = new Intent(selectData.this, Select.class);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if(registered) {
                            unregisterReceiver(mReceiver);
                            registered=false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if(registered) {
                            unregisterReceiver(mReceiver);
                            registered=false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                }
            }
        }
    };
}
