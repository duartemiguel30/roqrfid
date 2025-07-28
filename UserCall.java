import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import javax.swing.JOptionPane;
import java.text.SimpleDateFormat;
import java.util.Date;


    public class UserCall {

        // atual 
        public static final String ipReader = "10.0.7.10";
        public static final int portReader = 7086;

        public static final String Comcon = "COM4";

        public static Pointer handleReader = null; 

        public static boolean TcpInit() {
        PointerByReference handleRef = new PointerByReference();

        int result = RFIDLibrary.INSTANCE.SAAT_TCPInit(handleRef, ipReader, portReader);
        System.out.println("SAAT_TCPInit result: " + result);

        if (result == 1) {
            handleReader = handleRef.getValue();
            System.out.println("TCP Ligação estabelecida. Handle: " + handleReader);
        } else {
            handleReader = null;
            System.out.println("TCP Ligação falhou.");
        }

        return result == 1;
    }

        public static boolean ComInit() {
        PointerByReference handleRef = new PointerByReference();

        int result = RFIDLibrary.INSTANCE.SAAT_COMInit(handleRef, 0x00, Comcon, 19200);
        if (result == 1) {
            handleReader = handleRef.getValue();
        }

        return result == 1;
    }

        public static boolean OpenReader() {
        if (handleReader == null) {
            System.err.println("Handle é nulo antes do SAAT_Open.");
            return false;
        }

        int result = RFIDLibrary.INSTANCE.SAAT_Open(handleReader);
        System.out.println("Resultado de SAAT_Open: " + result);

        if (result != 1) {
            handleReader = null;  
            return false;
        }
        return true;
    }

    public static int PowerOff() {
    if (handleReader == null) {
        return 0; // ou -1, se quiseres indicar erro
    }

    return RFIDLibrary.INSTANCE.SAAT_PowerOff(handleReader);
    }

    public static void log(String mensagem) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("[" + timestamp + "] " + mensagem);
    }



        public static boolean CloseConnection() {
        if (handleReader == null) {
            return false;
        }

        int result = RFIDLibrary.INSTANCE.SAAT_Close(handleReader);
        handleReader = null;
        return result == 1;
    }


        public static boolean ReadEpc() {
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        int result = RFIDLibrary.INSTANCE.SAAT_6CReadEPCCode(handleReader, 0, 1, 0);
        log("📡 Resultado SAAT_6CReadEPCCode: " + result);

        if (result != 1) {
            byte[] errMsg = new byte[256];
            boolean gotMsg = RFIDLibrary.INSTANCE.SAAT_GetErrorMessage(handleReader, errMsg, errMsg.length);
            log("❌ Erro ao ler EPC. Código retorno: " + result);
            if (gotMsg) log("🪪 Descrição: " + new String(errMsg).trim());
            return false;
        }
        return true;
    }

    


        public static void SetupAntenna() {
        RFIDLibrary.INSTANCE.SAAT_SetAntennaPortEnable(handleReader, (byte) 1, (byte) 1); // ativa antena 1
        RFIDLibrary.INSTANCE.SAAT_SetAntennaPortEnable(handleReader, (byte) 1, (byte) 2);
        RFIDLibrary.INSTANCE.SAAT_SetAntennaPortEnable(handleReader, (byte) 1, (byte) 3); 
        RFIDLibrary.INSTANCE.SAAT_SetAntennaPortEnable(handleReader, (byte) 1, (byte) 4); 


        RFIDLibrary.INSTANCE.SAAT_SetAntennaPower(handleReader, (byte) 1, (byte) 30);     // potência máxima 
    }

     public static String RecEpcMsgAsString() {
        Memory data = new Memory(256);
        IntByReference dLen = new IntByReference(32);
        IntByReference nAnt = new IntByReference(1);

        int result = RFIDLibrary.INSTANCE.SAAT_6CRevEPCMsg(handleReader, nAnt, data, dLen);
        if (result != 0) {
            byte[] bytes = data.getByteArray(0, dLen.getValue());
            StringBuilder tag = new StringBuilder();
            for (byte b : bytes) tag.append(String.format("%02x", b < 0 ? b + 256 : b));
            return tag.toString().toUpperCase();
        }
        return null;
    }

    public static boolean RecEpcMsg() {
        Memory data = new Memory(256);
        IntByReference dLen = new IntByReference(32);
        IntByReference nAnt = new IntByReference(1);

        int result = RFIDLibrary.INSTANCE.SAAT_6CRevEPCMsg(handleReader, nAnt, data, dLen);

        if (result != 0) {
            int len = dLen.getValue();
            byte[] bytes = data.getByteArray(0, len);
            StringBuilder tag = new StringBuilder();
            for (byte b : bytes) {
                tag.append(String.format("%02x", b < 0 ? b + 256 : b));
            }
            System.out.println("Tag EPC: " + tag.toString());
        }

        return result == 1;
    }

    private static byte[] hexStringToBytes(String hex) {
        try {
            int len = hex.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2)
                data[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            return data;
        } catch (Exception e) {
            log("❌ Erro a converter hex para bytes: " + e.getMessage());
            return null;
        }
    }

