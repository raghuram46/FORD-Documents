package com.ford.protech.utils;

import com.ford.protech.domain.entity.EmailCommunication;
import com.ford.protech.domain.entity.Sequence;
import com.ford.protech.domain.repository.EmailCommunicationRepository;
import com.ford.protech.domain.repository.SequenceRepository;
import io.netty.util.internal.StringUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class MailUtil {
    private final JavaMailSender javaMailSender;
    private final EmailCommunicationRepository emailCommunicationRepository;
    private final SequenceRepository sequenceRepository;

    @Value("${spring.mail.username}")
    public String fromAddress;

    @Value("${spring.datasource.username}")
    public String dbUsername;

    public MailUtil(JavaMailSender javaMailSender, EmailCommunicationRepository emailCommunicationRepository, SequenceRepository sequenceRepository) {
        this.javaMailSender = javaMailSender;
        this.emailCommunicationRepository = emailCommunicationRepository;
        this.sequenceRepository = sequenceRepository;
    }

    public void sendEmail(String toAddress, String ccAddress, String mailSubject, String mailContent) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(toEmailId(toAddress));
            msg.setCc(ccEmailId(ccAddress));
            msg.setSubject(mailSubject);
            msg.setText(mailContent);
            javaMailSender.send(msg);
        } catch (Exception e) {
            log.error("/*Error in sending an email to user*/" + e.getMessage());
            throw new MailSendException(e.getMessage());
        }
    }

    public void sendMailWithAttachment(String toAddress, String ccAddress, String subject, String body, String fileName, byte[] bytes) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);
            message.setFrom(fromAddress);
            message.setTo(toEmailId(toAddress));
            message.setCc(ccEmailId(ccAddress));
            message.setSubject(subject);
            message.setText(body, true);
            if (bytes != null)
                message.addAttachment(fileName, new ByteArrayResource(bytes));
            javaMailSender.send(mimeMessage);
        } catch (Exception ex) {
            log.error("Error while sending mail with attachment : " + ex.getMessage());
            throw new MailSendException(ex.getMessage());
        }
    }

    public String[] toEmailId(String toAddress) {
        return !StringUtil.isNullOrEmpty(toAddress) ? toAddress.split(InternalJobsConstants.COMMA) : new String[0];
    }

    public String[] ccEmailId(String ccAddress) {
        return !StringUtil.isNullOrEmpty(ccAddress) ? ccAddress.split(InternalJobsConstants.COMMA) : new String[0];
    }

    public void saveMail(String dealerToMailId, String dealerCcMailId, String mailSubject, String messages, String process) {
        Date todayDate = new Date();
        EmailCommunication ea = emailCommunicationRepository.save(EmailCommunication.builder().from(InternalJobsConstants.GBMSF).sendTo(dealerToMailId)
                .subject(mailSubject).message(messages).bccList(null)
                .ccList(dealerCcMailId).sendFlag(InternalJobsConstants.FLAG_Y).sendDateTime(todayDate)
                .createProcess(process).createUser(dbUsername).createdDate(todayDate)
                .lastUpdateProcess(process).lastUpdateUser(dbUsername).lastUpdatedDate(todayDate).build());

        Optional<Sequence> sequence = sequenceRepository.findById(InternalJobsConstants.EMAIL_COMM_SEQUENCE);
        sequence.ifPresentOrElse(val -> {
            val.setSequenceId((long) ea.getEmailKey());
            val.setLastUpdateProcess(InternalJobsConstants.SAVE_MAIL);
            val.setLastUpdateUser(dbUsername);
            val.setLastUpdatedDate(todayDate);
            sequenceRepository.save(val);
        }, () -> sequenceRepository.save(Sequence.builder().sequenceName(InternalJobsConstants.EMAIL_COMM_SEQUENCE).sequenceId((long) ea.getEmailKey())
                .createProcess(InternalJobsConstants.SAVE_MAIL).createUser(dbUsername).createTimeStamp(todayDate)
                .lastUpdateProcess(InternalJobsConstants.SAVE_MAIL).lastUpdateUser(dbUsername).lastUpdatedDate(todayDate).build()));
    }

}

 