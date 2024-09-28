package server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MailServerUDP {

    private static final String MAIL_DIR = "src/mail_accounts/";
    private static final int PORT = 8080;

    private static String createNewAccount(String accountName, String password) throws IOException {
        File accountDir = new File(MAIL_DIR + accountName);
        System.out.println("Account directory path: " + accountDir.getAbsolutePath());
        if (accountDir.exists()) {
            return "Account already exists!";
        } else {
            accountDir.mkdirs();
            File welcomeFile = new File(accountDir, "new_email.txt");
            try (PrintWriter writer = new PrintWriter(welcomeFile)) {
                writer.println("Thank you for using this service. We hope that you will feel comfortable...");
                System.out.println("Created welcome file successfully for account: " + accountName);
            }
            return "Account created";
        }
    }

    private static String receiveEmail(String sender, String recipient, DatagramSocket socket) throws IOException {
        File accountDir = new File(MAIL_DIR + recipient);
        if (!accountDir.exists()) {
            return "Recipient account does not exist";
        }

        // Nhận nội dung email từ client
        byte[] buffer = new byte[1024];
        DatagramPacket emailPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(emailPacket);

        String emailContent = new String(emailPacket.getData(), 0, emailPacket.getLength());

        // Tạo file mới với tên chứa timestamp để lưu email
        File emailFile = new File(accountDir, System.currentTimeMillis() + ".txt");
        try (PrintWriter writer = new PrintWriter(emailFile)) {
            writer.println("From: " + sender);
            writer.println("Content: ");
            writer.println(emailContent);
        }
        return "Email sent successfully";
    }

    private static String sendEmailList(String accountName) throws IOException {
        File accountDir = new File(MAIL_DIR + accountName);
        if (!accountDir.exists()) {
            return "Account does not exist";
        }

        File[] emails = accountDir.listFiles();
        if (emails == null || emails.length == 0) {
            return "No emails found";
        }

        StringBuilder response = new StringBuilder();
        for (File email : emails) {
            response.append("-----\n");
            response.append(new String(Files.readAllBytes(Paths.get(email.getPath()))));
            response.append("\n");
        }
        return response.toString();
    }

    private static String viewEmailsFromSender(String accountName, String sender) throws IOException {
        File accountDir = new File(MAIL_DIR + accountName);
        if (!accountDir.exists()) {
            return "Account does not exist";
        }

        File[] emails = accountDir.listFiles();
        if (emails == null || emails.length == 0) {
            return "No emails found";
        }

        StringBuilder response = new StringBuilder();
        for (File email : emails) {
            List<String> lines = Files.readAllLines(Paths.get(email.getPath()));
            if (!lines.isEmpty() && lines.get(0).contains("From: " + sender)) {
                response.append("-----\n");
                response.append(String.join("\n", lines));
                response.append("\n");
            }
        }

        return response.length() > 0 ? response.toString() : "No emails found from " + sender;
    }

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Mail Server is running on port " + PORT);

            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String request = new String(packet.getData(), 0, packet.getLength());
                String[] parts = request.split(" ", 3);

                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                String response;
                if (parts[0].equals("NEW_ACCOUNT")) {
                    response = createNewAccount(parts[1], parts[2]);
                } else if (parts[0].equals("SEND_EMAIL")) {
                    String sender = parts[1];
                    String recipient = parts[2];
                    response = receiveEmail(sender, recipient, socket);
                } else if (parts[0].equals("LOGIN")) {
                    response = sendEmailList(parts[1]);
                } else if (parts[0].equals("VIEW_EMAIL_FROM_SENDER")) {
                    String accountName = parts[1];
                    String sender = parts[2];
                    response = viewEmailsFromSender(accountName, sender);
                } else {
                    response = "Invalid command";
                }

                byte[] responseData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                socket.send(responsePacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
