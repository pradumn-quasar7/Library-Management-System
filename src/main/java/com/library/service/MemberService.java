package com.library.service;

import com.library.dao.AuditLogDAO;
import com.library.dao.MemberDAO;
import com.library.dao.UserDAO;
import com.library.dao.impl.AuditLogDAOImpl;
import com.library.dao.impl.MemberDAOImpl;
import com.library.dao.impl.UserDAOImpl;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.AuditLog;
import com.library.model.Member;
import com.library.model.UserStatus;
import com.library.validation.MemberValidator;
import com.library.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MemberService {
    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);

    private final MemberDAO memberDAO;
    private final UserDAO userDAO;
    private final AuditLogDAO auditLogDAO;

    public MemberService() {
        this.memberDAO = new MemberDAOImpl();
        this.userDAO = new UserDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public MemberService(MemberDAO memberDAO, UserDAO userDAO, AuditLogDAO auditLogDAO) {
        this.memberDAO = memberDAO;
        this.userDAO = userDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public Member getMemberById(Long id) {
        Member member = memberDAO.findById(id);
        if (member == null) {
            throw new ResourceNotFoundException("Member not found with ID: " + id);
        }
        return member;
    }

    public Member getMemberByUserId(Long userId) {
        Member member = memberDAO.findByUserId(userId);
        if (member == null) {
            throw new ResourceNotFoundException("Member record not found for user ID: " + userId);
        }
        return member;
    }

    public List<Member> getAllMembers() {
        return memberDAO.findAll();
    }

    public List<Member> searchMembers(String query) {
        return memberDAO.search(query);
    }

    public void updateProfile(Long memberId, String fullName, String phone, String address) {
        Member member = getMemberById(memberId);
        member.setFullName(fullName);
        member.setPhone(phone);
        member.setAddress(address);

        ValidationResult vr = MemberValidator.validate(member, member.getEmail(), null, false);
        if (!vr.isValid()) {
            throw new ValidationException(vr.getErrors());
        }

        memberDAO.update(member);
        auditLogDAO.create(new AuditLog(member.getUserId(), "PROFILE_UPDATED", "MEMBER", memberId, "Member updated profile details"));
        logger.info("Updated profile for member ID {}", memberId);
    }

    public void setMemberStatus(Long memberId, UserStatus status, Long actorUserId) {
        Member member = getMemberById(memberId);
        userDAO.updateStatus(member.getUserId(), status);
        auditLogDAO.create(new AuditLog(actorUserId, "MEMBER_STATUS_CHANGED", "MEMBER", memberId, "Changed member status to " + status));
        logger.info("Changed status of member ID {} to {}", memberId, status);
    }
}
