import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import javax.swing.JOptionPane;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;


    public class UserCall {

        // atual 
        public static final String ipReader = "10.0.7.10";
        public static final int portReader = 7086;

        public static final String Comcon = "COM4";

        public static Pointer handleReader = null; 

        
        
        public static boolean TcpInit() {
            PointerByReference handleRef = new PointerByReference();

            int result = RFIDLibrary.INSTANCE.SAAT_TCPInit(handleRef, ipReader, portReader);

            if (result == 1) {
                handleReader = handleRef.getValue();
                System.out.println("Ligação TCP estabelecida");
            } else {
                handleReader = null;
                System.out.println("Ligação TCP falhou.");
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
                    return false;
                }

            int result = RFIDLibrary.INSTANCE.SAAT_Open(handleReader);

                if (result != 1) {
                    handleReader = null;  
                    return false;
                }
                return true;
            }

    public static int PowerOff() {
        if (handleReader == null) {
            return 0; 
        }
        return RFIDLibrary.INSTANCE.SAAT_PowerOff(handleReader);
        }

    public static boolean CloseConnection() {  
        if (handleReader == null) {
            return false;
        }
        
        int result = RFIDLibrary.INSTANCE.SAAT_Close(handleReader);
        handleReader = null;
        return result == 1;
        }   

    public static boolean ClearTagSelection() {
        if (handleReader == null) {
            System.out.println("Handle nulo ao tentar limpar seleção de tag.");
            return false;
        }

        PowerOff();

        try { 
            Thread.sleep(200);
        } catch (InterruptedException e) {}

        byte memoryBank = 0x01;     
        byte startAddress = 0x00;
        byte matchBits = 0x00;
        byte[] matchData = new byte[0];
        byte matchLen = 0x00;
        byte session = 0x00;
        byte assertFlag = 0x00;
        byte cutFlag = 0x00;

        int result = RFIDLibrary.INSTANCE.SAAT_6CTagSelect(
            handleReader,
            memoryBank,
            startAddress,
            matchBits,
            matchData,
            matchLen,
            session,
            assertFlag,
            cutFlag
        );

        if (result == 1) {
            System.out.println("Seleção de tag limpa com sucesso.");
            return true;
        } else {
            System.out.println("Falha ao limpar seleção. Código: " + result);
            byte[] errMsg = new byte[256];
            if (RFIDLibrary.INSTANCE.SAAT_GetErrorMessage(handleReader, errMsg, errMsg.length)) {
                System.out.println("Erro do leitor: " + new String(errMsg).trim());
            }
            return false;
        }
    }

    public static void startContinuousRead() {
        Thread leituraThread = new Thread(() -> {
            System.out.println("Leitura intercalada");

            Set<String> epcsAtuais = new HashSet<>();

            while (!Thread.currentThread().isInterrupted()) {
                int result = RFIDLibrary.INSTANCE.SAAT_6CReadEPCCode(handleReader, 0, 1, 0); // leitura única
                Set<String> epcsNovas = new HashSet<>();

                if (result == 1) {
                    try { 
                        Thread.sleep(200); 
                        } catch (InterruptedException e) { 
                            break; }

                    while (true) {
                        String epc = RecEpcMsgAsString();
                        if (epc == null) break;
                        epcsNovas.add(epc);
                    }

                    System.out.println("\nTags visíveis (" + epcsNovas.size() + "):");
                    for (String tag : epcsNovas) {
                        System.out.println(tag);
                    }

                    System.out.println("Atualizado: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    epcsAtuais = epcsNovas;

                } else {
                    byte[] errMsg = new byte[256];
                    if (RFIDLibrary.INSTANCE.SAAT_GetErrorMessage(handleReader, errMsg, errMsg.length)) {
                        System.out.println("Erro ao ler EPC: " + new String(errMsg).trim());
                    }
                }

                try { Thread.sleep(2_000); } catch (InterruptedException e) { break; }
            }

            System.out.println("Leitura terminada.");
        });

        leituraThread.start();

        JOptionPane.showMessageDialog(null, "Leitura periódica ativa.\nClica OK para parar.");
        leituraThread.interrupt();
        try { leituraThread.join(); } catch (InterruptedException e) {}
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
            return null;
        }
    }

    public static boolean SelectTagByEPC(String epcHex, int startAddress, int matchBits) {
        EnsureReaderIsIdle();

        if (epcHex == null || epcHex.length() % 2 != 0) {
            return false;
        }

        byte[] epcBytes = hexStringToBytes(epcHex);
        if (epcBytes == null) return false;

        int result = RFIDLibrary.INSTANCE.SAAT_6CTagSelect(
            handleReader,
            (byte) 0x01,                
            (byte) startAddress,        
            (byte) matchBits,           
            epcBytes,
            (byte) epcBytes.length,
            (byte) 0x00,                 
            (byte) 0x01,                 
            (byte) 0x00                  
        );

        if (result != 1) {
            System.out.println("Falha: Código:" + result);
            return false;
        }

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
        try { Thread.sleep(500); } 
            catch (InterruptedException e) {}
    }

    public static boolean WriteEPC(int codeLenWords, String newEpcHex, String accessPwdHex) {

        if (!newEpcHex.matches("^[0-9A-Fa-f]+$")) {
            System.out.print("Caracteres inválidos");
            return false;
        }

        if (newEpcHex.length() != codeLenWords * 4) {
            System.out.println("Comprimento do EPC inválido:" + newEpcHex.length() + " dígitos hex. Esperado: " + (codeLenWords * 4));
        }

        byte[] epcBytes = hexStringToBytes(newEpcHex);
        byte[] pwdBytes = hexStringToBytes(accessPwdHex);

        if (epcBytes == null || pwdBytes == null || pwdBytes.length != 4) {
            System.out.println("Erro na conversão.");
            return false;
        }

        PowerOff();
        try { 
            Thread.sleep(300); 
            } 
            catch (InterruptedException e) {}

        SetupAntenna();
        try {
            Thread.sleep(200); }
            catch (InterruptedException e) {}

        int result = RFIDLibrary.INSTANCE.SAAT_6CWriteEPCCode(
            handleReader,
            (byte) 0x01,             // Antena 1
            (byte) 0x00,             // Modo normal
            pwdBytes,                
            epcBytes,                
            (byte) epcBytes.length   
        );


        if (result == 1) {
            System.out.println("EPC escrito com sucesso.");
            return true;
        } else {
            byte[] errMsg = new byte[256];
            if (RFIDLibrary.INSTANCE.SAAT_GetErrorMessage(handleReader, errMsg, errMsg.length)) {
                System.out.println(" Erro do leitor: " + new String(errMsg).trim());
            } else {
                System.out.println("Erro desconhecido.");
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

