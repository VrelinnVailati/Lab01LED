package com.equipo33CV16.Lab01LED;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    String DIRECCION_DISPOSITIVO = "adfasdfasdf"; //acá hay que poner la dirección MAC de nuestro arduino
    TextView jtvTexto;
    ToggleButton jtbEstado;
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

        jtbEstado.setOnClickListener(v -> {
            textoBoton = jtbEstado.getText().toString();
            if(textoBoton.equals("ON")) {
                jivImagenLed.setImageResource(R.drawable.led_encendido);
            } else {
                jivImagenLed.setImageResource(R.drawable.led_apagado);
            }
        });

        BluetoothAdapter adaptador = BluetoothAdapter.getDefaultAdapter();

        if(adaptador != null) {
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
            }
        } else {
            Toast.makeText(this, "Este dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
            jtbEstado.setEnabled(false);
        }
    }
}