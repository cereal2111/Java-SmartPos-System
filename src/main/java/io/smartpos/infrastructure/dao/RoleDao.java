/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.infrastructure.dao;

import io.smartpos.core.domain.security.Role;
import java.util.List;

public interface RoleDao {

    Role findById(int roleId);

    List<Role> findByUserId(int userId);
}
