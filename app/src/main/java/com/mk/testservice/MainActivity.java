package com.mk.testservice;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.mk.testservice.utils.Base64Utils;
import com.mk.testservice.utils.DateUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Logger;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v24.datatype.ED;
import ca.uhn.hl7v2.model.v24.message.ORU_R01;
import ca.uhn.hl7v2.model.v24.message.QRY_Q01;
import ca.uhn.hl7v2.model.v24.segment.MSA;
import ca.uhn.hl7v2.model.v24.segment.MSH;
import ca.uhn.hl7v2.model.v24.segment.OBX;
import ca.uhn.hl7v2.model.v24.segment.QRD;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;

import static com.mk.testservice.MessageQryQ01.parserMessage;

public class MainActivity extends AppCompatActivity {

    private TextView mTvInfo, mTvGetData;

    private ServerSocket mServerSocket = null;
    private Socket socket = null;
    private InputStream mInputSteam;
    private String ip;
    private int port;
    private ImageView imageView;

    StringBuffer mStringBuffer = new StringBuffer();
    private static final char END_OF_BLOCK = '\u001c';
    private static final char START_OF_BLOCK = '\u000b';
    private static final char CARRIAGE_RETURN = 13; //"\r"

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //本地ip和port信息
                    mTvInfo.setText(msg.obj.toString());
                    break;
                case 2:
                    //从客户端获取到的消息
                    mTvGetData.setText("CustomInfo:" + msg.obj.toString());

                    break;

                case 3:
                    String imgBase = msg.obj.toString();
                    Bitmap bitmap = Base64Utils.base64ToBitmap(imgBase);
                    imageView.setImageBitmap(bitmap);
                    break;

            }

        }
    };

    //查询消息需要上传的数据
    String deviceId = "", patientName = "张三", patientNo = "", messageNo = "";
    //服务器根据查询条件返回的对应数据
    String patientSex = "M", checkTime = DateUtils.messageTime(), patientBird = "20200110";

    private void parserReceiveMessage(String message) {
        if (message.isEmpty()) {
            return;
        }
        Log.d("aaaaaaaaaaaaaaaa", message);

        HapiContext hapiContext = new DefaultHapiContext();

        ca.uhn.hl7v2.model.Message msg = null;
        try {
            msg = hapiContext.getGenericParser().parse(message);
        } catch (HL7Exception e) {
            e.printStackTrace();
        }
        Class<? extends ca.uhn.hl7v2.model.Message> aClass = msg.getClass();

        try {
            Log.d("aaaaaaaaaaaaaaaa", "response");
      switch (aClass.getSimpleName()) {
                case "QRY_Q01":
                    QRY_Q01 qryQ01 = (QRY_Q01) msg;
                    parserQryMsh(qryQ01);
                    parserQryQrd(qryQ01);

                    String dsrQ01 = MessageDsrQ01.createDsrQ01Message(deviceId, DateUtils.messageTime(), messageNo, patientNo,
                            patientName, patientSex, patientBird, checkTime);
                    Log.d("hl7Message", "response qryQ01Msg -> " + dsrQ01);
//                    dsrQ01 = dsrQ01 + END_OF_BLOCK + CARRIAGE_RETURN;
                    new ResponseThread(dsrQ01).start();
                    break;
                case "ORU_R01":
                    ORU_R01 oru_r01 = (ORU_R01) msg;
                    parserOruMsh(oru_r01);
//                    parserOruObx1(oru_r01);
                    parserOruObx2(oru_r01);
                    String oruR01 = MessageACKR01.createACKR01Message(deviceId, DateUtils.messageTime(), messageNo + "");
                    Log.d("aaaaaaaaaaaaaaaa", "ORU_R01");
                    new ResponseThread(oruR01).start();
                    break;
                default:
                    break;
            }
        } catch (HL7Exception e) {
            e.printStackTrace();
        }
    }

//    private void parserOruObx1(ORU_R01 oru_r01) {
//
//
//        OBX obx = oru_r01.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATION(0).getOBX();
//        ca.uhn.hl7v2.model.Message message = obx.getObx5_ObservationValue(0).getMessage();
////        ca.uhn.hl7v2.model.Message message = obx.insertObx5_ObservationValue(0).getMessage();//这个地方 是需要一个 TYPE 的参数
//        ED ed = new ED(message);
//        ED ed1 = new ED(message);
//
//        ED ed2 = (ED) obx.getObservationValue(0).getData();
//
//        String value = ed.getEd4_Encoding().getValue();
//        String value1 = ed.getData().getValue();
//        Log.d("11base64Img", "msg -> "+ value1);
////        Bitmap bitmap = Base64Utils.base64ToBitmap(value1);
////        imageView.setImageBitmap(bitmap );
//
//    }


    private void parserOruObx2(ORU_R01 oru_r01) {


        OBX obx = oru_r01.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATION(1).getOBX();
//        ca.uhn.hl7v2.model.Message message = obx.getObx5_ObservationValue(0).getMessage();
//        ca.uhn.hl7v2.model.Message message = obx.insertObx5_ObservationValue(0).getMessage();//这个地方 是需要一个 TYPE 的参数
        ED ed = (ED) obx.getObservationValue(0).getData();
        String value = ed.getEd4_Encoding().getValue();
        String value1 = ed.getData().getValue();
        Log.d("base64Img", "msg -> " + value1);

        final Bitmap bitmap = Base64Utils.base64ToBitmap(value1);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });


