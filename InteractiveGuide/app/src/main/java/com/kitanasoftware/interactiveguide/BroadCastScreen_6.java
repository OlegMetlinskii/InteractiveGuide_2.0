package com.kitanasoftware.interactiveguide;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;

public class    BroadCastScreen_6 extends AppCompatActivity {




    public static DatagramSocket socket;
    public byte[] buffer;

    private int VOICE_STREAM_PORT = 50005;
    private int BROADCAST_IP_PORT = 36367;



    //Audio Configuration.
    private int RECORDER_SAMPLE_RATE = 44100;
    @SuppressWarnings("deprecation")
    private int RECORDER_CHANNELS = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    private int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private boolean status_sending = false;
    private boolean status_receiving = true;

    ArrayList<String> devices = new ArrayList<String>();
    ArrayList<AudioRecord> recorders = new ArrayList<AudioRecord>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.broad_cast_screen_6);

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#f8bfd8"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);



    }

    public void startClick(final View v) {

        v.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                v.setVisibility(View.VISIBLE);
                sendStream();
            }
        }, 50);
    }


    public void sendStream(){
        status_sending = true;
        for (int i=0; i<devices.size(); i++){
            String device_ip = devices.get(i);
            sendStream(device_ip);
        }
    }


    //Send voice stream to a specific device on the network.
    public void sendStream(final String device_ip) {


        Thread sendStreamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    int MIN_BUFFER_SIZE = 8192;

                    @SuppressWarnings("resource")
                    //Socket Created
                            DatagramSocket socket = new DatagramSocket();

                    byte[] buffer = new byte[MIN_BUFFER_SIZE];

                    DatagramPacket packet;

                    //Destination Address retrieved.
                    final InetAddress destination = InetAddress.getByName(device_ip);

                    //Initialize Recorder
                    AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                            RECORDER_SAMPLE_RATE,
                            RECORDER_CHANNELS,
                            RECORDER_AUDIO_ENCODING,
                            MIN_BUFFER_SIZE * 5);

                    //add to array
                    recorders.add(recorder);
                    recorder.startRecording();

                    while (status_sending == true) {

                        //reading data from MIC into buffer
                        MIN_BUFFER_SIZE = recorder.read(buffer, 0, buffer.length);

                        //putting buffer in the packet
                        packet = new DatagramPacket (buffer, buffer.length, destination, VOICE_STREAM_PORT);

                        //Send packet
                        socket.send(packet);
                    }

                } catch(UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        //socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        });
        sendStreamThread.start();
    }
}
