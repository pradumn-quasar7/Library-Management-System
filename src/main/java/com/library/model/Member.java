package com.library.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Member implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String membershipId;
    private String fullName;
    private String phone;
    private String address;
    private LocalDate joinedAt;

    // Transient joined fields from User
    private String email;
    private UserStatus status;

    public Member() {
    }

    public Member(Long id, Long userId, String membershipId, String fullName, String phone, String address, LocalDate joinedAt) {
        this.id = id;
        this.userId = userId;
        this.membershipId = membershipId;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.joinedAt = joinedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDate joinedAt) {
        this.joinedAt = joinedAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id) && Objects.equals(membershipId, member.membershipId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, membershipId);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", membershipId='" + membershipId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
