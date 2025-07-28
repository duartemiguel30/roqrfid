public class RFIDReset {
    public static void main(String[] args) {
        System.out.println("A tentar resetar sessão ativa...");

        if (UserCall.TcpInit()) {
            boolean fechado = UserCall.CloseConnection();
            if (fechado) {
                System.out.println("Ligação encerrada com sucesso.");
            } else {
                System.err.println("Falha ao encerrar ligação.");
            }
        } else {
            System.err.println("Falha ao estabelecer ligação.");
        }
    }
}
