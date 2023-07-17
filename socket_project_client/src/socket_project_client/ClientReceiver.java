package socket_project_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.gson.Gson;

import socket_project_client.dto.RequestBodyDto;

public class ClientReceiver extends Thread {
	private String username;
	@Override
	public void run() {
		ClientGUI clientGUI = ClientGUI.getInstance();
		username = ClientGUI.getInstance().getUsername();
		ClientGUI.getInstance().getUserNameLabel().setText(username + "님 환영합니다!");
		while (true) {
			try {
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(clientGUI.getSocket().getInputStream()));
				String requestBody = bufferedReader.readLine();

				requestController(requestBody);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Server에서 받아온 데이터를 클라이언트들에게 전송
	private void requestController(String requestBody) {
		Gson gson = new Gson();
		String resource = gson.fromJson(requestBody, RequestBodyDto.class).getResource();

		switch (resource) {
		case "updateRoomName":
			String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
			ClientGUI.getInstance().getRoomNameLabel().setText("[ " + roomName + " ]");
			break;
			
		case "updateRoomList":
			List<String> roomList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
			ClientGUI.getInstance().getRoomListModel().clear();
			ClientGUI.getInstance().getRoomListModel().addAll(roomList);
			break;
			
		case "connectedUserList":
			List<String> connectedUserList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
			//로비의 connectedUserList 중 자신 이름 색 바꿔줌
			ClientGUI.getInstance().getConnectedUserListcellRenderer().setTargetIndex(connectedUserList.indexOf(username));
			ClientGUI.getInstance().getConnectedUserList().setCellRenderer(ClientGUI.getInstance().getConnectedUserListcellRenderer());
			
			ClientGUI.getInstance().getConnectedUserListModel().clear();
			ClientGUI.getInstance().getConnectedUserListModel().addAll(connectedUserList);
			break;
			
		case "clearTextArea":
			ClientGUI.getInstance().getChattingTextArea().setText("");
			break;
			
		case "showMessage":
			String messageContent = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
			ClientGUI.getInstance().getChattingTextArea().append(messageContent + "\n");
			break;
			
		case "updateUserList":
			List<String> usernameList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
			//방 안의 userList 중 자신 이름 색 바꿔줌
			ClientGUI.getInstance().getUserListcellRenderer().setTargetIndex(usernameList.indexOf(username));
			ClientGUI.getInstance().getUserList().setCellRenderer(ClientGUI.getInstance().getUserListcellRenderer());
			//List의 0번째 인덱스가 방장이 됨
			usernameList.set(0, usernameList.get(0) + "(방장)");
			ClientGUI.getInstance().getUserListModel().clear(); // 리스트 초기화 시키고		
			ClientGUI.getInstance().getUserListModel().addAll(usernameList); // 새로 받아온 리스트를 addAll
			break;
			
		}
	}

}
