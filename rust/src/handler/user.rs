/* use std::sync::Arc;

use axum::{extract::Query, middleware, response::IntoResponse, routing::*, Extension, Json, Router};
use axum_valid::Valid;
use validator::Validate;

use crate::{
    app::{ error::HttpError, middleware::JWTAuthMiddeware}, dao::user::UserDao, entity::dtos::{
        FilterUserDto, NameUpdateDto, RequestQueryDto, RoleUpdateDto, UserDate,
        UserListResponseDto, UserResponseDto,
    }, AppState
};


pub fn create_router()-> Router{


    Router::new()
        .route(
            "/me", 
            get(get_me)
           
    )
    .route(
        "/users", 
        get(get_users)
    )
    .route("/name", put(update_user_name))   
    .route("/role", put(update_user_role))   

}

pub async fn get_me(
    Extension(state): Extension<Arc<AppState>>,
    Extension(user): Extension<JWTAuthMiddeware>,
) -> Result<impl IntoResponse, HttpError> {
    let filtered_user = FilterUserDto::filter_user(&user.user);

    let response_data = UserResponseDto {
        status: "success".to_string(),
        data: UserDate {
            user: filtered_user,
        },
    };

    Ok(Json(response_data))
}

pub async fn get_users(
    Valid(Query(params)) : Valid<Query<RequestQueryDto>>,
    Extension(state): Extension<Arc<AppState>>,
) -> Result<impl IntoResponse, HttpError> {
    params
        .validate()?;

    let page = params.page.unwrap_or(1);
    let limit = params.limit.unwrap_or(10);

    let users =
        UserDao::get_users(page as u32, limit as u32)
        .await?;

    let user_count =  UserDao::get_user_count()
        .await?;

    let response = UserListResponseDto {
        status: "success".to_string(),
        users: FilterUserDto::filter_users(&users),
        results: user_count,
    };

    Ok(Json(response))
}

pub async fn update_user_name(
    Extension(state): Extension<Arc<AppState>>,
    Extension(user): Extension<JWTAuthMiddeware>,
    Json(body): Json<NameUpdateDto>,
) -> Result<impl IntoResponse, HttpError> {
    body.validate()?;

    let user = &user.user;

    let user_id = uuid::Uuid::parse_str(&user.id.to_string()).unwrap();

    let result =  UserDao::update_user_name(user_id, &body.name)
        .await?;

    let filtered_user = FilterUserDto::filter_user(&result);

    let response = UserResponseDto {
        status: "success".to_string(),
        data: UserDate {
            user: filtered_user,
        },
    };

    Ok(Json(response))
}

pub async fn update_user_role(
    Extension(state): Extension<Arc<AppState>>,
    Extension(user): Extension<JWTAuthMiddeware>,
    Json(body): Json<RoleUpdateDto>,
) -> Result<impl IntoResponse, HttpError> {
    body.validate()?;

    let user = &user.user;

    let user_id = uuid::Uuid::parse_str(&user.id.to_string()).unwrap();

    let result = UserDao::update_user_role(user_id, body.role)
        .await?;

    let filtered_user = FilterUserDto::filter_user(&result);
    let response = UserResponseDto {
        status: "success".to_string(),
        data: UserDate {
            user: filtered_user,
        },
    };

    Ok(Json(response))
}
 */