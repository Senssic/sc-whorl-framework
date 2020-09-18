
package com.whorl.web.model;

import org.apache.ibatis.type.Alias;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Alias("users")
@Table(name = "users")
@Getter@Setter
public class Users implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "accountName")
    private String accountName;


    @Column(name = "password")
    private String password;
}
