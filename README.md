# Messages API C# Tutorial

[![Community][community-img]][community-url]
[![Twitter][twitter-img]][twitter-url]

 [community-img]: https://img.shields.io/badge/dynamic/json.svg?label=community&colorB=&suffix=%20users&query=$.approximate_people_count&uri=http%3A%2F%2Fapi.getsatisfaction.com%2Fcompanies%2F102909.json
 [community-url]: https://devcommunity.ringcentral.com/ringcentraldev
 [twitter-img]: https://img.shields.io/twitter/follow/ringcentraldevs.svg?style=social&label=follow
 [twitter-url]: https://twitter.com/RingCentralDevs

A tutorial to teach users to use RingCentral Messages API. The following topics are included:

- How to send SMS
- How to send MMS
- How to track delivery status of messages
- How to modify the message's read status.
- How to delete messages.
- How to receive and reply to SMS messages

### Clone - Setup - Run the project
```
$ git clone https://github.com/ringcentral-tutorials/sms-api-java-demo
$ cd sms-api-java-demo
$ open the project using Eclipse IDE
```
Specify your app client id and client secret as well as account login credentials to the constant defined within the brackets "< >".

#### How to send SMS
Uncomment the function call below and run the app
```
//obj.send_sms();
```
#### How to send MMS
Uncomment the function call below and run the app
```
//obj.send_mms();
```
#### How to track delivery status of messages
Uncomment the function call below and run the app
```
//obj.track_status(rc, resp.id, resp.messageStatus);
```
#### How to retrieve and modify message's read status
Uncomment the function call below and run the app
```
//obj.retrieve_modify();
```
#### How to delete messages
Uncomment the function call below and run the app
```
//obj.retrieve_delete();
```

#### How to receive and reply to SMS messages
Uncomment the function call below and run the app
```
//obj.receive_reply();
```

## RingCentral Java SDK
The SDK is available at https://github.com/ringcentral/ringcentral-java
