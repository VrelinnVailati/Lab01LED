package com.equipo33CV16.Lab01LED;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    String DIRECCION_DISPOSITIVO = "adfasdfasdf"; //acá hay que poner la dirección MAC de nuestro arduino
    TextView jtvTexto;
    ToggleButton jtbEstado;
    Button jbnVerificarConexion;
    ImageView jivImagenLed;
    BluetoothDevice arduino;
    String textoBoton;
    boolean arduinoEncontrado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jtvTexto = findViewById(R.id.xtvTexto);
        jtbEstado = findViewById(R.id.xtbEstado);
        jivImagenLed = findViewById(R.id.xivImagenLed);
        jbnVerificarConexion = findViewById(R.id.xbnVerificarConexion);

        jtbEstado.setOnClickListener(v -> {
            textoBoton = jtbEstado.getText().toString();
            if(textoBoton.equals("ON")) {
                jivImagenLed.setImageResource(R.drawable.led_encendido);
            } else {
                jivImagenLed.setImageResource(R.drawable.led_apagado);
            }
        });

        BluetoothAdapter adaptador = BluetoothAdapter.getDefaultAdapter();

        jbnVerificarConexion.setOnClickListener(v -> {
            try {
                verificarConexion(adaptador);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        if(adaptador != null) {
            try {
                verificarConexion(adaptador);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Este dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
            jtbEstado.setEnabled(false);
        }
    }

    private void verificarConexion(BluetoothAdapter adaptador) throws IOException {
        BluetoothSocket socket;

        if(!adaptador.isEnabled()) {
            Intent encenderBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(encenderBluetooth, 0);
        }

        Set<android.bluetooth.BluetoothDevice> dispositivosEmparejados = adaptador.getBondedDevices();

        if(dispositivosEmparejados.isEmpty()) {
            Toast.makeText(this, "Empareje el arduino", Toast.LENGTH_SHORT).show();
        } else {
            for(BluetoothDevice dispositivoEmparejado : dispositivosEmparejados) {
                if(dispositivoEmparejado.getAddress().equals(DIRECCION_DISPOSITIVO)) {
                    arduino = dispositivoEmparejado;
                    arduinoEncontrado = true;
                    break;
                }
            }

            jtvTexto.setText(arduinoEncontrado ?
                    "El arduino está listo, presione el botón para apagar o prender el LED" :
                    "No se encontró el arduino, verifique que está conectado  y presione el botón de nuevo");

            //Desactivar esta linea para probar si funciona el cambio de imagen.
            //Sí funciona
            jtbEstado.setEnabled(arduinoEncontrado);

            socket = arduino.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket.connect();
        }
    }
}