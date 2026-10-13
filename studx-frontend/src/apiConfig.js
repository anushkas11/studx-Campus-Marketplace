// Central API Configuration for STUDx Client
// In development, requests to /api are proxied to Spring Boot backend (:8080) by Vite.
// You can also override the API base URL via the VITE_API_URL environment variable.

export const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";
