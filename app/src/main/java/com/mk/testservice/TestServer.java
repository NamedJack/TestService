package com.mk.testservice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class TestServer extends AppCompatActivity {
    TextView tv, ipTv;
    ServerSocket mServerSocket;
    private String ip;
    private int port;
    private Socket socket = null;
    private InputStream mInputSteam;
    StringBuffer mStringBuffer = new StringBuffer();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    ipTv.setText(msg.obj.toString());
                    break;
                case 2:
                    tv.setText(msg.obj.toString());
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_server);
        tv = findViewById(R.id.tv);
        ipTv = findViewById(R.id.ip);
        receiveData();
    }

    public void receiveData() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                /*指明服务器端的端口号*/
                try {
                    mServerSocket = new ServerSocket(8001);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                getLocalIpAddress(mServerSocket);

                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = "IP:" + ip + " , PORT: " + port;
                handler.sendMessage(msg);

                //持续获取消息
                while (true) {

                    try {
                        if (mServerSocket != null) {
                            socket = mServerSocket.accept();
                            mInputSteam = socket.getInputStream();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    new ServerThread(socket, mInputSteam).start();

                    new ServerThread(socket, mInputSteam).start();

                }
            }
        };
        thread.start();
    }


    private void getLocalIpAddress(ServerSocket serverSocket) {
        try {
            //for(;;)
            for (Enumeration<NetworkInterface> mEnumNetwork = NetworkInterface.getNetworkInterfaces(); mEnumNetwork.hasMoreElements(); ) {
                NetworkInterface mNetwork = mEnumNetwork.nextElement();
                for (Enumeration<InetAddress> mEnumIpAddress = mNetwork.getInetAddresses(); mEnumIpAddress.hasMoreElements(); ) {
                    InetAddress mIpAddress = mEnumIpAddress.nextElement();
                    String mIp = mIpAddress.getHostAddress().substring(0, 3);
                    if (mIp.equals("192")) {
                        ip = mIpAddress.getHostAddress();    //获取本地IP
                        port = serverSocket.getLocalPort();    //获取本地的PORT
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    class ServerThread extends Thread {
        private static final char END_OF_BLOCK = '\u001c';
        private static final char START_OF_BLOCK = '\u000b';
        private static final char CARRIAGE_RETURN = 13;//"\r"
        private static final char NEW_LINE = 10; //"\n"
        private Socket socket;
        private InputStream inputStream;
        private StringBuffer stringBuffer = mStringBuffer;

        private BufferedReader bufferedReader;

        private Boolean flagBase64 = false;

        public ServerThread(Socket socket, InputStream inputStream) {
            this.socket = socket;
            this.inputStream = inputStream;
        }

        @Override
        public void run() {

            try {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                String line = null;


                while ((line = bufferedReader.readLine()) != null) {

                    if (line.endsWith(String.valueOf(END_OF_BLOCK))) {
                        Log.d("aaaaaaaaaaaaaaaa", "read end");
                        Message msg = handler.obtainMessage();
                        msg.what = 2;
                        msg.obj = stringBuffer;
                        handler.sendMessage(msg);
//                        parserReceiveMessage(stringBuffer.toString().replace(String.valueOf(START_OF_BLOCK), ""));
                        mStringBuffer.setLength(0);
                        new ResponseThread("AAA"+END_OF_BLOCK).start();
                        flagBase64 = false;
                    }  else {
                        stringBuffer.append(line).append(CARRIAGE_RETURN);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    inputStream.close();
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }


        }
    }



    class ResponseThread extends Thread {
        String responseData = null;

        public ResponseThread(String dsrQ01) {
            this.responseData = dsrQ01;
        }

        @Override
        public void run() {
            super.run();
            try {
                OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                if (responseData == null) {
                    return;
                }
                Log.d("aaaaaaaaaaaaaaaa", "right msg start");
                writer.write(responseData);
                writer.flush();
                Log.d("aaaaaaaaaaaaaaaa", "right msg end");

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("aaaaaaaaaaaaaaaa", "e -> "+ e);
            }


        }
    }
}
