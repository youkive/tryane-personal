package com.tryane.saas.modelviewer.dao.common;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tryane.saas.modelviewer.model.common.User;

public interface UserManager extends JpaRepository<User, Integer> {

}
