package sms.api.java.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ringcentral.RestClient;
import com.ringcentral.definitions.*;
import com.ringcentral.pubnub.Subscription;
import com.ringcentral.RestException;

public class sms_api_tutorial {
	String RECIPIENT_PHONE_NUMBER = "<ENTER PHONE NUMBER>";
	String RINGCENTRAL_CLIENTID = "<ENTER CLIENT ID>";
	String RINGCENTRAL_CLIENTSECRET = "<ENTER CLIENT SECRET>";
	String RINGCENTRAL_SERVER = "https://platform.devtest.ringcentral.com";

	String RINGCENTRAL_USERNAME = "<YOUR ACCOUNT PHONE NUMBER>";
	String RINGCENTRAL_PASSWORD = "<YOUR ACCOUNT PASSWORD>";
	String RINGCENTRAL_EXTENSION = "<YOUR EXTENSION, PROBABLY";
	boolean waitLoop = true;

	public static void main(String[] args) {
		sms_api_tutorial obj = new sms_api_tutorial();

		try {
			obj.send_sms();
			// obj.send_mms();
			// obj.retrieve_modify();
			// obj.retrieve_delete();
			// obj.receive_reply();
		} catch (RestException | IOException e) {
			e.printStackTrace();
		}
	}

	public void send_sms() throws RestException, IOException {
		RestClient rc = new RestClient(RINGCENTRAL_CLIENTID, RINGCENTRAL_CLIENTSECRET, RINGCENTRAL_SERVER);
		rc.authorize(RINGCENTRAL_USERNAME, RINGCENTRAL_EXTENSION, RINGCENTRAL_PASSWORD);
		CreateSMSMessage parameters = new CreateSMSMessage();
		parameters.from = new MessageStoreCallerInfoRequest().phoneNumber(RINGCENTRAL_USERNAME);
		parameters.to = new MessageStoreCallerInfoRequest[] {
				new MessageStoreCallerInfoRequest().phoneNumber(RECIPIENT_PHONE_NUMBER) };
		parameters.text = "Hello World from Java";

		var response = rc.restapi().account().extension().sms().post(parameters);
		System.out.println("SMS sent. Message status: " + response.messageStatus);
	}

	public void send_mms() throws RestException, IOException {
		RestClient rc = new RestClient(RINGCENTRAL_CLIENTID, RINGCENTRAL_CLIENTSECRET, RINGCENTRAL_SERVER);
		rc.authorize(RINGCENTRAL_USERNAME, RINGCENTRAL_EXTENSION, RINGCENTRAL_PASSWORD);
		var parameters = new CreateMMSMessage();
		parameters.from = new MessageStoreCallerInfoRequest().phoneNumber(RINGCENTRAL_USERNAME);
		parameters.to = new MessageStoreCallerInfoRequest[] {
				new MessageStoreCallerInfoRequest().phoneNumber(RECIPIENT_PHONE_NUMBER) };
		parameters.text = "This is a test MMS message from Java";

		Attachment attachment = new Attachment();
		attachment.fileName = "test.jpg";
		attachment.contentType = "image/jpeg";
		attachment.bytes = Files.readAllBytes(Paths.get("./src/main/resources/test.jpg"));
		Attachment[] attachments = new Attachment[] { attachment };
		parameters.attachments = attachments;

		var response = rc.restapi().account().extension().mms().post(parameters);
		System.out.println("MMS sent. Delivery status: " + response.messageStatus);
		// track_status(rc, response.id, response.messageStatus);
	}

	public void track_status(RestClient rc, String messageId, String messageStatus) {
		while (messageStatus.equals("Queued")) {
			try {
				Thread.sleep(1000);
				var resp = rc.restapi().account().extension().messagestore(messageId).get();
				messageStatus = resp.messageStatus;
				System.out.println("MMS message delivery status: " + messageStatus);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void retrieve_modify() throws RestException, IOException {
		RestClient rc = new RestClient(RINGCENTRAL_CLIENTID, RINGCENTRAL_CLIENTSECRET, RINGCENTRAL_SERVER);
		rc.authorize(RINGCENTRAL_USERNAME, RINGCENTRAL_EXTENSION, RINGCENTRAL_PASSWORD);

		var requestParams = new ListMessagesParameters();
		requestParams.readStatus = new String[] { "Unread" };

		var resp = rc.restapi().account().extension().messagestore().list(requestParams);
		int count = resp.records.length;
		System.out.println(String.format("Retrieving a list of {0} messages.", count));
		for (var record : resp.records) {
			var messageId = record.id;
			var updateRequest = new UpdateMessageRequest();
			updateRequest.readStatus = "Read";

			var result = rc.restapi().account().extension().messagestore(messageId).put(updateRequest);
			var readStatus = result.readStatus;
			System.out.println("Message status has been changed to " + readStatus);
			break;
		}
	}

	public void retrieve_delete() throws RestException, IOException {
		RestClient rc = new RestClient(RINGCENTRAL_CLIENTID, RINGCENTRAL_CLIENTSECRET, RINGCENTRAL_SERVER);
		rc.authorize(RINGCENTRAL_USERNAME, RINGCENTRAL_EXTENSION, RINGCENTRAL_PASSWORD);

		var requestParams = new ListMessagesParameters();
		requestParams.readStatus = new String[] { "Read" };

		var resp = rc.restapi().account().extension().messagestore().list(requestParams);
		int count = resp.records.length;
		System.out.println(String.format("Get get a list of {0} messages.", count));
		for (var record : resp.records) {
			var messageId = record.id;
			rc.restapi().account().extension().messagestore(messageId).delete();
			System.out.println(String.format("Message {0} has been deleted.", messageId));
			break;
		}
	}

	public void receive_reply() throws RestException, IOException {
		RestClient rc = new RestClient(RINGCENTRAL_CLIENTID, RINGCENTRAL_CLIENTSECRET, RINGCENTRAL_SERVER);
		rc.authorize(RINGCENTRAL_USERNAME, RINGCENTRAL_EXTENSION, RINGCENTRAL_PASSWORD);

		var eventFilters = new String[] { "/restapi/v1.0/account/~/extension/~/message-store/instant?type=SMS" };
		Subscription subscription = new Subscription(rc, eventFilters, (message) -> {
			reply_sms_message(rc, message);
		});

		subscription.subscribe();

		System.out.println("Waiting for notifications ...");
		while (waitLoop) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		subscription.revoke();
		System.out.println("Done!");
	}

	public void reply_sms_message(RestClient rc, String message) {
		JSONObject jsonObj = JSON.parseObject(message);
		if (jsonObj.getString("event").contains("instant?type=SMS")) {
			InstantMessageEvent notification = JSON.parseObject(message, InstantMessageEvent.class);
			InstantMessageEventBody body = notification.body;
			String senderNumber = body.from.phoneNumber;

			CreateSMSMessage postParameters = new CreateSMSMessage();
			postParameters.from = new MessageStoreCallerInfoRequest().phoneNumber(RINGCENTRAL_USERNAME);
			postParameters.to = new MessageStoreCallerInfoRequest[] {
					new MessageStoreCallerInfoRequest().phoneNumber(senderNumber) };
			postParameters.text = "This is an automatic reply. Thank you for your message!";
			rc.restapi().account().extension().sms().post(postParameters);
			System.out.println("Replied message sent.");
			waitLoop = false;
		} else {
			System.out.println("Not an event we are waiting for.");
		}
	}
}
