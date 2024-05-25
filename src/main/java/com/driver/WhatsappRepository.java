package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    private HashMap<String , User> userMap;  // added by me
    private HashMap<Integer , Message> messageHashMap;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
        this.userMap = new HashMap<String , User>();
    }

    public String createUser(String name, String mobile) {
        if(userMobile.contains(mobile)){
            throw new RuntimeException("User already exists");
        }
        userMobile.add(mobile);
        User user = new User(name, mobile);
        userMap.put(user.getName(), user);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        if(users.size()<2){
            throw new RuntimeException("Group must have at least 2 users");
        }
        if(users.size()==2){
            Group group = new Group();
            group.setName(users.get(1).getName());
            group.setNumberOfParticipants(2);
            groupUserMap.put(group, users);
            return group;
        }else{
            Group group = new Group();
            group.setName("Group " + (++customGroupCount));
            group.setNumberOfParticipants(users.size());
            groupUserMap.put(group, users);
            return group;
        }
    }

    public int createMessage(String content) {
        if(content != null){
            Message message = new Message();
            message.setId(++messageId);
            message.setContent(content);
            message.setTimestamp(new Date());
            messageHashMap.put(message.getId(), message);
            return message.getId();
        }
        return -1;
    }

    public int sendMessage(Message message, User sender, Group group) {
        if(!groupUserMap.containsKey(group)){
            throw new RuntimeException("Group does not exist");
        }
        if(!groupUserMap.get(group).contains(sender)){
            throw new RuntimeException("You are not allowed to send message");
        }
        groupMessageMap.get(group).add(message);
        senderMap.put(message, sender);
        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) {
        if(!groupUserMap.containsKey(group)){
            throw new RuntimeException("Group does not exist");
        }
        if(!groupUserMap.get(group).contains(approver)){
            throw new RuntimeException("Approver does not have rights");
        }
        if(!groupUserMap.get(group).contains(user)){
            throw new RuntimeException("User is not a participant");
        }
        adminMap.put(group, user);
        return "SUCCESS";
    }

    public int removeUser(User user) {
        if(!userMap.containsKey(user.getName())){
            throw new RuntimeException("User not found");
        }
        if(adminMap.containsValue(user)){
            throw new RuntimeException("Cannot remove admin");
        }
        for(Group group : groupUserMap.keySet()){
            if(groupUserMap.get(group).contains(user)){
                groupUserMap.get(group).remove(user);
                if(groupUserMap.get(group).size() == 0){
                    groupMessageMap.remove(group);
                    groupUserMap.remove(group);
                    adminMap.remove(group);
                }
            }
        }
        userMap.remove(user.getName());
        return groupUserMap.size();
    }


    public String findMessage(Date start, Date end, int k) {
        if(messageHashMap.size() < k){
            throw new RuntimeException("K is greater than the number of messages");
        }
        List<Message> messageList = new ArrayList<Message>(messageHashMap.values());
        Collections.sort(messageList, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o2.getTimestamp().compareTo(o1.getTimestamp());
            }
        });
        return messageList.get(k-1).getContent();
    }
}