//        Message message = handler.obtainMessage();
//        message.what = 3;
//        message.obj = value1;


    }

    private void parserOruMsh(ORU_R01 oru_r01) {
        MSH qryMsh = oru_r01.getMSH();
        deviceId = qryMsh.getSendingFacility().getNamespaceID().getValue();
        messageNo = qryMsh.getMessageControlID().getValue();
        Log.d("aaaaaaaaaa", "  deviceId -> " + deviceId);
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
                Log.d("aaaaaaaaaaaaaaaa", "wright msg start");
                writer.write(responseData);
                writer.flush();
                Log.d("aaaaaaaaaaaaaaaa", "wright msg end");

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("aaaaaaaaaaaaaaaa", "e -> "+ e);
            }


        }
    }


    private void parserQryMsh(QRY_Q01 qryQ01) {
        MSH qryMsh = qryQ01.getMSH();

        deviceId = qryMsh.getSendingFacility().getNamespaceID().getValue();
        messageNo = qryMsh.getMessageControlID().getValue();
        Log.d("aaaaaaaaaa", "  deviceId -> " + deviceId);
    }

    private void parserQryQrd(QRY_Q01 qryQ01) {
        QRD qryQrd = qryQ01.getQRD();

        patientNo = qryQrd.getQrd4_QueryID().getValue();
//        patientName = qryQrd.getQrd8_WhoSubjectFilter(0).getFamilyName().getSurname().getValue();

        Log.d("aaaaaaaaaa", patientNo + "<- patientNo patientName -> " + patientName);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvInfo = findViewById(R.id.info);
        mTvGetData = findViewById(R.id.data);
        imageView = findViewById(R.id.iv);
        receiveData();
    }


    public void receiveData() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                /*指明服务器端的端口号*/
                try {
                    mServerSocket = new ServerSocket(8000);
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

                    new ServerThread1(socket, mInputSteam).start();

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
        private Socket socket;
        private InputStream inputStream;
        private StringBuffer stringBuffer = mStringBuffer;

        public ServerThread(Socket socket, InputStream inputStream) {
            this.socket = socket;
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            int len;
            byte[] bytes = new byte[24];
            boolean isString = false;

            try {
                if (inputStream != null) {
                    //输入流关闭时循环才会停止
                    //数据读完了，再读是等于0
                    while ((len = inputStream.read(bytes)) != -1) {
                        for (int i = 0; i < len; i++) {
                            if (bytes[i] != '\0') {
                                stringBuffer.append((char) bytes[i]);
                            } else {
                                isString = true;
                                break;
                            }
                        }
                        if (isString) {
                            Message msg = handler.obtainMessage();
                            msg.what = 2;
                            msg.obj = stringBuffer;
                            handler.sendMessage(msg);
                            isString = false;
                        }
                    }
                }
            } catch (IOException e) {
                //当这个异常发生时，说明客户端那边的连接已经断开
                e.printStackTrace();
                try {
                    inputStream.close();
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        }
    }


    class ServerThread1 extends Thread {
        private static final char END_OF_BLOCK = '\u001c';
        private static final char START_OF_BLOCK = '\u000b';
        private static final char CARRIAGE_RETURN = 13;//"\r"
        private static final char NEW_LINE = 10; //"\n"
        private Socket socket;
        private InputStream inputStream;
        private StringBuffer stringBuffer = mStringBuffer;

        private BufferedReader bufferedReader;

        private Boolean flagBase64 = false;

        public ServerThread1(Socket socket, InputStream inputStream) {
            this.socket = socket;
            this.inputStream = inputStream;
        }

        @Override
        public void run() {

            try {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                String line = null;


                while ((line = bufferedReader.readLine()) != null) {
//                    Log.d("serverSocket", "receive msg" + line);
//                    mTvGetData.setText(line);
//                    new ResponseThread("OK").start();

                    if (line.endsWith(String.valueOf(END_OF_BLOCK))) {
                        Log.d("aaaaaaaaaaaaaaaa", "read end");
                        mTvGetData.setText(line);
                        new ResponseThread("OK").start();

//                        Message msg = handler.obtainMessage();
//                        msg.what = 2;
//                        msg.obj = stringBuffer;
//                        handler.sendMessage(msg);
//                        parserReceiveMessage(stringBuffer.toString().replace(String.valueOf(START_OF_BLOCK), ""));
//                        mStringBuffer.setLength(0);
//                        flagBase64 = false;
                    } else if (line.startsWith("OBX|2")) {
                        flagBase64 = true;
                        stringBuffer.append(line).append(NEW_LINE);
                    } else if (flagBase64) {
                        stringBuffer.append(line).append(NEW_LINE);
                    } else {
                        stringBuffer.append(line).append(CARRIAGE_RETURN);
                    }















//                    if (line.endsWith(String.valueOf(END_OF_BLOCK))) {
//                        Log.d("aaaaaaaaaaaaaaaa", "read end");
//                        Message msg = handler.obtainMessage();
//                        msg.what = 2;
//                        msg.obj = stringBuffer;
//                        handler.sendMessage(msg);
//                        parserReceiveMessage(stringBuffer.toString().replace(String.valueOf(START_OF_BLOCK), ""));
//                        mStringBuffer.setLength(0);
//                        flagBase64 = false;
//                    } else if (line.startsWith("OBX|2")) {
//                        flagBase64 = true;
//                        stringBuffer.append(line).append(NEW_LINE);
//                    } else if (flagBase64) {
//                        stringBuffer.append(line).append(NEW_LINE);
//                    } else {
//                        stringBuffer.append(line).append(CARRIAGE_RETURN);
//                    }
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


    /*当按返回键时，关闭相应的socket资源*/
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
