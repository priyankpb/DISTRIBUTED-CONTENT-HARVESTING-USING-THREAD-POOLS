/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.wireformat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author YANK
 */
public class TaskHandoff {
    
    private byte type;
    private String info;
    private String rootURL;
    private byte[] data;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public byte getType() {
        return this.type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getRootURL() {
        return this.rootURL;
    }

    public void setRootURL(String rootURL) {
        this.rootURL = rootURL;
    }
    
    public TaskHandoff (){
        
    }
    
    public TaskHandoff(byte[] data) throws IOException{
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        
        this.type = din.readByte();
        
        int taskLength = din.readInt();
        byte[] temp = new byte[taskLength];
        din.readFully(temp, 0, taskLength);
        this.info = new String(temp);
        
        int rootLength = din.readInt();
        byte[] temp2 = new byte[rootLength];
        din.readFully(temp2, 0, rootLength);
        this.rootURL = new String(temp2);
        
        baInputStream.close();
        din.close();
    }
    
    public byte[] getByte() throws IOException {
        // TODO Auto-generated method stub
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        
        dout.write(getType());
        
        dout.writeInt(info.length());
        dout.write(info.getBytes());
        
        dout.writeInt(rootURL.length());
        dout.write(rootURL.getBytes());
        
        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }
    
    
    
}
