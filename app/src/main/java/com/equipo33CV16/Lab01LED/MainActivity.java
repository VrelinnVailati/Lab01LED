package com.equipo33CV16.Lab01LED;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    Button jbnBuscar, jbnConectar;
    ListView jlvDispositivos;
    BluetoothAdapter adaptador;
    static final int BT_ENABLE_REQUEST = 10;
    static final int SETTINGS = 20;
    UUID idDispositivo = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    int mBufferSize = 50000;
    static final String DEVICE_EXTRA = "com.equipo33CV16.Lab01LED.Socket";
    static final String DEVICE_UUID = "com.equipo33CV16.Lab01LED.uuid";
    static final String DEVICE_LIST = "com.equipo33CV16.Lab01LED.device_list";
    static final String DEVICE_LIST_SELECTED = "com.equipo33CV16.Lab01LED.device_list_selected";
    static final String BUFFER_SIZE = "com.equipo33CV16.Lab01LED.buffer_size";
    static final String TAG = "BlueText5-MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jbnBuscar = findViewById(R.id.xbnBuscar);
        jbnConectar = findViewById(R.id.xbnConectar);
        jlvDispositivos = findViewById(R.id.xlvDispositivos);

        if (savedInstanceState != null) {
            ArrayList<BluetoothDevice> dispositivos = savedInstanceState.getParcelableArrayList(DEVICE_LIST);
            if (dispositivos != null) {
                initList(dispositivos);
                MyAdapter adapter = (MyAdapter) jlvDispositivos.getAdapter();
                int selectedIndex = savedInstanceState.getInt(DEVICE_LIST_SELECTED);
                if (selectedIndex != -1) {
                    adapter.setSelectedIndex(selectedIndex);
                    jbnConectar.setEnabled(true);
                }
            } else {
                initList(new ArrayList<>());
            }

        } else {
            initList(new ArrayList<>());
        }
        jbnBuscar.setOnClickListener(v -> {
            adaptador = BluetoothAdapter.getDefaultAdapter();

            if (adaptador == null) {
                Toast.makeText(getApplicationContext(), "Bluetooth not found", Toast.LENGTH_SHORT).show();
            } else if (!adaptador.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, BT_ENABLE_REQUEST);
            } else {
                new SearchDevices().execute();
            }
        });

        jbnConectar.setOnClickListener(v -> {
            BluetoothDevice device = ((MyAdapter) (jlvDispositivos.getAdapter())).getSelectedItem();
            Intent intent = new Intent(getApplicationContext(), InteraccionLED.class);
            intent.putExtra(DEVICE_EXTRA, device);
            intent.putExtra(DEVICE_UUID, idDispositivo.toString());
            intent.putExtra(BUFFER_SIZE, mBufferSize);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_ENABLE_REQUEST:
                if (resultCode == RESULT_OK) {
                    msg("Bluetooth Enabled successfully");
                    new SearchDevices().execute();
                } else {
                    msg("Bluetooth couldn't be enabled");
                }

                break;
            case SETTINGS: //If the settings have been updated
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String uuid = prefs.getString("prefUuid", "Null");
                idDispositivo = UUID.fromString(uuid);
                Log.d(TAG, "UUID: " + uuid);
                String bufSize = prefs.getString("prefTextBuffer", "Null");
                mBufferSize = Integer.parseInt(bufSize);

                String orientation = prefs.getString("prefOrientation", "Null");
                Log.d(TAG, "Orientation: " + orientation);
                switch (orientation) {
                    case "Landscape":
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        break;
                    case "Portrait":
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        break;
                    case "Auto":
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                        break;
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void msg(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    private void initList(ArrayList<BluetoothDevice> objects) {
        final MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.list_item, R.id.lstContent, objects);
        jlvDispositivos.setAdapter(adapter);

        jlvDispositivos.setOnItemClickListener((parent, view, position, id) -> {
            adapter.setSelectedIndex(position);
            jbnConectar.setEnabled(true);
        });
    }

    private class SearchDevices extends AsyncTask<Void, Void, List<BluetoothDevice>> {

        @Override
        protected List<BluetoothDevice> doInBackground(Void... params) {
            Set<BluetoothDevice> pairedDevices = adaptador.getBondedDevices();
            return new ArrayList<>(pairedDevices);
        }

        @Override
        protected void onPostExecute(List<BluetoothDevice> listDevices) {
            super.onPostExecute(listDevices);
            if (listDevices.size() > 0) {
                MyAdapter adapter = (MyAdapter) jlvDispositivos.getAdapter();
                adapter.replaceItems(listDevices);
            } else {
                msg("No paired devices found, please pair your serial BT device and try again");
            }
        }

    }

    private static class MyAdapter extends ArrayAdapter<BluetoothDevice> {
        private int selectedIndex;
        private final Context context;
        private final int selectedColor = Color.parseColor("#abcdef");
        private List<BluetoothDevice> myList;

        public MyAdapter(Context ctx, int resource, int textViewResourceId, List<BluetoothDevice> objects) {
            super(ctx, resource, textViewResourceId, objects);
            context = ctx;
            myList = objects;
            selectedIndex = -1;
        }

        public void setSelectedIndex(int position) {
            selectedIndex = position;
            notifyDataSetChanged();
        }

        public BluetoothDevice getSelectedItem() {
            return myList.get(selectedIndex);
        }

        @Override
        public int getCount() {
            return myList.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return myList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private static class ViewHolder {
            TextView tv;
        }

        public void replaceItems(List<BluetoothDevice> list) {
            myList = list;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            ViewHolder holder;
            if (convertView == null) {
                vi = LayoutInflater.from(context).inflate(R.layout.list_item, null);
                holder = new ViewHolder();

                holder.tv = vi.findViewById(R.id.lstContent);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            if (selectedIndex != -1 && position == selectedIndex) {
                holder.tv.setBackgroundColor(selectedColor);
            } else {
                holder.tv.setBackgroundColor(Color.WHITE);
            }
            BluetoothDevice device = myList.get(position);
            holder.tv.setText(device.getName() + "\n " + device.getAddress());

            return vi;
        }
    }
}