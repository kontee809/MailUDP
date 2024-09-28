package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class MailClientUDP {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            Scanner scanner = new Scanner(System.in);
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);

            System.out.println("Welcome to Mail Client");

            boolean isLoggedIn = false;
            String loggedInAccount = "";

            // Đăng nhập hoặc tạo tài khoản cho đến khi thành công
            while (!isLoggedIn) {
                System.out.println("1. Create new account");
                System.out.println("2. Login");
                int choice = scanner.nextInt();
                scanner.nextLine();

                String command = "";
                switch (choice) {
                    case 1:
                        System.out.println("Enter new account name: ");
                        String accountName = scanner.nextLine();
                        command = "NEW_ACCOUNT " + accountName;
                        break;
                    case 2:
                        System.out.println("Enter account name to login: ");
                        String loginAccount = scanner.nextLine();
                        command = "LOGIN " + loginAccount;
                        break;
                    default:
                        System.out.println("Invalid choice");
                        continue;
                }

                // Gửi lệnh đến server
                byte[] sendBuffer = command.getBytes();
                DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
                socket.send(packet);

                // Nhận phản hồi từ server
                byte[] buffer = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(responsePacket);
                String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                System.out.println(response);

                // Kiểm tra đăng nhập thành công
                if (choice == 2 && !response.equals("Account does not exist")) {
                    isLoggedIn = true;
                    loggedInAccount = command.split(" ")[1];
                }
            }

            // Đăng nhập thành công, cho phép người dùng chọn gửi email hoặc xem danh sách email
            while (true) {
                System.out.println("1. Send email");
                System.out.println("2. View inbox");
                System.out.println("3. View specific email");
                int choice = scanner.nextInt();
                scanner.nextLine();

                String command = "";
                switch (choice) {
                    case 1:
                        System.out.println("Enter recipient account: ");
                        String recipient = scanner.nextLine();
                        System.out.println("Enter email content: ");
                        String emailContent = scanner.nextLine();
                        command = "SEND_EMAIL " + loggedInAccount + " " + recipient;

                        // Gửi lệnh đến server
                        byte[] sendBuffer = command.getBytes();
                        DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
                        socket.send(packet);

                        // Gửi nội dung email đến server
                        byte[] emailBuffer = emailContent.getBytes();
                        DatagramPacket emailPacket = new DatagramPacket(emailBuffer, emailBuffer.length, serverAddress, SERVER_PORT);
                        socket.send(emailPacket);

                        // Nhận phản hồi từ server
                        byte[] buffer = new byte[1024];
                        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                        socket.receive(responsePacket);
                        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                        System.out.println(response);
                        break;

                    case 2:
                        command = "LOGIN " + loggedInAccount;

                        // Gửi lệnh đến server
                        byte[] loginBuffer = command.getBytes();
                        DatagramPacket loginPacket = new DatagramPacket(loginBuffer, loginBuffer.length, serverAddress, SERVER_PORT);
                        socket.send(loginPacket);

                        // Nhận phản hồi từ server
                        byte[] inboxBuffer = new byte[1024];
                        DatagramPacket inboxResponsePacket = new DatagramPacket(inboxBuffer, inboxBuffer.length);
                        socket.receive(inboxResponsePacket);
                        String inboxResponse = new String(inboxResponsePacket.getData(), 0, inboxResponsePacket.getLength());
                        System.out.println("Inbox:\n" + inboxResponse);
                        break;
                    case 3:
                        System.out.println("Enter sender name to view emails from: ");
                        String senderName = scanner.nextLine();
                        command = "VIEW_EMAIL_FROM_SENDER " + loggedInAccount + " " + senderName;

                        // Gửi lệnh đến server
                        byte[] viewBuffer = command.getBytes();
                        DatagramPacket viewPacket = new DatagramPacket(viewBuffer, viewBuffer.length, serverAddress, SERVER_PORT);
                        socket.send(viewPacket);

                        // Nhận phản hồi từ server
                        byte[] emailContentBuffer = new byte[1024];
                        DatagramPacket emailContentPacket = new DatagramPacket(emailContentBuffer, emailContentBuffer.length);
                        socket.receive(emailContentPacket);
                        String emailContentResponse = new String(emailContentPacket.getData(), 0, emailContentPacket.getLength());
                        System.out.println("Emails from " + senderName + ":\n" + emailContentResponse);
                        break;

                    default:
                        System.out.println("Invalid choice");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
