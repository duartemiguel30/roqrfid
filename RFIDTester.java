import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class RFIDTester {

    public static void main(String[] args) throws InterruptedException, IOException {

        loadLib();

        if (!UserCall.TcpInit()) {
            JOptionPane.showMessageDialog(null, "Falha ao estabelecer ligação TCP.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!UserCall.OpenReader()) {
            JOptionPane.showMessageDialog(null, "Falha ao abrir leitor RFID.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UserCall.SetupAntenna();

            while (true) {
                String[] opcoes = {"Ler EPC","Selecionar Tag por EPC","Reescrever EPC", "Reset seleção","Sair"};

                int escolha = JOptionPane.showOptionDialog(
                        null, "Escolhe uma opção:", "Menu RFID",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                        null, opcoes, opcoes[0]);

                if (escolha == 0) { //ler tags
                
                    UserCall.startContinuousRead();}

                else if (escolha == 1) { // SelecionarEPC
                    try {
                        String epc = JOptionPane.showInputDialog("EPC a selecionar:");
                        if (epc == null || epc.trim().isEmpty()) return;
                        epc = epc.trim().toUpperCase();

                        if (epc.length() % 2 != 0) {
                            JOptionPane.showMessageDialog(null, "❌ O Match Data (EPC) deve ter número par de caracteres (ex: 24 para 12 bytes)", "Erro", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        String startAddrStr = JOptionPane.showInputDialog("Start Address(0):");
                        if (startAddrStr == null || startAddrStr.trim().isEmpty()) return;
                        int startAddress = Integer.parseInt(startAddrStr.trim());

                        String matchBitsStr = JOptionPane.showInputDialog("Match bits(96):");
                        if (matchBitsStr == null || matchBitsStr.trim().isEmpty()) return;
                        int matchBits = Integer.parseInt(matchBitsStr.trim());

                        boolean ativa = UserCall.SelectTagByEPC(epc, startAddress, matchBits);

                        if (ativa) {
                            JOptionPane.showMessageDialog(null, "✅ Tag selecionada com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "❌ A tag não respondeu à seleção. Verifica os dados inseridos e a posição da tag.", "Erro", JOptionPane.ERROR_MESSAGE);
                        }

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "❌ Erro de entrada: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }

                else if (escolha == 2) { // Reescrever EPC
                    try {
                        String codeLenStr = JOptionPane.showInputDialog("Code Length (nº de words, ex: 6):");
                        if (codeLenStr == null || codeLenStr.trim().isEmpty()) return;
                        int codeLenWords = Integer.parseInt(codeLenStr.trim());

                        String novoEpc = JOptionPane.showInputDialog("Novo EPC (hex):");
                        if (novoEpc == null || novoEpc.trim().isEmpty()) return;
                        novoEpc = novoEpc.trim().toUpperCase();

                        String pwdInput = JOptionPane.showInputDialog("Tag Access Password (8 dígitos hex):");
                        if (pwdInput == null || pwdInput.trim().isEmpty()) return;
                        pwdInput = pwdInput.trim().toUpperCase();

                        boolean sucesso = UserCall.WriteEPC(codeLenWords, novoEpc, pwdInput);
                        JOptionPane.showMessageDialog(null,
                            sucesso ? "✅ EPC reescrito com sucesso." : "❌ Falha ao escrever EPC.",
                            sucesso ? "Sucesso" : "Erro",
                            sucesso ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                        );
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "❌ Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else if (escolha == 3 ) {
                    UserCall.ClearTagSelection();
                }
                else if ( escolha == 4 || escolha == JOptionPane.CLOSED_OPTION ) {
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Encerrando ligação com leitor...");
            if (UserCall.handleReader != null) {
                UserCall.PowerOff();
                UserCall.CloseConnection();
                System.out.println("Ligação encerrada.");
            } else {
                System.out.println("Ligação não foi estabelecida corretamente.");
            }
        }
    }

    public static void loadLib() throws IOException {
        File f = new File("RFIDAPI.dll");
        if (f.exists()) {
            System.load(f.getCanonicalPath());
        } else {
            JFileChooser jfc = new JFileChooser(new File("."));
            jfc.setDialogTitle("Seleciona a DLL RFIDAPI.dll");
            jfc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".dll");
                }

                @Override
                public String getDescription() {
                    return "DLL : Dynamic Link Library";
                }
            });

            if (JFileChooser.APPROVE_OPTION == jfc.showOpenDialog(null)) {
                System.load(jfc.getSelectedFile().getCanonicalPath());
                System.out.println("[INFO] DLL carregada via seleção manual: " + jfc.getSelectedFile().getName());
            } else {
                JOptionPane.showMessageDialog(null, "Nenhuma DLL carregada", "Erro", JOptionPane.ERROR_MESSAGE);
                System.err.println("[ERRO] DLL não carregada.");
            }
        }
    }
}
