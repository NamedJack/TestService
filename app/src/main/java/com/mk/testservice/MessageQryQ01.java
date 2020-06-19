package com.mk.testservice;

import android.util.Log;

import java.io.IOException;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.QRY_Q01;
import ca.uhn.hl7v2.model.v24.segment.DSC;
import ca.uhn.hl7v2.model.v24.segment.MSH;
import ca.uhn.hl7v2.model.v24.segment.QRD;
import ca.uhn.hl7v2.model.v24.segment.QRF;
import ca.uhn.hl7v2.parser.Parser;

/**
 * QRY_Q01  及时消息查询
 */
public class MessageQryQ01 {

    private static String TAG = "MessageQryQ01";

    /**
     * @param deviceId    请求设备唯一ID
     * @param currentTime 查询时间 格式为 yyyyMMddHHmmss
     * @param messageNo   查询消息的唯一编号 由1递增
     * @param patientName 患者姓名
     */
    public static Message createQryQ01Message(String deviceId, String currentTime, String messageNo, String patientNo, String patientName) throws IOException, HL7Exception {

        HapiContext hapiContext = new DefaultHapiContext();
        Parser parser = hapiContext.getPipeParser();

        QRY_Q01 qryQ01 = new QRY_Q01();
        createQryMsh(qryQ01, deviceId, currentTime, messageNo);
        createQryQrd(qryQ01, currentTime, patientNo, patientName);


        Log.d(TAG, "message -> " + parser.encode(qryQ01));
//        return parser.encode(qryQ01);
        return qryQ01;
    }


    private static void createQryMsh(QRY_Q01 qryQ01, String deviceId, String currentTime, String messageNo) throws HL7Exception {
        MSH qryMsh = qryQ01.getMSH();
        qryMsh.getFieldSeparator().setValue("|");
        qryMsh.getEncodingCharacters().setValue("^~\\&");
        qryMsh.getSendingFacility().getNamespaceID().setValue(deviceId);
        qryMsh.getDateTimeOfMessage().getTimeOfAnEvent().setValue(currentTime);
        qryMsh.getMessageType().getMsg1_MessageType().setValue("QRY");
        qryMsh.getMessageType().getMsg2_TriggerEvent().setValue("Q01");
        qryMsh.getMessageControlID().setValue(messageNo);
        qryMsh.getProcessingID().getProcessingID().setValue("P");
        qryMsh.getVersionID().getVersionID().setValue("2.4");
        qryMsh.insertCharacterSet(0).setValue("ASCII");
    }


    private static void createQryQrd(QRY_Q01 qryQ01, String time, String patientNo, String name) throws HL7Exception {
        QRD qryQrd = qryQ01.getQRD();
        qryQrd.getQrd1_QueryDateTime().getTimeOfAnEvent().setValue(time);
        qryQrd.getQrd2_QueryFormatCode().setValue("R");
        qryQrd.getQrd3_QueryPriority().setValue("I");
        qryQrd.getQrd4_QueryID().setValue(patientNo);
        qryQrd.getQrd7_QuantityLimitedRequest().getQuantity().setValue("RD");
//        qryQrd.insertQrd8_WhoSubjectFilter(0).getGivenName().setValue("si");
        qryQrd.insertQrd8_WhoSubjectFilter(0).getFamilyName().getSurname().setValue(name);
        qryQrd.insertQrd9_WhatSubjectFilter(0).getIdentifier().setValue("ORD");
        qryQrd.getQrd12_QueryResultsLevel().setValue("T");
    }

    private static void setQryQrf(QRY_Q01 qryQ01) {
        QRF qryQrf = qryQ01.getQRF();
    }

    private static void setQryDsc(QRY_Q01 qryQ01) {
        DSC qryDsc = qryQ01.getDSC();
    }


    public static void parserMessage(String message) {
        if (message.isEmpty()) {
            return;
        }

        HapiContext hapiContext = new DefaultHapiContext();
        try {
            Message msg = hapiContext.getGenericParser().parse(message);
            QRY_Q01 qryQ01 = (QRY_Q01) msg;
            parserQryMsh(qryQ01);
            parserQryQrd(qryQ01);
        } catch (HL7Exception e) {
            e.printStackTrace();
        }

    }

    private static void parserQryMsh(QRY_Q01 qryQ01) {
        MSH qryMsh = qryQ01.getMSH();
        String deviceId = qryMsh.getSendingFacility().getNamespaceID().getValue();
        Log.d(TAG, "  deviceId -> " + deviceId);
    }

    private static void parserQryQrd(QRY_Q01 qryQ01) {
        QRD qryQrd = qryQ01.getQRD();
//        String code = qryQrd.getQrd2_QueryFormatCode().getValue();

        String patientNo = qryQrd.getQrd4_QueryID().getValue();
        String patientName = qryQrd.getQrd8_WhoSubjectFilter(0).getFamilyName().getSurname().getValue();

        Log.d(TAG, patientName + "<- name no -> " + patientNo);
    }


}
