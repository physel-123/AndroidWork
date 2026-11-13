"""应用配置：从环境变量读取。"""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    database_url: str = "postgresql+asyncpg://jieqi:jieqi_password@localhost:5432/jieqi"
    secret_key: str = "change-me-to-a-random-secret"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 43200

    dashscope_api_key: str = ""


settings = Settings()
