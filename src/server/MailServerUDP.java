package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

            // Tạo file password.txt để lưu mật khẩu
            File passwordFile = new File(accountDir, "password.txt");
            try (PrintWriter writer = new PrintWriter(passwordFile)) {
                writer.println(password);
                System.out.println("Password saved for account: " + accountName);
            }

            File welcomeFile = new File(accountDir, "new_email.txt");
            try (PrintWriter writer = new PrintWriter(welcomeFile)) {
                writer.println("Thank you for using this service. We hope that you will feel comfortable...");
                System.out.println("Created welcome file successfully for account: " + accountName);
            }
            return "Account created";
        }
    }

    private static String receiveEmail(String sender, String recipient, String content) throws IOException {
        File accountDir = new File(MAIL_DIR + recipient);
        if (!accountDir.exists()) {
            return "Recipient account does not exist";
        }

        // Tạo file mới với tên chứa timestamp để lưu email
        File emailFile = new File(accountDir, System.currentTimeMillis() + ".txt");
        try (PrintWriter writer = new PrintWriter(emailFile)) {
            writer.println("From: " + sender);
            writer.println("Content: ");
            writer.println(content);
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

    private static boolean authenticateUser(String email, String password) {
        File accountDir = new File(MAIL_DIR + email);
        File passwordFile = new File(accountDir, "password.txt");

        if (!accountDir.exists() || !passwordFile.exists()) {
            System.out.println("Account directory or password file does not exist.");
            return false; // Tài khoản không tồn tại
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(passwordFile))) {
            String storedPassword = reader.readLine();
            if (storedPassword == null) {
                System.out.println("No password found in the file.");
                return false; // Không có mật khẩu trong tệp
            }
            System.out.println("Stored password: " + storedPassword);
            return storedPassword.trim().equals(password.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Mail Server is running on port " + PORT);

            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String request = new String(packet.getData(), 0, packet.getLength());
                String[] parts = request.split(" ", 4);

                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                String response;
                if (parts[0].equals("NEW_ACCOUNT")) {
                    response = createNewAccount(parts[1], parts[2]);
                } else if (parts[0].equals("SEND_EMAIL")) {
                    String sender = parts[1];
                    String recipient = parts[2];
                    String content = parts[3];
                    response = receiveEmail(sender, recipient, content);
                } else if (parts[0].equals("LOGIN")) {
                    String email = parts[1];
                    String password = parts[2];

                    if (authenticateUser(email, password)) {
                        response = "Login successful";
                    } else {
                        response = "Invalid account or password";
                    }

                } else if (parts[0].equals("CONTENT_EMAIL")) {
                    response = sendEmailList(parts[1]);
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
