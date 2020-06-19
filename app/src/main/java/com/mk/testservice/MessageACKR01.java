package com.mk.testservice;

import android.util.Log;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v24.message.ACK;
import ca.uhn.hl7v2.model.v24.segment.MSA;
import ca.uhn.hl7v2.model.v24.segment.MSH;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.Parser;

public class MessageACKR01 {
    private static String TAG = "MessageACKR01";
    private static final char END_OF_BLOCK = '\u001c';
    private static final char START_OF_BLOCK = '\u000b';
    private static final char CARRIAGE_RETURN = 13;
    /**
     *
     * @param deviceId 设备唯一ID编号
     * @param time 消息时间 取系统时间
     * @param messageId 消息ID 数  由1 递增
     * @throws HL7Exception
     */
    public static String createACKR01Message(String deviceId, String time, String messageId) throws HL7Exception {
        HapiContext hapiContext = new DefaultHapiContext();
        StringBuffer buffer = new StringBuffer();

        Parser parser = hapiContext.getPipeParser();
        ACK ackR01 = new ACK();
        String msh = setACKMsa(ackR01, deviceId, time, messageId, parser);
        String msa = setACKMsh(ackR01, messageId, parser);
        buffer.append(START_OF_BLOCK)
                .append(msh)
                .append(CARRIAGE_RETURN)
                .append(msa)
                .append(CARRIAGE_RETURN)
                .append(END_OF_BLOCK)
                .append(CARRIAGE_RETURN);
        return buffer.toString();
    }

    private static String setACKMsa(ACK ackR01, String deviceId, String time, String messageId, Parser parser) throws HL7Exception {
        MSH qryMsh = ackR01.getMSH();
        qryMsh.getFieldSeparator().setValue("|");
        qryMsh.getEncodingCharacters().setValue("^~\\&");
        qryMsh.getSendingFacility().getNamespaceID().setValue(deviceId);
        qryMsh.getDateTimeOfMessage().getTimeOfAnEvent().setValue(time);
        qryMsh.getMessageType().getMsg1_MessageType().setValue("ACK");
        qryMsh.getMessageType().getMsg2_TriggerEvent().setValue("R01");
        qryMsh.getMessageControlID().setValue(messageId);
        qryMsh.getProcessingID().getProcessingID().setValue("P");
        qryMsh.getVersionID().getVersionID().setValue("2.4");
        qryMsh.insertCharacterSet(0).setValue("ASCII");
        MSH msh = ackR01.getMSH();
        return parser.doEncode(msh, EncodingCharacters.defaultInstance());
    }

    private static String setACKMsh(ACK ackR01, String messageId, Parser parser) throws HL7Exception {
        MSA msa = ackR01.getMSA();
        msa.getAcknowledgementCode().setValue("AA");
        msa.getMessageControlID().setValue(messageId);
        msa.getErrorCondition().getIdentifier().setValue("0");
        MSA msaValue = ackR01.getMSA();
        return parser.doEncode(msaValue, EncodingCharacters.defaultInstance());
    }
}
