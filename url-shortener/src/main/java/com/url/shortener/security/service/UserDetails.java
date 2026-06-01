/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.url.shortener.security.service;

import java.util.Collection;

interface UserDetails {

    Collection<? extends GrantedAuthority> getAuthorities();

}
