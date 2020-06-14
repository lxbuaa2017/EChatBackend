package com.example.echatbackend.service;

import com.example.echatbackend.dao.*;
import com.example.echatbackend.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService extends BaseService<Group, Integer, GroupRepository> {

    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Autowired
    public GroupService(GroupUserRepository groupUserRepository, UserRepository userRepository, GroupRepository groupRepository,ConversationRepository conversationRepository,
                        MessageRepository messageRepository) {
        this.groupUserRepository = groupUserRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository =messageRepository;
    }

    public Group findGroupById(int id) {
        Optional<Group> group = baseRepository.findById(id);
        if (group.isEmpty()) {
            return null;
        }
        return group.get();
    }

    public User[] findUserByGroup(Group group) {
        List<GroupUser> groupUsers = groupUserRepository.findAllByGroup(group);
        return groupUsers.stream().map(GroupUser::getUser).toArray(User[]::new);
    }

    public List<Group> searchGroupByCode(String keyword, int offset, int limit) {
        Specification<Group> groupSpecification = (Specification<Group>) (root, criteriaQuery, cb) -> cb.like(root.get("code"), "%" + keyword + "%");
        Sort sort = Sort.by(Sort.Order.asc("code"));
        return baseRepository.findAll(groupSpecification, PageRequest.of(offset, limit, sort)).getContent();
    }

    public List<Group> searchGroupByName(String keyword, int offset, int limit) {
        Specification<Group> groupSpecification = (Specification<Group>) (root, criteriaQuery, cb) -> cb.like(root.get("name"), "%" + keyword + "%");
        Sort sort = Sort.by(Sort.Order.asc("name"));
        return baseRepository.findAll(groupSpecification, PageRequest.of(offset, limit, sort)).getContent();
    }

    public Group[] findGroupByUser(User user) {
        List<GroupUser> groupUsers = groupUserRepository.findAllByUser(user);
        return groupUsers.stream().map(GroupUser::getGroup).toArray(Group[]::new);
    }

    public boolean isUserInGroup(Integer groupId, Integer userId) {
        User user = userRepository.findById(userId).get();
        List<GroupUser> groupUsers = groupUserRepository.findAllByUser(user);
        Boolean judge = false;
        for (GroupUser item : groupUsers) {
            if (item.getGroup().getId() == groupId) {
                judge = true;
                break;
            }
        }
        return judge;
    }

    public Message quitGroup(Integer userId, Integer groupId){
                /*
        0.判断是不是群主，是就对群里每个人遍历执行退群操作
        退群操作：
        1.删除groupUser
        2.群聊conversation。getUsers()删除user，删后若已空则删除Group、Conversation
        3. user.getConversationList().remove(conversation);
         */
        User user = userRepository.findById(userId).get();
        Group group = groupRepository.findById(groupId).get();
        GroupUser groupUser = groupUserRepository.findByGroupAndUser(group,user);
        if(groupUser.getIsHolder()){
            List<GroupUser> userList = groupUserRepository.findAllByGroup(group);
            for(GroupUser each:userList){
                quitGroupOperation(each.getUser(),each.getGroup());
            }
            return null;
        }
        else {
            quitGroupOperation(user,group);
            //通知群聊有人退出
            Message org_message = new Message();
            org_message.setType("org");
            org_message.setUserM(user);
            org_message.setGroup(group);
            org_message.setMessage(user.getUserName()+" 退出了群聊！");
            org_message.setState("group");
            org_message.setConversationId(groupId.toString());
            org_message = messageRepository.saveAndFlush(org_message);
            return org_message;
        }
    }
    private void quitGroupOperation(User user,Group group){
                /*
        退群操作：
        1.删除groupUser
        2.群聊conversation.getUsers()删除user，删后若已空则删除Group、Conversation
        3. user.getConversationList().remove(conversation);
         */
        Integer groupId = group.getId();
        Integer userId = user.getId();
        GroupUser groupUser = groupUserRepository.findByGroupAndUser(group,user);
        Conversation conversation = conversationRepository.findByConversationId(groupId.toString());
        List<User> userList = conversation.getUsers();
        userList.removeIf(each->each.getId().equals(userId));
        user.getConversationList().removeIf(each->each.getConversationId().equals(groupId.toString()));
        groupUserRepository.delete(groupUser);
        userRepository.saveAndFlush(user);
        conversation = conversationRepository.saveAndFlush(conversation);
        if (userList.size()==0){
            groupRepository.delete(group);
//            conversationRepository.delete(conversation);
        }
    }
//    public boolean deleteGroup(User user, int groupId) {
//
//        Optional<Group> optionalGroup = groupRepository.findById(groupId);
//        int myId = user.getId();
//        if (optionalGroup.isEmpty()) {
//            return false;
//        }
//    }
   /*
   todo
   加群（把SocketHandler里的搬过来，优化代码）
    */
}
