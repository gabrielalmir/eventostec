use std::fs;
use std::path::Path;

use axum::extract::{self, Multipart};
use axum::response::IntoResponse;
use axum::{http, routing::{get, post}};
use sqlx::{query, PgPool};
use tokio::io::AsyncWriteExt;

use crate::dtos::CreateEventDTO;
use crate::config;
use crate::models::Event;

pub async fn health () -> http::StatusCode {
    http::StatusCode::OK
}

pub async fn create_event (
    extract::State(pool): extract::State<PgPool>,
    mut multipart: Multipart,
) -> Result<impl IntoResponse, http::StatusCode> {
    let create_event_dto = CreateEventDTO::from_multipart(&mut multipart).await.unwrap();

    let event = Event::new(
        create_event_dto.title,
        create_event_dto.description,
        upload_image(&create_event_dto.image, &create_event_dto.original_filename).await.unwrap(),
        create_event_dto.event_url,
        create_event_dto.date,
        create_event_dto.remote,
    );

    let sql = r#"
        INSERT INTO event (id, title, description, image_url, event_url, date, remote)
        VALUES ($1, $2, $3, $4, $5, $6, $7)"#;
    let query = query(sql)
        .bind(&event.id)
        .bind(&event.title)
        .bind(&event.description)
        .bind(&event.image_url)
        .bind(&event.event_url)
        .bind(&event.date)
        .bind(&event.remote)
        .execute(&pool)
        .await;

    match query {
        Ok(_) => Ok((http::StatusCode::CREATED, axum::Json(event))),
        Err(e) => {
            eprintln!("Failed to execute query: {:?}", e);
            Err(http::StatusCode::INTERNAL_SERVER_ERROR)
        }
    }
}

async fn upload_image (data: &[u8], original_filename: &str) -> Result<String, std::io::Error> {
    // create image folder if it doesn't exist
    if !Path::new("images").exists() {
        fs::create_dir("images").unwrap();
    }

    let filename = uuid::Uuid::new_v4().to_string();
    let path = format!("images/{}-{}", filename, original_filename);
    let mut file = tokio::fs::File::create(&path).await?;

    match file.write_all(data).await {
        Ok(_) => Ok(path),
        Err(e) => {
            eprintln!("Failed to upload image: {:?}", e);
            return Err(e);
        }
    }
}

pub async fn routes() -> axum::Router<()> {
    let pool = config::pool().await;

    axum::Router::new()
        .route("/health", get(health))
        .route("/event", post(create_event))
        .with_state(pool)
}
