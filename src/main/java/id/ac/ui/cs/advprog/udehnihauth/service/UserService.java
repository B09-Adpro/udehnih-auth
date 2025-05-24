package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.dto.response.UserInfoResponse;

public interface UserService {
    UserInfoResponse getUserInfo(Long userId);
    UserInfoResponse getUserInfoWithRoles(Long userId);
}