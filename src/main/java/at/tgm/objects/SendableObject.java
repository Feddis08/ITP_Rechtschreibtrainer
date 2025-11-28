package at.tgm.objects;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class SendableObject {

    private static final Kryo kryo = createKryo();

    private static Kryo createKryo() {
        Kryo k = new Kryo();
        k.setRegistrationRequired(false);
        k.setReferences(true);
        return k;
    }

    // ======================================================
    // ENCODE → schreibt sich selbst in einen DataOutputStream
    // ======================================================
    public final void encode(DataOutputStream out) throws IOException {
        // Erst in Byte[] serialisieren
        Output kryoOut = new Output(256, -1);
        kryo.writeClassAndObject(kryoOut, this);
        byte[] data = kryoOut.toBytes();

        // Länge + Daten schreiben
        out.writeInt(data.length);
        out.write(data);
    }

    // ======================================================
    // DECODE → liest sich selbst aus einem DataInputStream
    // ======================================================
    @SuppressWarnings("unchecked")
    public static <T extends SendableObject> T decode(DataInputStream in) throws IOException {
        // Länge lesen
        int len = in.readInt();

        // Bytearray lesen
        byte[] data = new byte[len];
        in.readFully(data);

        // Kryo: Byte[] -> Objekt
        Input kryoIn = new Input(data);
        return (T) kryo.readClassAndObject(kryoIn);
    }
}
