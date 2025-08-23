use chrono::{DateTime, Utc};
use core::str;
use serde::{Deserialize, Serialize};
use validator::Validate;


#[derive(Debug, Default, Clone, Serialize, Deserialize, Validate)]
pub struct RegisterUserDto {
    #[validate(length(min = 1, message = "用户名不能为空"))]
    pub name: String,

    #[validate(
        length(min = 1, max = 100, message = "邮箱长度必须在1到100个字符之间"),
        email(message = "邮箱格式不正确")
    )]
    pub email: String,

    #[validate(length(min = 1, max = 100, message = "密码不能为空"))]
    pub password: String,

    #[validate(
        length(min = 1, message = "密码不能为空"),
        must_match(other = "password", message = "密码不匹配")
    )]
    #[serde(rename = "passwordConfirm")]
    pub password_confirm: String,
}

#[derive(Serialize, Deserialize, Validate)]
pub struct RequestQueryDto {
    #[validate(range(min = 1, message = "页码必须大于0"))]
    pub page: Option<usize>,
    #[validate(range(min = 1, max = 50, message = "每页数量必须大于0,且小于等于50"))]
    pub limit: Option<usize>,
}

#[derive(Serialize, Deserialize, Validate)]
pub struct VerifyEmailQueryDto {
    #[validate(length(min = 1, message = "Token is required."))]
    pub token: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct FilterUserDto {
    pub id: String,
    pub name: String,
    pub email: String,
    pub role: String,
    pub verified: bool,

    #[serde(rename = "createdAt")]
    pub created_at: DateTime<Utc>,
    #[serde(rename = "updatedAt")]
    pub updated_at: DateTime<Utc>,
}



#[derive(Validate, Debug, Default, Clone, Serialize, Deserialize)]
pub struct LoginUserDto {
    #[validate(
        length(min = 1, message = "Email is required"),
        email(message = "Email is invalid")
    )]
    pub email: String,
    #[validate(length(min = 1, message = "Password is required"))]
    pub password: String,
}
#[derive(Debug, Serialize, Deserialize)]
pub struct UserDate {
    pub user: FilterUserDto,
}
#[derive(Debug, Validate, Default, Clone, Serialize, Deserialize)]
pub struct UserPasswordUpdateDto {
    #[validate(length(min = 1, message = "New password is required."))]
    pub new_password: String,

    #[validate(
        length(min = 1, message = "New password confirm is required."),
        must_match(other = "new_password", message = "new passwords do not match")
    )]
    pub new_password_confirm: String,

    #[validate(length(min = 1, message = "Old password is required."))]
    pub old_password: String,
}
#[derive(Debug, Serialize, Deserialize)]
pub struct UserResponseDto {
    pub status: String,
    pub data: UserDate,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct UserListResponseDto {
    pub status: String,
    pub users: Vec<FilterUserDto>,
    pub results: i64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct UserLoginResponseDto {
    pub status: String,
    pub token: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Response {
    pub status: &'static str,
    pub message: String,
}

#[derive(Validate, Debug, Default, Clone, Serialize, Deserialize)]
pub struct NameUpdateDto {
    #[validate(length(min = 1, message = "用户名不能为空"))]
    pub name: String,
}



#[derive(Validate, Debug, Clone, Serialize, Deserialize)]
pub struct ForgotPasswordRequestDto {
    #[validate(
        length(min = 1, max = 100, message = "邮箱长度必须在1到100个字符之间"),
        email(message = "邮箱格式不正确")
    )]
    pub email: String,
}

#[derive(Validate, Debug, Clone, Serialize, Deserialize)]
pub struct ResetPasswordRequestDto {
    #[validate(length(min = 1, message = "token不能为空"))]
    pub token: String,

    #[validate(length(min = 1, message = "新密码不能为空"))]
    pub new_password: String,

    #[validate(
        length(min = 1, message = "确认密码不能为空"),
        must_match(other = "new_password", message = "新密码和确认密码不匹配")
    )]
    pub new_password_confirm: String,
}
