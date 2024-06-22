use std::fs;
use std::path::Path;

use axum::extract::{self, Multipart};
use axum::response::IntoResponse;
use axum::Json;
use axum::{http, routing::post};
use sqlx::PgPool;
use tokio::io::AsyncWriteExt;

use crate::config;

use super::dtos::CreateEventDTO;
use super::models::Event;
use super::repositories::EventRepository;

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

    let repository = EventRepository::new(pool);
    match repository.create_event(event).await {
        Ok(event) => Ok(Json(event).into_response()),
        Err(_) => Err(http::StatusCode::INTERNAL_SERVER_ERROR),
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
        .route("/", post(create_event))
        .with_state(pool)
}