public static boolean SelectTagByEPC(String epcHex, int startAddress, int matchBits) {
    EnsureReaderIsIdle();

    if (epcHex == null || epcHex.length() % 2 != 0) {
        log("❌ EPC inválido.");
        return false;
    }

    byte[] epcBytes = hexStringToBytes(epcHex);
    if (epcBytes == null) return false;

    int result = RFIDLibrary.INSTANCE.SAAT_6CTagSelect(
        handleReader,
        (byte) 0x01,                 // EPC Data Area
        (byte) startAddress,         // valor fornecido pelo utilizador
        (byte) matchBits,            // valor fornecido pelo utilizador
        epcBytes,
        (byte) epcBytes.length,
        (byte) 0x00,                 // Session
        (byte) 0x01,                 // Assert
        (byte) 0x00                  // Cut
    );

    if (result != 1) {
        log("❌ Falha ao selecionar tag. Código: " + result);
        return false;
    }

    log("✅ Tag selecionada com sucesso.");
    return true;
}

public static String bytesToHex(byte[] bytes, int length) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length && i < bytes.length; i++) {
        sb.append(String.format("%02X", bytes[i]));
    }
    return sb.toString();
}

    public static void EnsureReaderIsIdle() {
        PowerOff();
        try { Thread.sleep(500); } catch (InterruptedException e) {}
    }

    public static boolean WriteEPC(int codeLenWords, String newEpcHex, String accessPwdHex) {
    log("✏️ A escrever EPC: " + newEpcHex);
    log("🔢 CodeLen (words): " + codeLenWords);
    log("🔐 Password: " + accessPwdHex);

    if (!newEpcHex.matches("^[0-9A-Fa-f]+$")) {
        log("❌ EPC contém caracteres inválidos.");
        return false;
    }

    if (newEpcHex.length() != codeLenWords * 4) {
        log("⚠️ Comprimento do EPC inválido: " + newEpcHex.length() + " dígitos hex. Esperado: " + (codeLenWords * 4));
        // Podes fazer return false aqui se quiseres ser estrito
    }

    byte[] epcBytes = hexStringToBytes(newEpcHex);
    byte[] pwdBytes = hexStringToBytes(accessPwdHex);

    if (epcBytes == null || pwdBytes == null || pwdBytes.length != 4) {
        log("❌ Erro na conversão de hex para bytes.");
        return false;
    }

    log("📦 EPC Bytes [" + epcBytes.length + "]: " + bytesToHex(epcBytes));
    log("🔑 Password Bytes [" + pwdBytes.length + "]: " + bytesToHex(pwdBytes));

    // ❗️ NÃO limpar seleção nem fazer ReadEpc()
    log("🛑 Parar leitura ativa com PowerOff...");
    PowerOff();
    try { Thread.sleep(300); } catch (InterruptedException e) {}

    SetupAntenna();
    try { Thread.sleep(200); } catch (InterruptedException e) {}

    log("🚀 Enviando SAAT_6CWriteEPCCode...");

    int result = RFIDLibrary.INSTANCE.SAAT_6CWriteEPCCode(
        handleReader,
        (byte) 0x01,             // Antena 1
        (byte) 0x00,             // Modo normal
        pwdBytes,                // Password (4 bytes)
        epcBytes,                // Novo EPC
        (byte) epcBytes.length   // Número de bytes do EPC
    );

    log("📥 Resultado SAAT_6CWriteEPCCode: " + result);

    if (result == 1) {
        log("✅ EPC escrito com sucesso.");
        return true;
    } else {
        byte[] errMsg = new byte[256];
        if (RFIDLibrary.INSTANCE.SAAT_GetErrorMessage(handleReader, errMsg, errMsg.length)) {
            log("🪪 Erro do leitor: " + new String(errMsg).trim());
        } else {
            log("⚠️ Erro desconhecido.");
        }
        return false;
    }
}

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }}

