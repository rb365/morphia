/**
 *
 */
package com.google.code.morphia.mapping;

import org.bson.types.Binary;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 */
public class Serializer
{
    /** serializes object to byte[] */
    public static byte[] serialize(final Object o, final boolean zip) throws IOException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        if (zip)
        {
            os = new SnappyOutputStream(os);// Snappy! YAY! now we serialize/deserialize 20 times faster!
        }
        final ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(o);
        oos.flush();
        oos.close();

        return baos.toByteArray();
    }

    /** deserializes DBBinary/byte[] to object */
    public static Object deserialize(final Object data, final boolean zipped) throws IOException,
            ClassNotFoundException
    {
        ByteArrayInputStream bais;
        if (data instanceof Binary)
        {
            bais = new ByteArrayInputStream(((Binary) data).getData());
        }
        else
        {
            bais = new ByteArrayInputStream((byte[]) data);
        }

        InputStream is = bais;
        try
        {
            if (zipped)
            {
                is = new SnappyInputStream(is);// Snappy! YAY! now we serialize/deserialize 20 times faster!
            }

            final ObjectInputStream ois = new ObjectInputStream(is);
            return ois.readObject();
        }
        finally
        {
            is.close();
        }
    }

}