package dk.dtu.sensible.economicsgames.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

public class SerializationUtils {
	
	public static byte[] serialize(Object o) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
		  out = new ObjectOutputStream(bos);   
		  out.writeObject(o);
		  return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		  try {
			out.close();
			bos.close();
		  } catch (IOException e) {}
		}
		return null;
	}

	public static Object deserialize(byte[] bytes) {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		try {
		  in = new ObjectInputStream(bis);
		  return in.readObject(); 
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
		  try {
			bis.close();
			in.close();
		  } catch (IOException e) {}
		}
		return null;
	}
}
