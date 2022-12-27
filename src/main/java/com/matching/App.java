package com.matching;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.futronictech.AnsiSDKLib;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        String myFp = "Rk1SACAyMAAAAAE4AAABQAHgAMUAxQEAAQBkL4BNAD2aAEBEAIQcAIBjAKIVAIDoAT2wAICMAT2kAIBVATcfAIDeAMXSAIC2AF3wAECSAQ+pAIBvAE4HAIDWAUqvAIA7AQYvAICfAC35AIB6AVElAECpAUOkAECSAVMhAID1AOfOAEBvAPyuAIA/AV0NAID3AM5YAIBdAVsYAECMASAsAIBTAFycAIDNAVsoAECGAWoVAIDSAOXJAEAvAVAXAIDgAVcvAIDbATwyAIBNAFEYAECIAQkuAIBdAMEhAECXALjhAECMAPqzAIDeASq1AEBxARuvAICSANtmAEBoASwmAECgANjGAID7ALzVAIC+AM1fAICZABiHAECxAHvpAIBjASUuAIBgASqrAED7ARvJAECxAHV5AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        byte[] myFinger = Base64.getDecoder().decode(myFp);
        Connection con;
        Connection logCon;
        Class.forName("oracle.jdbc.OracleDriver");
        con = DriverManager.getConnection(
                "jdbc:oracle:thin:@10.11.201.211:1525/baabsdb", "biotpl", "BIOTPL#2");
        Class.forName("org.postgresql.Driver");
        logCon = DriverManager.getConnection("jdbc:postgresql://localhost:5432/logs", "postgres",
                "1234");

        long count = 0;
        float[] score = new float[1];
        AnsiSDKLib ansiSDKLib = new AnsiSDKLib();
        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery("select * from fp_enroll where standard='S'");
        while (rs.next()) {
            Map<String, byte[]> singleFingerData = new HashMap<>();
            singleFingerData.put("RTHUMB", getByteDataFromBlob(rs.getBlob("RTHUMB")));
            singleFingerData.put("RINDEX", getByteDataFromBlob(rs.getBlob("RINDEX")));
            singleFingerData.put("RMIDDLE", getByteDataFromBlob(rs.getBlob("RMIDDLE")));
            singleFingerData.put("RRING", getByteDataFromBlob(rs.getBlob("RRING")));
            singleFingerData.put("RLITTLE", getByteDataFromBlob(rs.getBlob("RLITTLE")));
            singleFingerData.put("LTHUMB", getByteDataFromBlob(rs.getBlob("LTHUMB")));
            singleFingerData.put("LINDEX", getByteDataFromBlob(rs.getBlob("LINDEX")));
            singleFingerData.put("LMIDDLE", getByteDataFromBlob(rs.getBlob("LMIDDLE")));
            singleFingerData.put("LRING", getByteDataFromBlob(rs.getBlob("LRING")));
            singleFingerData.put("LLITTLE", getByteDataFromBlob(rs.getBlob("LLITTLE")));
            String custFromDB = rs.getString("cust_no");

            for (String randomKey : singleFingerData.keySet()) {
                if (singleFingerData.get(randomKey) != null) {
                    count++;
                    ansiSDKLib.MatchTemplates(myFinger, singleFingerData.get(randomKey),
                            score);

                        System.out.println(count);

                        PreparedStatement pstmt = logCon.prepareStatement(
                                "insert into matching_script_log (with_customer_number, with_customer_finger,score, created_at) values (?, ?, ?, NOW())");

                        pstmt.setString(1, custFromDB);
                        pstmt.setString(2, randomKey);
                        pstmt.setInt(3, (int) score[0]);
                        pstmt.execute();
                        pstmt.close();
                    
                }
            }

        }
        System.out.println("total matches --- " + count);
        rs.close();
        stmt.close();
    }

    public static byte[] getByteDataFromBlob(Blob blob) {
        if (blob != null) {
            try {
                return blob.getBytes(1, (int) blob.length());
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        }
        return null;
    }
}
