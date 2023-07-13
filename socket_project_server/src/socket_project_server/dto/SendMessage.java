package socket_project_server.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SendMessage {
	private String fromUsername;
	private String toUsername;	//귓속말 기능
	private String messageBody;
}
