package socket_project_server.Entity;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import socket_project_server.ServerReceiver;

@Builder
@Data
public class Room {
	private String roomName;
	private String owner;
	private List<ServerReceiver> userList;
}
