package socket_project_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.RequiredArgsConstructor;
import socket_project_server.Entity.Room;
import socket_project_server.dto.RequestBodyDto;
import socket_project_server.dto.SendMessage;

@RequiredArgsConstructor
public class ServerReceiver extends Thread {

	private final Socket socket; // RequiredArg
	private Gson gson;
	
	private String username;
	private String roomName;
	
	@Override
	public void run() {
		gson = new Gson();
		while (true) { 
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String requestBody = bufferedReader.readLine();
				requestController(requestBody);
			} catch(SocketException e) {
				return;		//소켓 종료
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void requestController(String requestBody) { // 클래스 안에서만 쓸 것이므로 private
		String resource = gson.fromJson(requestBody, RequestBodyDto.class).getResource();

		switch (resource) {
		case "connection":
			connection(requestBody);
			break;

		case "createRoom":
			createRoom(requestBody);
			break;

		case "join":
			join(requestBody);
			break;

		case "sendMessage":
			sendMessage(requestBody);
			break;
			
		case "quit":
			quit(requestBody);
			break;
			
		case "disconnection":
			disconnection(requestBody);
			break;
		}
	}
	
	private void connection(String requestBody) {
		// 접속 될 때 ServerReceiver마다 username을 부여
		username = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody(); // private 전역 변수에 저장
		// 메소드 분리
		updateRoomList(true);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		updateConnectedUserList();
	}
	
	private void updateConnectedUserList() {
		//첫화면 connectedUserList
		List<String> connectedUserList = new ArrayList<>();
		Server.serverReceiverList.forEach(con -> {
			connectedUserList.add(con.username);
		});
		RequestBodyDto<List<String>> connectedUserListDto = 
				new RequestBodyDto<List<String>>("updateConnectedUserList", connectedUserList);
		Server.serverReceiverList.forEach(con -> {
			ServerSender.getInstance().send(con.socket, connectedUserListDto);
		});
	}
	
	private void updateRoomList(boolean isConnection) {
		List<String> roomNameList = new ArrayList<>();
		Server.roomList.forEach(room -> {
			roomNameList.add(room.getRoomName());
		});
		RequestBodyDto<List<String>> updateRoomListRequestBodyDto = 
				new RequestBodyDto<List<String>>("updateRoomList", roomNameList);
		
		if(isConnection) {	//접속시엔 자기 자신만 업데이트
			ServerSender.getInstance().send(socket, updateRoomListRequestBodyDto); // 자기 자신만 업데이트(forEach 안 씀)
			return;
		}
		//모두 업데이트
		Server.serverReceiverList.forEach(serverReceiver -> {
			ServerSender.getInstance().send(serverReceiver.socket, updateRoomListRequestBodyDto);
		});
	}

	private void createRoom(String requestBody) {
		roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();

		Room newRoom = Room.builder().roomName(roomName) // 방 만들기를 누른 사람이 owner이므로 해당 소켓의 username이 들어가면 됨.
				.userList(new ArrayList<ServerReceiver>()).build();
		
		//roomName 전송
		RequestBodyDto<String> roomNameRequestBodyDto = 
				new RequestBodyDto<String>("updateRoomName", roomName);
		ServerSender.getInstance().send(socket, roomNameRequestBodyDto);
		
		Server.roomList.add(newRoom);

		updateRoomList(false);
		
		List<String> usernameList = new ArrayList<>();
		usernameList.add(username);
		RequestBodyDto<List<String>> updateUserListDto = 
				new RequestBodyDto<List<String>>("updateUserList", usernameList);
		ServerSender.getInstance().send(socket, updateUserListDto);
	}

	private void join(String requestBody) {		
		roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		
		Server.roomList.forEach(room -> {
			if (room.getRoomName().equals(roomName)) { // 들어가고자 하는 방 이름과 같은가?
				room.getUserList().add(this); // 자기 자신(ServerReceiver)을 userList에 추가

				//메소드로 분리
				updateRoomUserList(room);	//update UserList
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				sendJoinAndQuitRoomMessage(room, username + "님이 접속했습니다.");		//send joinMessage
			}
		});
	}
	
	private void updateRoomUserList(Room room) {
		//update UserList
		List<String> usernameList = new ArrayList<>();
		
		room.getUserList().forEach(con -> { // 방 안의 유저 이름 리스트
			usernameList.add(con.username);
		});

		RequestBodyDto<List<String>> updateUserListDto = 
				new RequestBodyDto<List<String>>("updateUserList", usernameList);
		
		room.getUserList().forEach(serverReceiver -> {
			ServerSender.getInstance().send(serverReceiver.socket, updateUserListDto);
		});
		
	}

	private void sendJoinAndQuitRoomMessage(Room room, String message) {
		room.getUserList().forEach(serverReceiver -> { // 방 안의 사람들에게만 유저리스트 업데이트, 접속메시지 전송
			//send join message
			RequestBodyDto<String> joinMessageDto = 
					new RequestBodyDto<String>("showMessage", message);

			ServerSender.getInstance().send(serverReceiver.socket, joinMessageDto);
		});
	}
	
	private void sendMessage(String requestBody) {
		TypeToken<RequestBodyDto<SendMessage>> typeToken = new TypeToken<>() { };
		RequestBodyDto<SendMessage> requestBodyDto = gson.fromJson(requestBody, typeToken.getType());
		// RequestBodyDto의 제네릭 타입까지 SendMessage로 바꾸려면 typeToken을 쓰는 것이 필요
		SendMessage sendMessage = requestBodyDto.getBody();
		if(!Objects.isNull(sendMessage.getToUsername())) {	//toUsername이 null이 아닌가? -> 귓속말
			whisper(sendMessage);
			return;
		}
		sendMessageAll(sendMessage);
	}
	
	private void whisper(SendMessage sendMessage) {
		Server.roomList.forEach(room -> {
			if (room.getRoomName().equals(roomName)) { // 들어가고자 하는 방 이름과 같은가?
				room.getUserList().forEach(con -> { // 방 안의 유저 이름 리스트
					String toUsername = sendMessage.getToUsername();
					String fromUsername = sendMessage.getFromUsername();
					
					if (con.username.equals(toUsername) || con.username.equals(fromUsername)) {
						
						if (toUsername.equals(fromUsername)) {
							toUsername = fromUsername = "나";
						}
						
						else if (con.username.equals(fromUsername)) {
							fromUsername = "나";
						}
						
						else {
							toUsername = "나";
						}
						
						RequestBodyDto<String> dto = new RequestBodyDto<String>("showMessage"
								,"[ " + fromUsername + " → " + toUsername + " ] : " + sendMessage.getMessageBody());
						
						ServerSender.getInstance().send(con.socket, dto);
					}
				});
			}
		});
	}
	
	private void sendMessageAll(SendMessage sendMessage) {
		Server.roomList.forEach(room -> {	//roomList의 room객체 - userList<ServerReceiver>
			//userList 안에 해당 클라이언트의 ServerReceiver이 들어 있는지 : 방 안에 유저가 있는지
			if (room.getUserList().contains(this)) {	
				room.getUserList().forEach(ServerReceiver -> {
					RequestBodyDto<String> dto = new RequestBodyDto<String>("showMessage",
							"[ " + sendMessage.getFromUsername() + " ] : " + sendMessage.getMessageBody());
					ServerSender.getInstance().send(ServerReceiver.socket, dto);
				});
			}
		});
	}
	
	private void quit(String requestBody) {
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		
		for(Room room : Server.roomList) {	//향상된 for문
			if (room.getRoomName().equals(roomName)) { // 나가고자 하는 방 이름과 같은가?
				
				room.getUserList().remove(this); // 자기 자신(ServerReceiver)을 userList에 삭제
				
				if(room.getUserList().isEmpty()) {	//userList 비었다면
					Server.roomList.remove(room);	//roomList에서의 해당 room 삭제
					//roomListUpdate
					updateRoomList(false);
					break;
				}
				else {
					//메소드 분리
					sendJoinAndQuitRoomMessage(room, username + "님이 퇴장하셨습니다.");	//send quit message
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					updateRoomUserList(room);	//userList update
				}
			}
		}
	}
	
	private void disconnection(String requestBody) {
		Server.serverReceiverList.remove(this);
		updateConnectedUserList();
	}
}
