package io.github.shuoros.allAboutSpring.controller;

import java.lang.invoke.MethodHandles;
import java.security.Principal;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;

import io.github.shuoros.allAboutSpring.socket.SocketEndpoints;

@Controller
public class SocketController {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	public SocketController(SimpMessagingTemplate messagingTemplate) {

		this.messagingTemplate = messagingTemplate;
	}

	@EventListener
	@Async
	public void crashServer(ApplicationReadyEvent e) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Long time = 0L;
				while (true) {
					JSONObject response = new JSONObject();
					response.put("status", 200);
					response.put("destination", "timer");
					response.put("data", new JSONObject().put("time", time));
					messagingTemplate.convertAndSend(SocketEndpoints.SUBSCRIBE_QUEUE + "/public", response.toString());
					time++;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	@MessageMapping(SocketEndpoints.ENDPOINT_USER + "/chat")
	public void chat(Message<Object> message, @Payload String payload, Principal principal) throws Exception {
		String session = principal.getName();
		log.info("<=== handleLogInCheckEvent: session=" + session + ", payload=" + payload);
		JSONObject data = new JSONObject(payload);
		JSONObject response = new JSONObject();
		response.put("status", 200);
		response.put("destination", "chat");
		response.put("data",
				new JSONObject().put("from", data.getString("from")).put("message", data.getString("message")));
		messagingTemplate.convertAndSend(SocketEndpoints.SUBSCRIBE_QUEUE + "/public", response.toString());
	}
}