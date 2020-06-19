package com.mk.testservice;

import android.util.Log;

import java.util.List;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.v24.message.DSR_Q01;
import ca.uhn.hl7v2.model.v24.segment.DSC;
import ca.uhn.hl7v2.model.v24.segment.DSP;
import ca.uhn.hl7v2.model.v24.segment.MSA;
import ca.uhn.hl7v2.model.v24.segment.MSH;
import ca.uhn.hl7v2.model.v24.segment.QAK;
import ca.uhn.hl7v2.model.v24.segment.QRD;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.Parser;

public class MessageDsrQ01 {

    private static String TAG = "MessageDsrQ01";

    private static final char END_OF_BLOCK = '\u001c';
    private static final char START_OF_BLOCK = '\u000b';
    private static final char CARRIAGE_RETURN = 13;
    /**
     *
     * @param deviceId 设备唯一ID 按查询消息原样返回
     * @param currentTime 当前查询时间，取系统时间
     * @param messageNo  消息ID 由1递增 随查询消息原样返回
     * @param patientNo  患者住院号
     * @param patientName 患者姓名
     * @param patientSex 患者性别 男：M 女：F 其他：0
     * @param patientBird 患者生日
     * @param patientCheckTime 患者检查时间
     * @throws HL7Exception
     */
    public static String createDsrQ01Message(String deviceId, String currentTime, String messageNo,
                                           String patientNo, String patientName, String patientSex, String patientBird, String patientCheckTime)  {

        HapiContext hapiContext = new DefaultHapiContext();
        Parser parser = hapiContext.getPipeParser();

        StringBuffer buffer = new StringBuffer();

        DSR_Q01 dsrQ01 = new DSR_Q01();
        try {
            String msh = setDsrMsh(dsrQ01, deviceId, currentTime, messageNo, parser);
            String msa = setDsrMsa(dsrQ01, messageNo, parser);
            String qak = setDsrQak(dsrQ01, parser);
            String qrd = setDsrQrd(dsrQ01, currentTime, patientNo, parser);
            String dsp = setDsrDsp(dsrQ01, patientNo, patientName, patientSex, patientBird, patientCheckTime, parser);
            setDsrDsc(dsrQ01,parser);

            buffer.append(START_OF_BLOCK)
                    .append(msh)
                    .append(CARRIAGE_RETURN)
                    .append(msa)
                    .append(CARRIAGE_RETURN)
                    .append(qak)
                    .append(CARRIAGE_RETURN)
                    .append(qrd)
                    .append(CARRIAGE_RETURN)
                    .append(dsp)
                    .append(CARRIAGE_RETURN)
                    .append(END_OF_BLOCK)
                    .append(CARRIAGE_RETURN);

        } catch (HL7Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "ENCODE -> " + buffer.toString());
        return buffer.toString();

    }


    private static String setDsrMsh(DSR_Q01 dsrQ01, String deviceId, String currentTime, String messageNo, Parser parser) throws HL7Exception {
        MSH msh = dsrQ01.getMSH();
        msh.getFieldSeparator().setValue("|");
        msh.getEncodingCharacters().setValue("^~\\&");
        msh.getSendingFacility().getNamespaceID().setValue(deviceId);
        msh.getDateTimeOfMessage().getTimeOfAnEvent().setValue(currentTime);
        msh.getMessageType().getMsg1_MessageType().setValue("DSR");
        msh.getMessageType().getMsg2_TriggerEvent().setValue("Q01");
        msh.getMessageControlID().setValue(messageNo);
        msh.getProcessingID().getProcessingID().setValue("P");
        msh.getVersionID().getVersionID().setValue("2.4");
        msh.insertCharacterSet(0).setValue("ASCII");

        MSH qryMshValue = dsrQ01.getMSH();
        String encode = parser.doEncode(qryMshValue, EncodingCharacters.defaultInstance());
        return encode;
    }

    private static String setDsrMsa(DSR_Q01 dsrQ01, String messageNo, Parser parser) throws HL7Exception {
        MSA msa = dsrQ01.getMSA();
        msa.getAcknowledgementCode().setValue("AA");
        msa.getMessageControlID().setValue(messageNo);

        MSA msaValue = dsrQ01.getMSA();
        String encode = parser.doEncode(msaValue, EncodingCharacters.defaultInstance());
        return encode;
    }

    private static String setDsrQak(DSR_Q01 dsrQ01, Parser parser) throws HL7Exception {
        QAK qak = dsrQ01.getQAK();
        qak.getQueryResponseStatus().setValue("OK");

        QAK qakValue = dsrQ01.getQAK();
        String encode = parser.doEncode(qakValue, EncodingCharacters.defaultInstance());
        return encode;
    }


    private static String setDsrQrd(DSR_Q01 dsrQ01, String currentTime, String patientNo, Parser parser) throws HL7Exception {
        QRD qrd = dsrQ01.getQRD();
        qrd.getQrd1_QueryDateTime().getTimeOfAnEvent().setValue(currentTime);
        qrd.getQrd2_QueryFormatCode().setValue("R");
        qrd.getQrd3_QueryPriority().setValue("I");
        qrd.getQrd4_QueryID().setValue(patientNo);
        qrd.getQrd7_QuantityLimitedRequest().getQuantity().setValue("");
        qrd.insertQrd8_WhoSubjectFilter(0).getFamilyName().getSurname().setValue("");
        qrd.insertQrd9_WhatSubjectFilter(0).getIdentifier().setValue("");
        qrd.insertQrd10_WhatDepartmentDataCode(0).getIdentifier().setValue("");
        qrd.getQrd12_QueryResultsLevel().setValue("T");

        QRD qrdValue = dsrQ01.getQRD();
        String encode = parser.doEncode(qrdValue, EncodingCharacters.defaultInstance());
        return encode;
    }

    private static String setDsrDsp(DSR_Q01 dsrQ01, String patientNo, String patientName, String patientSex, String patientBird, String patientCheckTime, Parser parser) throws HL7Exception {

        DSP dsp0 = dsrQ01.getDSP(0);
        DSP dsp1 = dsrQ01.getDSP(1);
        DSP dsp2 = dsrQ01.getDSP(2);
        DSP dsp3 = dsrQ01.getDSP(3);
        DSP dsp4 = dsrQ01.getDSP(4);


        dsp0.getDsp1_SetIDDSP().setValue("1");
        dsp0.getDsp3_DataLine().setValue(patientNo);

        dsp1.getDsp1_SetIDDSP().setValue("2");
        dsp1.getDsp3_DataLine().setValue(patientName);

        dsp2.getDsp1_SetIDDSP().setValue("3");
        dsp2.getDsp3_DataLine().setValue(patientSex);

        dsp3.getDsp1_SetIDDSP().setValue("4");
        dsp3.getDsp3_DataLine().setValue("24");

        dsp4.getDsp1_SetIDDSP().setValue("5");
        dsp4.getDsp3_DataLine().setValue(patientCheckTime);

        List<DSP> dspAll = dsrQ01.getDSPAll();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dspAll.size(); i++) {
            String s = parser.doEncode(dspAll.get(i), EncodingCharacters.defaultInstance());
            builder.append(s).append(CARRIAGE_RETURN);
        }
        return builder.toString();

    }

    private static void setDsrDsc(DSR_Q01 dsrQ01, Parser parser) throws DataTypeException {
        DSC dsc = dsrQ01.getDSC();
        dsc.getDsc1_ContinuationPointer().setValue("");
        dsc.getDsc2_ContinuationStyle().setValue("");

    }
}
