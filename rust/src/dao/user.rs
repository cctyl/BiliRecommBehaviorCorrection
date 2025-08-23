/* use std::sync::LazyLock;

use chrono::{DateTime, Utc};
use uuid::Uuid;

use crate::{
    app::database::get_db,
    entity::models::{User, UserRole},
};

#[derive(Debug)]
pub struct UserDao;

impl UserDao {
    pub async fn get_user(
        user_id: Option<Uuid>,
        name: Option<&str>,
        email: Option<&str>,
        token: Option<&str>,
    ) -> Result<Option<User>, sqlx::Error> {
        let mut user: Option<User> = None;

        // CREATE TABLE "users" (
        //     id UUID NOT NULL PRIMARY KEY DEFAULT (uuid_generate_v4 ()),
        //     name VARCHAR (100) NOT NULL,
        //     email VARCHAR (255) NOT NULL UNIQUE,
        //     verified BOOLEAN NOT NULL DEFAULT FALSE,
        //     PASSWORD VARCHAR (100) NOT NULL,
        //     verification_token VARCHAR (255),
        //     token_expires_at TIMESTAMP WITH TIME ZONE,
        //     ROLE user_role NOT NULL DEFAULT 'user',
        //     created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
        //      updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
        //   );
        if let Some(user_id) = user_id {
            user = sqlx::query_as!(
                User,
                r#"
                    select id, name, email, password, role as "role: UserRole", verified, verification_token, token_expires_at, created_at, updated_at
                    from users
                    where id = $1
                "#,
                user_id
            ).fetch_optional(  get_db()).await?;
        } else if let Some(name) = name {
            user = sqlx::query_as!(
                User,
                r#"
                    select id, name, email, password, role as "role: UserRole", verified, verification_token, token_expires_at, created_at, updated_at
                    from users
                    where name = $1
                "#,
                name
            ).fetch_optional(  get_db()).await?;
        } else if let Some(email) = email {
            user = sqlx::query_as!(
                User,
                r#"
                    select id, name, email, password, role as "role: UserRole", verified, verification_token, token_expires_at, created_at, updated_at
                    from users
                    where email = $1
                "#,
                email
            ).fetch_optional(  get_db()).await?;
        } else if let Some(token) = token {
            user = sqlx::query_as!(
                User,
                r#"
                    select id, name, email, password, role as "role: UserRole", verified, verification_token, token_expires_at, created_at, updated_at
                    from users
                    where verification_token = $1
                "#,
                token
            ).fetch_optional(  get_db()).await?;
        }

        Ok(user)
    }

    pub async fn get_users(page: u32, limit: u32) -> Result<Vec<User>, sqlx::Error> {
        let offset = (page - 1) * limit as u32;

        let users = sqlx::query_as!(
            User,
            r#"
            
                select id, name, email, password, role as "role: UserRole", verified, verification_token, token_expires_at, created_at, updated_at
                from users
                order by created_at desc
                limit $1 offset $2
            "#,
            limit as i64,
            offset as i64
        ).fetch_all(get_db()).await?;

        Ok(users)
    }

    pub async fn save_user<T: Into<String> + Send>(
        name: T,
        email: T,
        password: T,
        verification_token: T,
        token_expires_at: DateTime<Utc>,
    ) -> Result<User, sqlx::Error> {
        let user = sqlx::query_as!(
            User,
            r#"
                insert into users (name, email, password, verification_token, token_expires_at)
                values ($1, $2, $3, $4, $5)
                returning id, name, email, password, role as "role: UserRole", verified, verification_token, token_expires_at, created_at, updated_at
            "#,
            name.into(),
            email.into(),
            password.into(),
            verification_token.into(),
            token_expires_at
        ).fetch_one(get_db()).await?;
        Ok(user)
    }

    pub async fn get_user_count() -> Result<i64, sqlx::Error> {
        let count = sqlx::query_scalar!(r#"select count(*) from users "#)
            .fetch_one(get_db())
            .await?;

        Ok(count.unwrap_or(0))
    }

    pub async fn update_user_name<T: Into<String> + Send>(
        user_id: Uuid,
        new_name: T,
    ) -> Result<User, sqlx::Error> {
        let user = sqlx::query_as!(
            User,
            r#"
                update users
                set name = $1,updated_at = now()
                where id = $2
                returning id, name, email, password, role as "role: UserRole", verified, verification_token, token_expires_at, created_at, updated_at
            "#,
            new_name.into(),
            user_id
        ).fetch_one(get_db()).await?;

        Ok(user)
    }

    pub async fn update_user_role(user_id: Uuid, new_role: UserRole) -> Result<User, sqlx::Error> {
        let user = sqlx::query_as!(
            User,
            r#"
                update users 
                set role=$1 ,updated_at = now()
                where id = $2
                returning id, name, email, password, role as "role: UserRole", verified, verification_token, token_expires_at, created_at, updated_at
            "#,
            new_role as UserRole, //  as UserRole  或者 as _ 必须存在
            user_id
        )
        .fetch_one(get_db())
        .await?;

        Ok(user)
    }

    pub async fn update_user_password(
        user_id: Uuid,
        new_password: String,
    ) -> Result<User, sqlx::Error> {
        let user = sqlx::query_as!(

            User,
            r#"
                update users
                set password =$1 ,updated_at = now()
                where id = $2
                returning id, name, email, password, role as "role: UserRole", verified, verification_token, token_expires_at, created_at, updated_at
            "#,
            new_password,
            user_id
        ).fetch_one(get_db())
        .await?;

        Ok(user)
    }

    pub async fn verifed_token(token: &str) -> Result<(), sqlx::Error> {
        let _ = sqlx::query!(
            r#"
                update users 
                set verified = true,
                updated_at = now(),
                verification_token = null,
                token_expires_at = null
                where verification_token = $1
            "#,
            token
        )
        .execute(get_db())
        .await?;

        Ok(())
    }

    pub async fn add_verifed_token(
        user_id: Uuid,
        token: &str,
        token_expires_at: DateTime<Utc>,
    ) -> Result<(), sqlx::Error> {
        let _ = sqlx::query!(
            r#"
                update users
                set verification_token =$1,token_expires_at = $2, updated_at = now()
                where id = $3
            "#,
            token,
            token_expires_at,
            user_id
        )
        .execute(get_db())
        .await?;

        Ok(())
    }
}
 */