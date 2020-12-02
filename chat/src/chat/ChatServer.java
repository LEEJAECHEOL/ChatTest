package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import com.google.gson.Gson;

import protocol.Chat;
import protocol.ChatDto;

public class ChatServer {
	
	private static final String TAG = "ChatServer : ";
	private ServerSocket serverSocket; 
	private Vector<ClientInfo> vc;	// 연결된 클라이언트 클래스(소켓)을 담는 컬렉션

	int num = 1;
	
	public ChatServer() {
		try {
			vc = new Vector<>();
			serverSocket = new ServerSocket(10000);
			System.out.println(TAG + "Client Connect Waiting....");
			// 메인쓰레드의 역할
			while(true) {
				Socket socket = serverSocket.accept();
				System.out.println("Client Connect!!");
				ClientInfo clientInfo = new ClientInfo(socket);
				clientInfo.start();
				vc.add(clientInfo);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	class ClientInfo extends Thread {
		
		private Socket socket;
		private String id;
		private BufferedReader reader;
		private PrintWriter writer;	// BufferedWriter()함수와 다른점 : 내려쓰기 함수 지원
		
		
		public ClientInfo(Socket socket) {
			this.socket = socket;
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream(), true);
			} catch (Exception e) {
				System.out.println(TAG + "Server Connect Fail : " + e.getMessage());
			}
		}
		// 역할 : 클라이언트로부터 받은 메시지를 모든 클라이언트에게 재전송
		@Override
		public void run() {
			try {
				String input = null;
				while((input = reader.readLine()) != null) {
					for (int i = 0; i < vc.size(); i++) {
						Gson gson = new Gson();
						ChatDto dto = gson.fromJson(input, ChatDto.class);
						if(dto.getGubun().equals(Chat.MSG)) {
							// 자신빼고 모든 대화방에 뿌리기
							if(vc.get(i) != this) {
								String tempMsg = dto.getMessage();
								vc.get(i).writer.println("[" + id + "] " + tempMsg);
							}
						} else if(dto.getGubun().equals(Chat.ID)) {
							id = dto.getMessage();
							vc.get(i).writer.println("[" + id + "] 님께서 입장하셨습니다.");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		new ChatServer();
	}

}
