package org.springframework.samples.petclinic.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Message;
import org.springframework.samples.petclinic.model.Tag;
import org.springframework.samples.petclinic.model.UserTW;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(includeFilters = @ComponentScan.Filter(Service.class))
class MessageServiceTest {
	@Autowired
	protected MessageService messageService;
	@Autowired
	protected UserTWService userTWService;
	@Autowired
	protected TagService tagService;
	
	
	@Test
	@Transactional
	void shouldInsertMessageIntoDataBaseAndGenerateId() {

		UserTW sender = userTWService.findUserById(1);
		ArrayList<Integer> recipients = new ArrayList<>();
		recipients.add(2);
		Message message = new Message();
		message.setSubject("Prueba de Mensaje correcto");
		message.setText("Primer mensaje enviado");
		message.setRecipientsIds(recipients);
		message.setSender(sender);
		
		ArrayList<Integer> tags = new ArrayList<>();
		//tag: planning
		tags.add(1);
		message.setTagList(tags);
		
		try {
			messageService.saveMessage(message);
		} catch (DataAccessException ex) {
			Logger.getLogger(MilestoneServiceTest.class.getName()).log(Level.SEVERE, null, ex);
		}

		assertThat(message.getId()).isNotNull();
	}
	
	
	@Test
	@Transactional
	void shouldFindMessageByTag() {
		Tag tag = tagService.findTagById(1);
		UserTW user = userTWService.findUserById(1);
		Collection<Message> messagesByTag= messageService.findMessagesByTag(user, tag);
		assertThat(messagesByTag).isNotNull();
	}
	
	
	//TODO:
	//NEGATIVE USE CASES:

}
