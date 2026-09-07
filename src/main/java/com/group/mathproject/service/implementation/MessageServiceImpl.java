package com.group.mathproject.service.implementation;

import com.group.mathproject.model.Message;
import com.group.mathproject.repository.MessageRepository;
import com.group.mathproject.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    @Override
    public Message saveMessage(Message msg) {
        return messageRepository.save(msg);
    }

    @Override
    public List<Message> getMessages() {
        return messageRepository.findAll();
    }

    @Override
    public Optional<Message> findById(Integer id) {
        return messageRepository.findById(id);
    }

    @Override
    public void deleteById(Integer id) {
        messageRepository.deleteById(id);
    }





}
